package io.siggi.territoryparticles.location;

public final class TBlockColumn {

	private final String world;
	private final int x, z;

	public TBlockColumn(String world, int x, int z) {
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

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}
	
	public TBlock getBlock(int y){
		return new TBlock(world, x, y, z);
	}

	public TChunk getChunk() {
		return new TChunk(this);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof TBlockColumn) {
			TBlockColumn o = (TBlockColumn) other;
			return o.world.equals(world) && o.x == x && o.z == z;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 73 * hash + this.world.hashCode();
		hash = 73 * hash + this.x;
		hash = 73 * hash + this.z;
		return hash;
	}
}
