package io.siggi.territoryparticles.handlers;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.event.LandClaimEvent;
import com.massivecraft.factions.event.LandUnclaimAllEvent;
import com.massivecraft.factions.event.LandUnclaimEvent;
import io.siggi.territoryparticles.TerritoryParticles;
import io.siggi.territoryparticles.bridge.factions.Faction;
import io.siggi.territoryparticles.bridge.factions.FactionsBridge;
import io.siggi.territoryparticles.location.TChunk;
import io.siggi.territoryparticles.location.TPointColumn;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class FactionsHandler implements Handler, Listener {

	private final TerritoryParticles plugin;
	private final FactionsBridge<?, ?, ?> factions;

	public FactionsHandler(TerritoryParticles plugin, FactionsBridge<?, ?, ?> factions) {
		this.plugin = plugin;
		this.factions = factions;
	}

	@Override
	public void init() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		Bukkit.getScheduler().runTaskTimer(plugin, this::runLoop, 1L, 1L);
	}

	private void runLoop() {
		Set<TChunk> toUpdate = new HashSet<>();
		synchronized (chunksToUpdate) {
			toUpdate.addAll(chunksToUpdate);
			chunksToUpdate.clear();
		}
		for (TChunk chunk : toUpdate) {
			if (!chunk.isLoaded()) {
				setOwnership(chunk, null);
				for (TPointColumn column : getAll(chunk)) {
					plugin.getRenderer().deleteColumn(column);
				}
				continue;
			}
			Faction faction = factions.getFactionLandOwner(chunk);
			String landOwner = faction.getId();
			String prev = getOwnership(chunk);
			if (prev != null && prev.equals(landOwner)) {
				continue;
			}
			setOwnership(chunk, landOwner);
			for (TPointColumn column : getAll(chunk)) {
				plugin.getRenderer().deleteColumn(column);
			}
			addIfDifferent(landOwner, new TChunk(chunk.getWorld(), chunk.getX(), chunk.getZ() + 1), () -> getN(chunk));
			addIfDifferent(landOwner, new TChunk(chunk.getWorld(), chunk.getX() + 1, chunk.getZ() + 1), () -> getNE(chunk));
			addIfDifferent(landOwner, new TChunk(chunk.getWorld(), chunk.getX() + 1, chunk.getZ()), () -> getE(chunk));
			addIfDifferent(landOwner, new TChunk(chunk.getWorld(), chunk.getX() + 1, chunk.getZ() - 1), () -> getSE(chunk));
			addIfDifferent(landOwner, new TChunk(chunk.getWorld(), chunk.getX(), chunk.getZ() - 1), () -> getS(chunk));
			addIfDifferent(landOwner, new TChunk(chunk.getWorld(), chunk.getX() - 1, chunk.getZ() - 1), () -> getSW(chunk));
			addIfDifferent(landOwner, new TChunk(chunk.getWorld(), chunk.getX() - 1, chunk.getZ()), () -> getW(chunk));
			addIfDifferent(landOwner, new TChunk(chunk.getWorld(), chunk.getX() - 1, chunk.getZ() + 1), () -> getNW(chunk));
		}
	}

	private void addIfDifferent(String myOwner, TChunk otherChunk, Supplier<Set<TPointColumn>> points) {
		if (!otherChunk.isLoaded()) {
			return;
		}
		Faction faction = factions.getFactionLandOwner(otherChunk);
		String landOwner = faction.getId();
		if (landOwner.equals(myOwner)) {
			return;
		}
		for (TPointColumn column : points.get()) {
			plugin.getRenderer().addColumn(column);
		}
	}

	private final Set<TChunk> chunksToUpdate = new HashSet<>();

	private final Map<TChunk, String> ownershipMap = new HashMap<>();
	private final Map<String, Set<TChunk>> reverseOwnershipMap = new HashMap<>();

	private void setOwnership(TChunk chunk, String owner) {
		if (chunk == null) {
			return;
		}
		String old = ownershipMap.get(chunk);
		if (old == null) {
			if (owner == null) {
				return;
			}
		} else {
			if (owner != null && owner.equals(old)) {
				return;
			}
			Set<TChunk> l = reverseOwnershipMap.get(old);
			if (l != null) {
				l.remove(chunk);
				if (l.isEmpty()) {
					reverseOwnershipMap.remove(old);
				}
			}
		}
		if (owner == null) {
			ownershipMap.remove(chunk);
			return;
		}
		ownershipMap.put(chunk, owner);
		Set<TChunk> l = reverseOwnershipMap.get(owner);
		if (l == null) {
			reverseOwnershipMap.put(owner, l = new HashSet<>());
		}
		l.add(chunk);
	}

	private String getOwnership(TChunk chunk) {
		return ownershipMap.get(chunk);
	}

	private Set<TChunk> getChunks(String owner) {
		HashSet<TChunk> s = new HashSet<>();
		Set<TChunk> l = reverseOwnershipMap.get(owner);
		if (l != null) {
			s.addAll(l);
		}
		return Collections.unmodifiableSet(s);
	}

	private void updateChunk(TChunk chunk) {
		synchronized (chunksToUpdate) {
			chunksToUpdate.add(chunk);
		}
	}

	private void updateAllChunks(String id) {
		for (TChunk chunk : getChunks(id)) {
			updateChunk(chunk);
		}
	}

	@EventHandler
	public void chunkLoaded(ChunkLoadEvent event) {
		updateChunk(new TChunk(event.getChunk()));
	}

	@EventHandler
	public void chunkUnloaded(ChunkUnloadEvent event) {
		updateChunk(new TChunk(event.getChunk()));
	}

	@EventHandler
	public void landClaim(LandClaimEvent event) {
		FLocation location = event.getLocation();
		updateChunk(new TChunk(location.getWorldName(), (int) location.getX(), (int) location.getZ()));
	}

	@EventHandler
	public void landUnclaim(LandUnclaimEvent event) {
		FLocation location = event.getLocation();
		updateChunk(new TChunk(location.getWorldName(), (int) location.getX(), (int) location.getZ()));
	}

	@EventHandler
	public void landUnclaimAll(LandUnclaimAllEvent event) {
		Faction faction = factions.wrapFaction(event.getFaction());
		updateAllChunks(faction.getId());
	}

	private Set<TPointColumn> getAll(TChunk chunk) {
		HashSet<TPointColumn> points = new HashSet<>();
		points.addAll(getN(chunk));
		points.addAll(getS(chunk));
		points.addAll(getW(chunk));
		points.addAll(getE(chunk));
		return points;
	}

	private Set<TPointColumn> getN(TChunk chunk) {
		HashSet<TPointColumn> points = new HashSet<>();
		for (double x = 0; x <= 16.0; x += 0.5) {
			points.add(new TPointColumn(chunk.getWorld(), (chunk.getX() * 16) + x, (chunk.getZ() + 1) * 16));
		}
		return points;
	}

	private Set<TPointColumn> getS(TChunk chunk) {
		HashSet<TPointColumn> points = new HashSet<>();
		for (double x = 0; x <= 16.0; x += 0.5) {
			points.add(new TPointColumn(chunk.getWorld(), (chunk.getX() * 16) + x, chunk.getZ() * 16));
		}
		return points;
	}

	private Set<TPointColumn> getW(TChunk chunk) {
		HashSet<TPointColumn> points = new HashSet<>();
		for (double z = 0; z <= 16.0; z += 0.5) {
			points.add(new TPointColumn(chunk.getWorld(), chunk.getX() * 16, (chunk.getZ() * 16) + z));
		}
		return points;
	}

	private Set<TPointColumn> getE(TChunk chunk) {
		HashSet<TPointColumn> points = new HashSet<>();
		for (double z = 0; z <= 16.0; z += 0.5) {
			points.add(new TPointColumn(chunk.getWorld(), (chunk.getX() + 1) * 16, (chunk.getZ() * 16) + z));
		}
		return points;
	}

	private Set<TPointColumn> getNW(TChunk chunk) {
		HashSet<TPointColumn> points = new HashSet<>();
		points.add(new TPointColumn(chunk.getWorld(), chunk.getX() * 16, (chunk.getZ() + 1) * 16));
		return points;
	}

	private Set<TPointColumn> getNE(TChunk chunk) {
		HashSet<TPointColumn> points = new HashSet<>();
		points.add(new TPointColumn(chunk.getWorld(), (chunk.getX() + 1) * 16, (chunk.getZ() + 1) * 16));
		return points;
	}

	private Set<TPointColumn> getSW(TChunk chunk) {
		HashSet<TPointColumn> points = new HashSet<>();
		points.add(new TPointColumn(chunk.getWorld(), chunk.getX(), chunk.getZ() * 16));
		return points;
	}

	private Set<TPointColumn> getSE(TChunk chunk) {
		HashSet<TPointColumn> points = new HashSet<>();
		points.add(new TPointColumn(chunk.getWorld(), (chunk.getX() + 1) * 16, chunk.getZ() * 16));
		return points;
	}
}
