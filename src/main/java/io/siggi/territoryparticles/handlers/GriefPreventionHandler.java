package io.siggi.territoryparticles.handlers;

import io.siggi.territoryparticles.TerritoryParticles;
import io.siggi.territoryparticles.bridge.griefprevention.GriefPreventionBridge;
import io.siggi.territoryparticles.bridge.griefprevention.GriefPreventionClaim;
import io.siggi.territoryparticles.location.TBlock;
import io.siggi.territoryparticles.location.TChunk;
import io.siggi.territoryparticles.location.TPointColumn;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import me.ryanhamshire.GriefPrevention.events.ClaimCreatedEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import me.ryanhamshire.GriefPrevention.events.ClaimModifiedEvent;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class GriefPreventionHandler implements Handler, Listener {
	
	private final TerritoryParticles plugin;
	private final GriefPreventionBridge<?, ?> griefPrevention;
	
	private final Map<TChunk, Set<TPointColumn>> chunksByPoint = new HashMap<>(1000);
	private final Map<TPointColumn, Set<GriefPreventionClaim>> claimsByPoint = new HashMap<>(100000);
	private final Map<GriefPreventionClaim, Set<TPointColumn>> pointsByClaim = new HashMap<>(1000);
	
	public GriefPreventionHandler(TerritoryParticles plugin, GriefPreventionBridge<?, ?> griefPrevention) {
		this.plugin = plugin;
		this.griefPrevention = griefPrevention;
	}
	
	@Override
	public void init() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		Collection<? extends GriefPreventionClaim> allClaims = griefPrevention.getAllClaims();
		for (GriefPreventionClaim claim : allClaims) {
			recursiveUpdate(claim);
		}
	}
	
	private void recursiveUpdate(GriefPreventionClaim claim) {
		updateClaim(claim);
		for (GriefPreventionClaim child : claim.getChildren()) {
			recursiveUpdate(child);
		}
	}
	
	private void announce(String msg) {
		System.err.println(msg);
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			p.sendMessage(msg);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void create(ClaimCreatedEvent event) {
		GriefPreventionClaim claim = griefPrevention.wrapClaim(event.getClaim());
		updateClaim(claim);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void modify(ClaimModifiedEvent event) {
		GriefPreventionClaim claim = griefPrevention.wrapClaim(event.getClaim());
		updateClaim(claim);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void delete(ClaimDeletedEvent event) {
		GriefPreventionClaim claim = griefPrevention.wrapClaim(event.getClaim());
		removeParticles(claim);
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void chunkLoaded(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		TChunk tchunk = new TChunk(chunk);
		Set<TPointColumn> tpc = chunksByPoint.get(tchunk);
		if (tpc == null) {
			return;
		}
		for (TPointColumn column : tpc) {
			plugin.getRenderer().addColumn(column);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void chunkUnloaded(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		TChunk tchunk = new TChunk(chunk);
		Set<TPointColumn> tpc = chunksByPoint.get(tchunk);
		if (tpc == null) {
			return;
		}
		for (TPointColumn column : tpc) {
			plugin.getRenderer().deleteColumn(column);
		}
	}
	
	private void updateClaim(GriefPreventionClaim claim) {
		removeParticles(claim);
		TBlock lowerBoundary = claim.getLowerBoundary();
		TBlock upperBoundary = claim.getUpperBoundary();
		double minX = (double) (lowerBoundary.getX());
		double maxX = (double) (upperBoundary.getX() + 1);
		double minZ = (double) (lowerBoundary.getZ());
		double maxZ = (double) (upperBoundary.getZ() + 1);
		for (double x = minX; x <= maxX; x += 0.5) {
			addColumn(claim, new TPointColumn(lowerBoundary.getWorld(), x, minZ));
			addColumn(claim, new TPointColumn(lowerBoundary.getWorld(), x, maxZ));
		}
		for (double z = minZ; z <= maxZ; z += 0.5) {
			addColumn(claim, new TPointColumn(lowerBoundary.getWorld(), minX, z));
			addColumn(claim, new TPointColumn(lowerBoundary.getWorld(), maxX, z));
		}
	}
	
	private void removeParticles(GriefPreventionClaim claim) {
		Set<TPointColumn> tpc = pointsByClaim.get(claim);
		if (tpc == null) {
			return;
		}
		TPointColumn[] points = tpc.toArray(new TPointColumn[tpc.size()]);
		for (TPointColumn point : points) {
			removeColumn(claim, point);
		}
	}
	
	private void addColumn(GriefPreventionClaim claim, TPointColumn column) {
		Set<TPointColumn> tpc = pointsByClaim.get(claim);
		if (tpc == null) {
			pointsByClaim.put(claim, tpc = new HashSet<>());
		}
		tpc.add(column);
		Set<GriefPreventionClaim> gpc = claimsByPoint.get(column);
		if (gpc == null) {
			claimsByPoint.put(column, gpc = new HashSet<>());
		}
		gpc.add(claim);
		TChunk chunk = column.getChunk();
		Set<TPointColumn> tpcC = chunksByPoint.get(chunk);
		if (tpcC == null) {
			chunksByPoint.put(chunk, tpcC = new HashSet<>());
		}
		tpcC.add(column);
		if (chunk.isLoaded()) {
			plugin.getRenderer().addColumn(column);
		}
	}
	
	private void removeColumn(GriefPreventionClaim claim, TPointColumn column) {
		Set<TPointColumn> tpc = pointsByClaim.get(claim);
		if (tpc != null) {
			tpc.remove(column);
			if (tpc.isEmpty()) {
				pointsByClaim.remove(claim);
			}
		}
		Set<GriefPreventionClaim> gpc = claimsByPoint.get(column);
		if (gpc != null) {
			gpc.remove(claim);
			if (gpc.isEmpty()) {
				claimsByPoint.remove(column);
			}
		}
		TChunk chunk = column.getChunk();
		Set<TPointColumn> tpcC = chunksByPoint.get(chunk);
		if (tpcC != null) {
			tpcC.remove(column);
			if (tpcC.isEmpty()) {
				chunksByPoint.remove(chunk);
			}
		}
		if (chunk.isLoaded()) {
			plugin.getRenderer().deleteColumn(column);
		}
	}
}
