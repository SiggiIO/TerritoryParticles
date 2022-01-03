package io.siggi.territoryparticles.bridge.factions.v1695;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

public class FactionLand extends io.siggi.territoryparticles.bridge.factions.FactionLand {

	private final FLocation location;
	FactionLand(FLocation location) {
		if (location == null) throw new NullPointerException();
		this.location = location;
	}

	@Override
	public Faction getFaction() {
		return new Faction(Board.getInstance().getFactionAt(location));
	}

	@Override
	public Chunk getChunk() {
		try {
			Bukkit.getServer().getWorld(location.getWorldName()).getChunkAt((int) location.getX(), (int) location.getZ());
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof FactionLand) {
			return equals((FactionLand) other);
		}
		return false;
	}

	public boolean equals(FactionLand other) {
		return location.getX() == other.location.getX() && location.getZ() == other.location.getZ();
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + (int) location.getX();
		hash = 97 * hash + (int) location.getZ();
		return hash;
	}
}
