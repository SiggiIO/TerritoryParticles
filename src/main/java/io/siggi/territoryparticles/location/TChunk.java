package io.siggi.territoryparticles.location;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class TChunk {

	private final String world;
	private final int x, z;

	public TChunk(String world, int x, int z) {
		if (world == null) {
			throw new NullPointerException("world cannot be null");
		}
		this.world = world;
		this.x = x;
		this.z = z;
	}
	
	public TChunk(Chunk chunk) {
		this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
	}
	
	public TChunk(Block block) {
		this(block.getWorld().getName(), block.getX() >> 4, block.getZ() >> 4);
	}
	
	public TChunk(TBlock block) {
		this(block.getWorld(), block.getX() >> 4, block.getZ() >> 4);
	}
	
	public TChunk(TBlockColumn blockColumn) {
		this(blockColumn.getWorld(), blockColumn.getX() >> 4, blockColumn.getZ() >> 4);
	}

	public boolean isLoaded() {
		try {
			World w = Bukkit.getWorld(world);
			if (w == null) {
				return false;
			}
			return w.isChunkLoaded(x, z);
		} catch (Exception e) {
		}
		return false;
	}

	public Chunk getBukkitChunk() {
		try {
			World w = Bukkit.getWorld(world);
			if (w == null) {
				return null;
			}
			if (w.isChunkLoaded(x, z)) {
				return w.getChunkAt(x, z);
			} else {
				return null;
			}
		} catch (Exception e) {
		}
		return null;
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

	@Override
	public boolean equals(Object other) {
		if (other instanceof TChunk) {
			TChunk o = (TChunk) other;
			return o.world.equals(world) && o.x == x && o.z == z;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash + this.world.hashCode();
		hash = 23 * hash + this.x;
		hash = 23 * hash + this.z;
		return hash;
	}
}
