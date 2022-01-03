package io.siggi.territoryparticles.bridge.factions;

import org.bukkit.Chunk;

public abstract class FactionLand {

	public abstract Faction getFaction();

	public abstract Chunk getChunk();

	@Override
	public abstract boolean equals(Object other);

	@Override
	public abstract int hashCode();
}
