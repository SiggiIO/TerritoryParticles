package io.siggi.territoryparticles.location;

public final class TPoint {

	private final String world;
	private final double x, y, z;

	public TPoint(String world, double x, double y, double z) {
		if (world == null) {
			throw new NullPointerException("world cannot be null");
		}
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public String getWorld() {
		return world;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}
	
	public TPointColumn getColumn(){return new TPointColumn(world, x, z);}

	@Override
	public boolean equals(Object other) {
		if (other instanceof TPoint) {
			TPoint o = (TPoint) other;
			return o.world.equals(world) && o.x == x && o.y == y && o.z == z;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 67 * hash + this.world.hashCode();
		hash = 67 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
		hash = 67 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
		hash = 67 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
		return hash;
	}
}
