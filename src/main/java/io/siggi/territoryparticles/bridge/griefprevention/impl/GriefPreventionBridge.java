package io.siggi.territoryparticles.bridge.griefprevention.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class GriefPreventionBridge extends io.siggi.territoryparticles.bridge.griefprevention.GriefPreventionBridge<GriefPreventionBridge, GriefPreventionClaim> {

	public GriefPreventionBridge() {
	}

	Claim getGPClaim(long id) {
		return GriefPrevention.instance.dataStore.getClaim(id);
	}

	@Override
	public GriefPreventionClaim getClaim(long id) {
		return wrapClaim(getGPClaim(id));
	}

	@Override
	public Collection<GriefPreventionClaim> getAllClaims() {
		Collection<Claim> claims = GriefPrevention.instance.dataStore.getClaims();
		Set<GriefPreventionClaim> c = new HashSet<>();
		for (Claim claim : claims) {
			GriefPreventionClaim wc = wrapClaim(claim);
			if (wc != null) {
				c.add(wc);
			}
		}
		return Collections.unmodifiableSet(c);
	}

	private final Map<Long, GriefPreventionClaim> wrapMap = new HashMap<>();

	@Override
	public GriefPreventionClaim wrapClaim(Object claimObject) {
		if (!(claimObject instanceof Claim))return null;
		Claim claim = (Claim) claimObject;
		GriefPreventionClaim result = wrapMap.get(claim.getID());
		if (result == null) {
			cleanClaims();
			wrapMap.put(claim.getID(), result = new GriefPreventionClaim(this, claim.getID()));
		}
		result.claim = claim;
		return result;
	}

	private long lastClean = 0L;

	private void cleanClaims() {
		long now = System.currentTimeMillis();
		if (now - lastClean < 60000L) {
			return;
		}
		lastClean = now;
		for (Iterator<Map.Entry<Long, GriefPreventionClaim>> it = wrapMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<Long, GriefPreventionClaim> entry = it.next();
			if (!entry.getValue().isValid()) {
				it.remove();
			}
		}
	}
}
