package io.siggi.territoryparticles.bridge.factions.v1695;

import java.util.Objects;

public class Faction extends io.siggi.territoryparticles.bridge.factions.Faction {

	private final com.massivecraft.factions.Faction faction;

	Faction(com.massivecraft.factions.Faction faction) {
		if (faction == null) throw new NullPointerException();
		this.faction = faction;
	}
	
	@Override
	public String getId() {
		if (faction.isNone()) {
			return "NONE";
		}
		if (faction.isSafeZone()) {
			return "SAFEZONE";
		}
		if (faction.isWarZone()) {
			return "WARZONE";
		}
		return faction.getId();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Faction) {
			return equals((Faction) other);
		}
		return false;
	}

	public boolean equals(Faction other) {
		return getId().equals(other.getId());
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 83 * hash + Objects.hashCode(getId());
		return hash;
	}
}
