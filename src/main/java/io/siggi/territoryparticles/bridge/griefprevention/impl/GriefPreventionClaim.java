package io.siggi.territoryparticles.bridge.griefprevention.impl;

import io.siggi.territoryparticles.location.TBlock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Location;

public class GriefPreventionClaim extends io.siggi.territoryparticles.bridge.griefprevention.GriefPreventionClaim {

	private final GriefPreventionBridge bridge;
	Claim claim = null;
	private final long claimId;

	GriefPreventionClaim(GriefPreventionBridge bridge, long claimId) {
		if (bridge == null) {
			throw new NullPointerException();
		}
		this.bridge = bridge;
		this.claimId = claimId;
	}

	Claim getGPClaim() {
		if (claim == null || !claim.inDataStore) {
			claim = null;
			claim = bridge.getGPClaim(claimId);
		}
		return claim;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof GriefPreventionClaim) {
			GriefPreventionClaim o = ((GriefPreventionClaim) other);
			return this.claimId == o.claimId;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (int) (this.claimId ^ (this.claimId >>> 32));
	}

	@Override
	public boolean isValid() {
		return getGPClaim() != null;
	}

	@Override
	public long getId() {
		return claimId;
	}

	@Override
	public UUID getOwner() {
		Claim c = getGPClaim();
		return c == null ? null : c.ownerID;
	}

	@Override
	public GriefPreventionClaim getParent() {
		Claim c = getGPClaim();
		if (c == null) {
			return null;
		}
		return bridge.wrapClaim(c.parent);
	}

	@Override
	public List<GriefPreventionClaim> getChildren() {
		Claim c = getGPClaim();
		if (c == null) {
			return null;
		}
		ArrayList<GriefPreventionClaim> list = new ArrayList<>(claim.children.size());
		for (Claim cc : c.children) {
			list.add(bridge.wrapClaim(cc));
		}
		return Collections.unmodifiableList(list);
	}

	@Override
	public TBlock getLowerBoundary() {
		Claim c = getGPClaim();
		if (c == null) {
			return null;
		}
		Location loc = c.getLesserBoundaryCorner();
		return new TBlock(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	@Override
	public TBlock getUpperBoundary() {
		Claim c = getGPClaim();
		if (c == null) {
			return null;
		}
		Location loc = c.getGreaterBoundaryCorner();
		return new TBlock(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}
}
