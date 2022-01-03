package io.siggi.territoryparticles;

import io.siggi.territoryparticles.Settings.Mode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class GFX {

	private GFX() {
	}

	private static final double maxDistanceUltra = 64.0;
	private static final double maxDistanceSquaredUltra = maxDistanceUltra * maxDistanceUltra;
	private static final double maxDistanceHigh = 48.0;
	private static final double maxDistanceSquaredHigh = maxDistanceHigh * maxDistanceHigh;
	private static final double maxDistance = 24.0;
	private static final double maxDistanceSquared = maxDistance * maxDistance;

	public static void spawnParticle(
			World w, Particle particle,
			double x, double y, double z,
			int count,
			double offsetX, double offsetY, double offsetZ,
			double acceleration) {
		int chanceNumber = (int) Math.floor(Math.random() * 100);
		for (Player p : w.getPlayers()) {
			double mds;
			Mode mode = Settings.getSettings(p.getUniqueId()).getMode();
			switch (mode) {
				case OFF:
					continue;
				case LOW:
					if (chanceNumber < 80) {
						continue;
					}
					mds = maxDistanceSquared;
					break;
				case NORMAL:
					mds = maxDistanceSquared;
					break;
				case HIGH:
					mds = maxDistanceSquaredHigh;
					break;
				case ULTRA:
					mds = maxDistanceSquaredUltra;
					break;
				default:
					throw new RuntimeException("Unreachable");
			}
			Location playerLoc = p.getLocation();
			double diffX = playerLoc.getX() - x;
			double diffY = playerLoc.getY() - y;
			double diffZ = playerLoc.getZ() - z;
			double distanceSquared = (diffX * diffX) + (diffY * diffY) + (diffZ * diffZ);
			if (distanceSquared < mds) {
				p.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, acceleration);
			}
		}
	}
}
