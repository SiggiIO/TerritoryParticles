package io.siggi.territoryparticles;

import io.siggi.territoryparticles.Settings.Mode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class TerritoryParticlesCommand implements CommandExecutor, TabExecutor {

	private final TerritoryParticles plugin;

	public TerritoryParticlesCommand(TerritoryParticles plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Only a player can use this command!");
			return true;
		}
		Player p = (Player) sender;
		try {
			Settings settings = Settings.getSettings(p.getUniqueId());
			Mode newMode = Settings.Mode.fromString(args[0]);
			settings.setMode(newMode);
			sender.sendMessage(ChatColor.GOLD + "Set TerritoryParticles to " + ChatColor.AQUA + (newMode.toString().toLowerCase()));
			return true;
		} catch (Exception e) {
		}
		sender.sendMessage(ChatColor.GOLD + "Usage: /territoryparticles [off|low|normal|high|ultra]");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> result = new ArrayList<>();
		Consumer<String> add = (suggestion) -> {
			String input = args[args.length - 1];
			if (suggestion.startsWith(input)) {
				result.add(suggestion);
			}
		};
		if (args.length == 1) {
			add.accept("off");
			add.accept("low");
			add.accept("normal");
			add.accept("high");
			add.accept("ultra");
		}
		return result;
	}

}
