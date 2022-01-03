package io.siggi.territoryparticles.location;

import java.util.HashSet;
import java.util.Set;

public final class TPointColumn {

	private final String world;
	private final double x, z;

	public TPointColumn(String world, double x, double z) {
		if (world == null) {
			throw new NullPointerException("world cannot be null");
		}
		this.world = world;
		this.x = x;
		this.z = z;
	}

	public String getWorld() {
		return world;
	}

	public double getX() {
		return x;
	}

	public double getZ() {
		return z;
	}

	public TPoint getPoint(double y) {
		return new TPoint(world, x, y, z);
	}
	
	public TChunk getChunk(){
		return new TChunk(getBlockColumn());
	}
	
	public TBlockColumn getBlockColumn(){
		return new TBlockColumn(world, (int) Math.floor(x), (int) Math.floor(z));
	}

	public Set<TBlockColumn> getBlockColumns() {
		HashSet<TBlockColumn> columns = new HashSet<>();
		double xx = Math.floor(x);
		double zz = Math.floor(z);
		columns.add(new TBlockColumn(world, ((int) xx), ((int) zz)));
		if (xx == x) {
			columns.add(new TBlockColumn(world, ((int) xx) - 1, ((int) zz)));
		}
		if (zz == z) {
			columns.add(new TBlockColumn(world, ((int) xx), ((int) zz) - 1));

		}
		if (xx == x && zz == z) {
			columns.add(new TBlockColumn(world, ((int) xx) - 1, ((int) zz) - 1));
		}
		return columns;
	}

	public Set<TChunk> getChunks() {
		HashSet<TChunk> chunks = new HashSet<>();
		for (TBlockColumn column : getBlockColumns()) {
			chunks.add(column.getChunk());
		}
		return chunks;
	}

	public boolean areRequiredChunksLoaded() {
		for (TChunk chunk : getChunks()) {
			if (!chunk.isLoaded()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof TPointColumn) {
			TPointColumn o = (TPointColumn) other;
			return o.world.equals(world) && o.x == x && o.z == z;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 97 * hash + this.world.hashCode();
		hash = 97 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
		hash = 97 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
		return hash;
	}
}
