package io.siggi.territoryparticles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;

public class Settings {

	private static final Map<UUID, Settings> settingsMap = new HashMap<>();

	public static Settings getSettings(UUID player) {
		Settings settings = settingsMap.get(player);
		if (settings == null) {
			gcSettings();
			settingsMap.put(player, settings = new Settings(player));
		}
		return settings;
	}

	static void gcSettings() {
		for (Iterator<Map.Entry<UUID, Settings>> it = settingsMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<UUID, Settings> entry = it.next();
			if (Bukkit.getPlayer(entry.getKey()) == null) {
				entry.getValue().save();
				it.remove();
			}
		}
	}

	private final UUID player;

	private Settings(UUID player) {
		this.player = player;
		load();
	}

	private File file;

	private File getFile() {
		if (file == null) {
			File parent = new File(TerritoryParticles.getInstance().getDataFolder(), "userdata");
			if (!parent.exists()) {
				parent.mkdirs();
			}
			file = new File(parent, player.toString().replace("-", "").toLowerCase() + ".txt");
		}
		return file;
	}

	private Mode mode = Mode.NORMAL;

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	private void load() {
		try (BufferedReader reader = new BufferedReader(new FileReader(getFile()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				int eqPos = line.indexOf("=");
				if (eqPos == -1) {
					continue;
				}
				String key = line.substring(0, eqPos);
				String val = line.substring(eqPos + 1);
				if (key.equals("mode")) {
					mode = Mode.fromString(val);
				}
			}
		} catch (Exception e) {
		}
	}

	private void save() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(getFile()))) {
			writer.write("mode=" + mode.toString());
			writer.newLine();
		} catch (Exception e) {
		}
	}

	public static enum Mode {

		OFF, LOW, NORMAL, HIGH, ULTRA;

		public static Mode fromString(String str) {
			try {
				str = str.toUpperCase();
				Mode v = valueOf(str);
				if (v != null) {
					return v;
				}
			} catch (Exception e) {
			}
			return NORMAL;
		}
	}
}
