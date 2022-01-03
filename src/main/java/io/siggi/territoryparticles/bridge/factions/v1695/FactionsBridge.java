package io.siggi.territoryparticles.bridge.factions.v1695;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FactionsPlugin;
import io.siggi.territoryparticles.location.TChunk;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

public class FactionsBridge extends io.siggi.territoryparticles.bridge.factions.FactionsBridge<FactionsBridge,Faction,FactionLand> {

	private final FactionsPlugin factionsPlugin;

	public FactionsBridge() {
		factionsPlugin = (FactionsPlugin) Bukkit.getServer().getPluginManager().getPlugin("Factions");
	}

	@Override
	public Faction getFactionById(int id) {
		return new Faction(com.massivecraft.factions.Factions.getInstance().getFactionById(Integer.toString(id)));
	}

	@Override
	public Faction getFactionByTag(String tag) {
		return new Faction(com.massivecraft.factions.Factions.getInstance().getByTag(tag));
	}

	@Override
	public FactionLand getFactionLand(Chunk chunk) {
		return new FactionLand(new FLocation(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
	}

	@Override
	public FactionLand getFactionLand(TChunk chunk) {
		return new FactionLand(new FLocation(chunk.getWorld(), chunk.getX(), chunk.getZ()));
	}

	@Override
	public Faction getFactionLandOwner(Chunk chunk) {
		return new Faction(Board.getInstance().getFactionAt(new FLocation(chunk.getWorld().getName(), chunk.getX(), chunk.getZ())));
	}

	@Override
	public Faction getFactionLandOwner(TChunk chunk) {
		return new Faction(Board.getInstance().getFactionAt(new FLocation(chunk.getWorld(), chunk.getX(), chunk.getZ())));
	}
	
	@Override
	public Faction wrapFaction(Object factionObject){return new Faction((com.massivecraft.factions.Faction) factionObject);}
	
	@Override
	public FactionLand wrapFactionLand(Object factionLocationObject){return new FactionLand((FLocation) factionLocationObject);}
}
