package io.siggi.territoryparticles;

import static io.siggi.territoryparticles.GFX.spawnParticle;
import io.siggi.territoryparticles.location.TBlock;
import io.siggi.territoryparticles.location.TBlockColumn;
import io.siggi.territoryparticles.location.TChunk;
import io.siggi.territoryparticles.location.TPoint;
import io.siggi.territoryparticles.location.TPointColumn;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.PluginManager;

public class Renderer implements Listener {

	public void init(TerritoryParticles plugin) {
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvents(this, plugin);
		Bukkit.getScheduler().runTaskTimer(plugin, this::runLoop, 1L, 1L);
	}

	private final Set<TPointColumn> columns = new HashSet<>(100000);
	private final Set<TPoint> points = new HashSet<>(1000000);
	private final List<TPoint> pointsArray = new ArrayList<>();
	private final Map<TBlockColumn, Set<TPointColumn>> columnMapping = new HashMap<>(100000);
	private final Map<TPointColumn, Set<TPoint>> pointMapping = new HashMap<>(100000);
	private final Map<TChunk, Set<TPointColumn>> chunkToPointColumn = new HashMap<>(1000);

	private final Set<TPointColumn> columnsToRecalculate = new HashSet<>(1000);
	private long lastRecalculate = 0L;

	private boolean rebuildArray = false;

	public void addColumn(TPointColumn column) {
		columns.add(column);
		for (TBlockColumn bc : column.getBlockColumns()) {
			Set<TPointColumn> mapped = columnMapping.get(bc);
			if (mapped == null) {
				columnMapping.put(bc, mapped = new HashSet<>(10));
			}
			mapped.add(column);
		}
		for (TChunk c : column.getChunks()) {
			Set<TPointColumn> tpc = chunkToPointColumn.get(c);
			if (tpc == null) {
				chunkToPointColumn.put(c, tpc = new HashSet<>(100));
			}
			tpc.add(column);
		}
		recalculateColumn(column);
	}

	public void deleteColumn(TPointColumn column) {
		for (TBlockColumn bc : column.getBlockColumns()) {
			Set<TPointColumn> mapped = columnMapping.get(bc);
			if (mapped != null) {
				mapped.remove(column);
				if (mapped.isEmpty()) {
					columnMapping.remove(bc);
				}
			}
			deletePoints(column);
		}
		for (TChunk c : column.getChunks()) {
			Set<TPointColumn> tpc = chunkToPointColumn.get(c);
			if (tpc != null) {
				tpc.remove(column);
				if (tpc.isEmpty()) {
					chunkToPointColumn.remove(c);
				}
			}
		}
		columns.remove(column);
	}

	private void addPoint(TPointColumn column, TPoint point) {
		Set<TPoint> thePoints = pointMapping.get(column);
		if (thePoints == null) {
			pointMapping.put(column, thePoints = new HashSet<>(10));
		}
		thePoints.add(point);
		points.add(point);
		rebuildArray = true;
	}

	private void deletePoints(TPointColumn column) {
		Set<TPoint> thePoints = pointMapping.remove(column);
		if (thePoints != null) {
			points.removeAll(thePoints);
		}
		rebuildArray = true;
	}

	private void recalculateColumn(TPointColumn column) {
		columnsToRecalculate.add(column);
	}

	private void runLoop() {
		long now = System.currentTimeMillis();
		if (!columnsToRecalculate.isEmpty() && now - lastRecalculate > 10000L) {
			for (TPointColumn column : columnsToRecalculate) {
				if (columns.contains(column)) {
					doRecalculateColumn(column);
				}
			}
			columnsToRecalculate.clear();
			lastRecalculate = now;
		}
		if (rebuildArray) {
			rebuildArray = false;
			pointsArray.clear();
			pointsArray.addAll(points);
		}
		int size = pointsArray.size();
		int numberToDo = (size * 3) / 100;
		if (numberToDo < 1) {
			numberToDo = 1;
		}
		numberToDo = Math.min(numberToDo, size);
		for (int i = 0; i < numberToDo; i++) {
			TPoint point = pointsArray.get((int) Math.floor(Math.random() * size));
			String worldName = point.getWorld();
			World w = Bukkit.getWorld(worldName);
			if (w != null) {
				spawnParticle(w, Particle.FIREWORKS_SPARK, point.getX(), point.getY(), point.getZ(), 2, 0.0, 0.0, 0.0, 0.01);
			}
		}
	}

	private void doRecalculateColumn(TPointColumn column) {
		if (!column.areRequiredChunksLoaded()) {
			return;
		}
		deletePoints(column);
		Set<TBlockColumn> blockColumns = column.getBlockColumns();
		boolean previouslyBlocked = true;
		derp:
		for (int y = 0; y < 256; y++) {
			boolean blocked = false;
			for (TBlockColumn blockColumn : blockColumns) {
				Block bl = blockColumn.getBlock(y).getBukkitBlock();
				if (bl == null) { // should never be null
					break derp;
				}
				if (!isAir(bl.getType())) {
					blocked = true;
					break;
				}
			}
			if (!blocked && previouslyBlocked) {
				addPoint(column, column.getPoint(((double) y) + 0.5));
			}
			previouslyBlocked = blocked;
		}
	}

	private boolean isAir(Material material) {
		switch (material) {
			case AIR:
			case CAVE_AIR:
			case VOID_AIR:
				return true;
			default:
				return false;
		}
	}

	private void updateForBlock(Block block) {
		TBlock tb = new TBlock(block);
		Set<TPointColumn> tpc = columnMapping.get(tb.getColumn());
		if (tpc != null) {
			for (TPointColumn column : tpc) {
				recalculateColumn(column);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void loadChunk(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		TChunk tc = new TChunk(chunk);
		Set<TPointColumn> tpc = chunkToPointColumn.get(tc);
		if (tpc != null) {
			for (TPointColumn column : tpc) {
				recalculateColumn(column);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void unloadChunk(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		TChunk tc = new TChunk(chunk);
		Set<TPointColumn> tpc = chunkToPointColumn.get(tc);
		if (tpc != null) {
			for (TPointColumn column : tpc) {
				deletePoints(column);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockPlace(BlockPlaceEvent event) {
		updateForBlock(event.getBlock());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockBreak(BlockBreakEvent event) {
		updateForBlock(event.getBlock());
	}
}
