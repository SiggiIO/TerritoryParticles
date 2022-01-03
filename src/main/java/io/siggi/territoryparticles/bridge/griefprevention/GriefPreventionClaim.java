package io.siggi.territoryparticles.bridge.griefprevention;

import io.siggi.territoryparticles.location.TBlock;
import java.util.List;
import java.util.UUID;

public abstract class GriefPreventionClaim {
	
	public abstract boolean isValid();
	
	public abstract long getId();

	public abstract UUID getOwner();
	
	public abstract GriefPreventionClaim getParent();
	
	public abstract List<? extends GriefPreventionClaim> getChildren();

	public abstract TBlock getLowerBoundary();

	public abstract TBlock getUpperBoundary();
}
