package io.siggi.territoryparticles.bridge.griefprevention;

import java.lang.reflect.Constructor;
import java.util.Collection;

public abstract class GriefPreventionBridge<B extends GriefPreventionBridge, C extends GriefPreventionClaim> {

	public static GriefPreventionBridge create() {
		try {
			Class.forName("me.ryanhamshire.GriefPrevention.GriefPrevention");
			Class<GriefPreventionBridge> clazz
					= (Class<GriefPreventionBridge>) Class.forName("io.siggi.territoryparticles.bridge.griefprevention.impl.GriefPreventionBridge");
			Constructor<GriefPreventionBridge> constructor = clazz.getDeclaredConstructor();
			return constructor.newInstance();
		} catch (Exception e) {
		}
		return null;
	}
	
	public abstract GriefPreventionClaim getClaim(long id);

	public abstract Collection<? extends GriefPreventionClaim> getAllClaims();

	public abstract GriefPreventionClaim wrapClaim(Object gpClaim);
}
