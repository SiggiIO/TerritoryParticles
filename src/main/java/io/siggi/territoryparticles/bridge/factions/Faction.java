package io.siggi.territoryparticles.bridge.factions;

public abstract class Faction {
	
	public abstract String getId();

	@Override
	public abstract boolean equals(Object other);

	@Override
	public abstract int hashCode();
}
