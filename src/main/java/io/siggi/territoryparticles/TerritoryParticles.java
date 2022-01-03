package io.siggi.territoryparticles;

import io.siggi.territoryparticles.bridge.factions.FactionsBridge;
import io.siggi.territoryparticles.bridge.griefprevention.GriefPreventionBridge;
import io.siggi.territoryparticles.handlers.FactionsHandler;
import io.siggi.territoryparticles.handlers.GriefPreventionHandler;
import io.siggi.territoryparticles.handlers.Handler;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class TerritoryParticles extends JavaPlugin {

	private final Renderer renderer = new Renderer();
	private Handler handler;

	public Renderer getRenderer() {
		return renderer;
	}

	private static TerritoryParticles instance = null;

	public static TerritoryParticles getInstance() {
		return instance;
	}

	@Override
	public void onLoad() {
		instance = this;
	}

	@Override
	public void onEnable() {
		PluginCommand c = getCommand("territoryparticles");
		TerritoryParticlesCommand territoryParticlesCommand = new TerritoryParticlesCommand(this);
		c.setExecutor(territoryParticlesCommand);
		c.setTabCompleter(territoryParticlesCommand);
		renderer.init(this);
		init:
		{
			FactionsBridge<?, ?, ?> factionsBridge = FactionsBridge.create();
			if (factionsBridge != null) {
				handler = new FactionsHandler(this, factionsBridge);
				break init;
			}
			GriefPreventionBridge<?, ?> griefPreventionBridge = GriefPreventionBridge.create();
			if (griefPreventionBridge != null) {
				handler = new GriefPreventionHandler(this, griefPreventionBridge);
				break init;
			}
			System.err.println("Error: No supported territory plugin found.");
			setEnabled(false);
			return;
		}
		handler.init();
	}

	@Override
	public void onDisable() {
		Settings.gcSettings();
	}
}
