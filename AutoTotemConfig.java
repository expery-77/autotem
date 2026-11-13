package com.example.autototem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Ayarlar. config/autototem.json dosyasina kaydedilir. */
public final class AutoTotemConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger("autototem");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("autototem.json");

	public static AutoTotemConfig INSTANCE = new AutoTotemConfig();

	/** AutoTotem acik mi. */
	public boolean enabled = true;
	/** Totem almadan once en az bekleme (tick, 20 tick = 1 sn). */
	public int minDelay = 2;
	/** Totem almadan once en fazla bekleme (tick). */
	public int maxDelay = 5;
	/** Can bu kalp sayisinin altindaysa calisir. 10 = her zaman. */
	public int healthHearts = 10;
	/** Ekranda ON/OFF gostergesi. */
	public boolean showHud = true;
	/** 0 = sol ust, 1 = sag ust, 2 = sol alt, 3 = sag alt. */
	public int hudCorner = 0;

	public static void load() {
		try {
			if (Files.exists(PATH)) {
				String json = Files.readString(PATH, StandardCharsets.UTF_8);
				AutoTotemConfig loaded = GSON.fromJson(json, AutoTotemConfig.class);
				if (loaded != null) {
					loaded.sanitize();
					INSTANCE = loaded;
				}
			}
		} catch (Exception e) {
			LOGGER.warn("AutoTotem ayarlari okunamadi, varsayilanlar kullaniliyor", e);
		}
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			Files.writeString(PATH, GSON.toJson(INSTANCE), StandardCharsets.UTF_8);
		} catch (IOException e) {
			LOGGER.warn("AutoTotem ayarlari kaydedilemedi", e);
		}
	}

	private void sanitize() {
		minDelay = clamp(minDelay, 0, 20);
		maxDelay = clamp(maxDelay, 0, 40);
		healthHearts = clamp(healthHearts, 1, 10);
		hudCorner = ((hudCorner % 4) + 4) % 4;
	}

	private static int clamp(int v, int lo, int hi) {
		return Math.max(lo, Math.min(hi, v));
	}
}
