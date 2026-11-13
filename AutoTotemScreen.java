package com.example.autototem;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/**
 * AutoTotem ayar menusu.
 * Kucuk ekranlarda (Pojav, yuksek GUI olcegi) da sigsin diye kompakt: 5 satir, 2 sutunlu.
 */
public class AutoTotemScreen extends Screen {
	private static final int PANEL_W = 268;
	private static final int HEADER_H = 32;
	private static final int PAD = 12;
	private static final int ROW_H = 24;
	private static final int BTN_H = 20;
	private static final int FOOTER_H = 20;
	private static final int ROWS = 5;

	private static final int ACCENT_ON = 0xFF3DDC84;
	private static final int ACCENT_OFF = 0xFFE5484D;

	private static final String[] CORNERS = {"Sol Üst", "Sağ Üst", "Sol Alt", "Sağ Alt"};

	private static final Text STATUS_ON = Text.literal("AÇIK").formatted(Formatting.GREEN);
	private static final Text STATUS_OFF = Text.literal("KAPALI").formatted(Formatting.RED);
	private static final Text HINT = Text.literal("1t = 50 ms  •  Esc veya kısayol tuşu ile kapat").formatted(Formatting.GRAY);

	private final Screen parent;
	private int px;
	private int py;
	private int ph;

	public AutoTotemScreen(Screen parent) {
		super(Text.literal("AutoTotem"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		AutoTotemConfig cfg = AutoTotemConfig.INSTANCE;

		ph = HEADER_H + 8 + ROWS * ROW_H + FOOTER_H;
		px = (this.width - PANEL_W) / 2;
		py = Math.max(4, (this.height - ph) / 2);

		int x = px + PAD;
		int fullW = PANEL_W - PAD * 2;
		int halfW = (fullW - 6) / 2;
		int y = py + HEADER_H + 8;

		// Satir 1: AutoTotem ac/kapat
		ButtonWidget toggleButton = ButtonWidget.builder(toggleText(cfg.enabled), b -> {
			cfg.enabled = !cfg.enabled;
			b.setMessage(toggleText(cfg.enabled));
		}).dimensions(x, y, fullW, BTN_H).build();
		this.addDrawableChild(toggleButton);
		y += ROW_H;

		// Satir 2: min / max gecikme
		this.addDrawableChild(new IntSlider(x, y, halfW, BTN_H, 0, 20, cfg.minDelay,
				v -> "Min gecikme: " + v + "t", v -> cfg.minDelay = v));
		this.addDrawableChild(new IntSlider(x + halfW + 6, y, halfW, BTN_H, 0, 40, cfg.maxDelay,
				v -> "Max gecikme: " + v + "t", v -> cfg.maxDelay = v));
		y += ROW_H;

		// Satir 3: can esigi
		this.addDrawableChild(new IntSlider(x, y, fullW, BTN_H, 1, 10, cfg.healthHearts,
				v -> v >= 10 ? "Can eşiği: Her zaman" : "Can eşiği: " + v + " kalp altı",
				v -> cfg.healthHearts = v));
		y += ROW_H;

		// Satir 4: HUD ac/kapat + konum
		ButtonWidget cornerButton = ButtonWidget.builder(cornerText(cfg.hudCorner), b -> {
			cfg.hudCorner = (cfg.hudCorner + 1) % 4;
			b.setMessage(cornerText(cfg.hudCorner));
		}).dimensions(x + halfW + 6, y, halfW, BTN_H).build();
		cornerButton.active = cfg.showHud;

		ButtonWidget hudButton = ButtonWidget.builder(hudText(cfg.showHud), b -> {
			cfg.showHud = !cfg.showHud;
			b.setMessage(hudText(cfg.showHud));
			cornerButton.active = cfg.showHud;
		}).dimensions(x, y, halfW, BTN_H).build();

		this.addDrawableChild(hudButton);
		this.addDrawableChild(cornerButton);
		y += ROW_H;

		// Satir 5: kapat
		this.addDrawableChild(ButtonWidget.builder(Text.literal("Kapat"), b -> this.close())
				.dimensions(x, y, fullW, BTN_H).build());
	}

	// Vanilla blur yerine kendi karartmamizi render()'da ciziyoruz (cift blur hatasini onler)
	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		AutoTotemConfig cfg = AutoTotemConfig.INSTANCE;
		int accent = cfg.enabled ? ACCENT_ON : ACCENT_OFF;

		// Arka plan karartma
		context.fill(0, 0, this.width, this.height, 0x99000000);

		// Panel (cerceve + govde)
		context.fill(px - 1, py - 1, px + PANEL_W + 1, py + ph + 1, 0xFF2B313B);
		context.fill(px, py, px + PANEL_W, py + ph, 0xF0161A20);

		// Baslik seridi
		context.fill(px, py, px + PANEL_W, py + HEADER_H, 0xFF1D222B);
		context.fill(px, py + HEADER_H - 1, px + PANEL_W, py + HEADER_H, accent);
		context.fill(px, py, px + 3, py + HEADER_H - 1, accent);
		context.drawTextWithShadow(this.textRenderer, this.title, px + PAD, py + 12, 0xFFFFFFFF);

		Text status = cfg.enabled ? STATUS_ON : STATUS_OFF;
		int statusW = this.textRenderer.getWidth(status);
		context.drawTextWithShadow(this.textRenderer, status, px + PANEL_W - PAD - statusW, py + 12, 0xFFFFFFFF);

		// Widget'lar
		super.render(context, mouseX, mouseY, delta);

		// Alt ipucu
		context.drawCenteredTextWithShadow(this.textRenderer, HINT, px + PANEL_W / 2, py + ph - 14, 0xFFAAAAAA);
	}

	/** Ayni tusa tekrar basinca menu kapansin. */
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (AutoTotemClient.guiKey != null && AutoTotemClient.guiKey.matchesKey(keyCode, scanCode)) {
			this.close();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		if (this.client != null) {
			this.client.setScreen(this.parent);
		}
	}

	@Override
	public void removed() {
		AutoTotemConfig.save();
	}

	private static Text toggleText(boolean enabled) {
		return Text.literal("AutoTotem: ")
				.append(enabled ? STATUS_ON : STATUS_OFF);
	}

	private static Text hudText(boolean show) {
		return Text.literal("HUD: ")
				.append(show ? STATUS_ON : STATUS_OFF);
	}

	private static Text cornerText(int corner) {
		return Text.literal("Konum: " + CORNERS[corner]);
	}

	/** Tam sayi kaydiricisi (basamakli). */
	private static final class IntSlider extends SliderWidget {
		private final int min;
		private final int max;
		private final IntFunction<String> format;
		private final IntConsumer onChange;

		IntSlider(int x, int y, int width, int height, int min, int max, int current,
				  IntFunction<String> format, IntConsumer onChange) {
			super(x, y, width, height, Text.empty(), (current - min) / (double) (max - min));
			this.min = min;
			this.max = max;
			this.format = format;
			this.onChange = onChange;
			this.updateMessage();
		}

		private int currentInt() {
			return this.min + (int) Math.round(this.value * (this.max - this.min));
		}

		@Override
		protected void updateMessage() {
			this.setMessage(Text.literal(this.format.apply(this.currentInt())));
		}

		@Override
		protected void applyValue() {
			int v = this.currentInt();
			// Basamaga oturt
			this.value = (v - this.min) / (double) (this.max - this.min);
			this.onChange.accept(v);
		}
	}
}
