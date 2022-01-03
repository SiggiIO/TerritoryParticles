package io.siggi.territoryparticles.location;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;

public final class TBlock {

	private final String world;
	private final int x, y, z;

	public TBlock(String world, int x, int y, int z) {
		if (world == null) {
			throw new NullPointerException("world cannot be null");
		}
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public TBlock(Block block) {
		this(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
	}

	public String getWorld() {
		return world;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}
	
	public TBlockColumn getColumn(){return new TBlockColumn(world, x, z);}

	public TChunk getChunk() {
		return new TChunk(this);
	}

	public Block getBukkitBlock() {
		if (!getChunk().isLoaded()) {
			return null;
		}
		return Bukkit.getWorld(world).getBlockAt(x, y, z);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof TBlock) {
			TBlock o = (TBlock) other;
			return o.world.equals(world) && o.x == x && o.y == y && o.z == z;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + this.world.hashCode();
		hash = 37 * hash + this.x;
		hash = 37 * hash + this.y;
		hash = 37 * hash + this.z;
		return hash;
	}
}
