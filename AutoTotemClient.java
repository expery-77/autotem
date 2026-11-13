package com.example.autototem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Auto Totem (Fabric, Minecraft 1.21.4).
 *
 * Offhand'de totem yoksa envanterdeki bir totemi offhand'e alir. Takas, rastgele bir
 * tick gecikmesinden sonra yapilir. Ayarlar menusu: Sag Shift (degistirilebilir).
 */
public class AutoTotemClient implements ClientModInitializer {

	public static KeyBinding toggleKey;
	public static KeyBinding guiKey;

	private static final int OFFHAND_BUTTON = 40;   // SWAP + 40 = offhand ile degistir
	private static final int FIRST_SLOT = 9;        // ana envanter baslangici (PlayerScreenHandler)
	private static final int LAST_SLOT = 44;        // hotbar sonu (36-44 hotbar)
	private static final int OFFHAND_SLOT = 45;
	private static final int COOLDOWN_AFTER_SWAP = 4;
	private static final int HUD_SCAN_INTERVAL = 10;

	private int pendingDelay = -1;
	private int cooldown = 0;
	private int scanTimer = 0;

	@Override
	public void onInitializeClient() {
		AutoTotemConfig.load();

		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"AutoTotem Toggle",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				"AutoTotem"
		));
		guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"AutoTotem Menu",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				"AutoTotem"
		));

		AutoTotemHud.init();
		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
	}

	private void onTick(MinecraftClient client) {
		AutoTotemConfig cfg = AutoTotemConfig.INSTANCE;
		ClientPlayerEntity player = client.player;

		// --- Kisayollar ---
		while (toggleKey.wasPressed()) {
			cfg.enabled = !cfg.enabled;
			pendingDelay = -1;
			AutoTotemConfig.save();
			if (player != null && !cfg.showHud) {
				player.sendMessage(Text.literal("AutoTotem: " + (cfg.enabled ? "ACIK" : "KAPALI")), true);
			}
		}
		while (guiKey.wasPressed()) {
			if (client.currentScreen == null) {
				client.setScreen(new AutoTotemScreen(null));
			}
		}

		if (player == null || client.interactionManager == null) {
			return;
		}

		PlayerScreenHandler handler = player.playerScreenHandler;

		// --- HUD icin totem sayisi (10 tick'te bir) ---
		if (cfg.showHud && --scanTimer <= 0) {
			scanTimer = HUD_SCAN_INTERVAL;
			AutoTotemHud.setTotemCount(countTotems(handler));
		}

		// --- Auto totem ---
		if (!cfg.enabled) {
			return;
		}
		// Baska bir ekran aciksa (sandik, menu vs.) karismayalim
		if (client.currentScreen != null) {
			return;
		}
		if (cooldown > 0) {
			cooldown--;
			return;
		}
		// Offhand'de zaten totem varsa is yok
		if (player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) {
			pendingDelay = -1;
			return;
		}
		if (cfg.healthHearts < 10 && player.getHealth() > cfg.healthHearts * 2.0f) {
			pendingDelay = -1;
			return;
		}

		int slot = findTotemSlot(handler);
		if (slot == -1) {
			pendingDelay = -1;
			return;
		}

		// Rastgele gecikme
		if (pendingDelay < 0) {
			int lo = cfg.minDelay;
			int hi = Math.max(cfg.minDelay, cfg.maxDelay);
			pendingDelay = hi > lo ? ThreadLocalRandom.current().nextInt(lo, hi + 1) : lo;
		}
		if (pendingDelay > 0) {
			pendingDelay--;
			return;
		}

		client.interactionManager.clickSlot(
				handler.syncId,
				slot,
				OFFHAND_BUTTON,
				SlotActionType.SWAP,
				player
		);
		pendingDelay = -1;
		cooldown = COOLDOWN_AFTER_SWAP;
	}

	/** PlayerScreenHandler slot numarasi: 9-35 ana envanter, 36-44 hotbar. */
	private static int findTotemSlot(PlayerScreenHandler handler) {
		for (int i = FIRST_SLOT; i <= LAST_SLOT; i++) {
			if (handler.getSlot(i).getStack().isOf(Items.TOTEM_OF_UNDYING)) {
				return i;
			}
		}
		return -1;
	}

	/** Envanter + hotbar + offhand'deki toplam totem. */
	private static int countTotems(PlayerScreenHandler handler) {
		int total = 0;
		for (int i = FIRST_SLOT; i <= OFFHAND_SLOT; i++) {
			ItemStack stack = handler.getSlot(i).getStack();
			if (stack.isOf(Items.TOTEM_OF_UNDYING)) {
				total += stack.getCount();
			}
		}
		return total;
	}
}
