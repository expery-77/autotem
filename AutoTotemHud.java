package com.example.autototem;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Ekranda "AutoTotem ON/OFF  xN" gostergesi.
 * Optimizasyon: yazi (Text) sadece durum veya totem sayisi degisince yeniden olusturulur,
 * her karede yeni nesne uretilmez. Totem sayisi tick'te 10 tick'te bir sayilir.
 */
public final class AutoTotemHud {
	private static final int ON_COLOR = 0xFF3DDC84;
	private static final int OFF_COLOR = 0xFFE5484D;
	private static final int BG_COLOR = 0xA0101418;

	private static int totemCount = 0;

	private static Text cachedLabel = Text.empty();
	private static int cachedWidth = 0;
	private static boolean cachedEnabled = false;
	private static int cachedCount = -1;

	private AutoTotemHud() {
	}

	public static void init() {
		HudRenderCallback.EVENT.register(AutoTotemHud::render);
	}

	public static void setTotemCount(int count) {
		totemCount = count;
	}

	private static void render(DrawContext context, RenderTickCounter tickCounter) {
		AutoTotemConfig cfg = AutoTotemConfig.INSTANCE;
		if (!cfg.showHud) {
			return;
		}
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.options.hudHidden) {
			return;
		}

		TextRenderer tr = mc.textRenderer;
		if (cfg.enabled != cachedEnabled || totemCount != cachedCount) {
			rebuild(cfg.enabled, tr);
		}

		int boxW = cachedWidth + 14;
		int boxH = 16;
		int margin = 4;
		int screenW = mc.getWindow().getScaledWidth();
		int screenH = mc.getWindow().getScaledHeight();

		boolean right = cfg.hudCorner == 1 || cfg.hudCorner == 3;
		boolean bottom = cfg.hudCorner >= 2;
		int x = right ? screenW - boxW - margin : margin;
		int y = bottom ? screenH - boxH - margin : margin;

		context.fill(x, y, x + boxW, y + boxH, BG_COLOR);
		context.fill(x, y, x + 2, y + boxH, cfg.enabled ? ON_COLOR : OFF_COLOR);
		context.drawTextWithShadow(tr, cachedLabel, x + 8, y + 4, 0xFFFFFFFF);
	}

	private static void rebuild(boolean enabled, TextRenderer tr) {
		cachedEnabled = enabled;
		cachedCount = totemCount;
		cachedLabel = Text.literal("AutoTotem ").formatted(Formatting.WHITE)
				.append(Text.literal(enabled ? "ON" : "OFF").formatted(enabled ? Formatting.GREEN : Formatting.RED))
				.append(Text.literal("  x" + totemCount).formatted(Formatting.GRAY));
		cachedWidth = tr.getWidth(cachedLabel);
	}
}
