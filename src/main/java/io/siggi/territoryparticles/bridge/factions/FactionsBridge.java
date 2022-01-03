package io.siggi.territoryparticles.bridge.factions;

import io.siggi.territoryparticles.location.TChunk;
import java.lang.reflect.Constructor;
import org.bukkit.Chunk;

public abstract class FactionsBridge<B extends FactionsBridge,F extends Faction, L extends FactionLand> {

	public static FactionsBridge create() {
		try {
			Class.forName("com.massivecraft.factions.FactionsPlugin");
			Class<FactionsBridge> clazz = (Class<FactionsBridge>) Class.forName("io.siggi.territoryparticles.bridge.factions.v1695.FactionsBridge");
			Constructor<FactionsBridge> constructor = clazz.getDeclaredConstructor();
			return constructor.newInstance();
		} catch (Exception e) {
		}
		return null;
	}

	public abstract F getFactionById(int id);

	public abstract F getFactionByTag(String tag);
	
	public abstract L getFactionLand(Chunk chunk);
	
	public abstract L getFactionLand(TChunk chunk);
	
	public abstract F getFactionLandOwner(Chunk chunk);
	
	public abstract F getFactionLandOwner(TChunk chunk);
	
	public abstract F wrapFaction(Object factionObject);
	
	public abstract L wrapFactionLand(Object factionLocationObject);
}
