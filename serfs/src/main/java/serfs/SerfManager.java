package serfs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.scheduler.BukkitRunnable;

import serfs.IO.Deserializer;
import serfs.IO.NBTExporter;
import serfs.Jobs.Job;
import serfs.Jobs.NoJob;

public class SerfManager {
	private Logger logger;
	private NBTExporter nbt = new NBTExporter(Main.plugin);
	private HashMap<UUID, PlayerData> playerData = new HashMap<UUID, PlayerData>();
	private static HashSet<UUID> trackedSerfs = new HashSet<UUID>();

	public SerfManager(Logger logger) {
		this.logger = logger;
		Update();
	}

	public void savePlayers(boolean verbose) {
		nbt.clear();
		playerData.values().forEach(player -> nbt.writePlayer(player));
		nbt.save();
		if (verbose == true) {
			logger.info("Saved player data for " + playerData.size() + " players.");
		}
	}

	public void restorePlayers() {
		playerData.clear();
		var container = nbt.getDataContainer();

		container.getKeys().stream()
				.map(key -> container.getCompound(key))
				.map(comp -> Deserializer.readPlayerData(comp))
				.forEach(state -> {
					playerData.put(state.getPlayerID(), state);
				});

		logger.info("Restored data for " + container.getKeys().size() + " players.");

		trackedSerfs = getServants()
				.map(data -> data.getEntityID())
				.collect(Collectors.toCollection(HashSet::new));

		logger.info("Restored " + trackedSerfs.size() + " entities.");
	}

	public boolean isServant

	(Entity entity) {
		return entity instanceof Villager && trackedSerfs.contains(entity.getUniqueId());
	}

	public Stream<SerfData> getServants(Player player) {
		PlayerData data = playerData.get(player.getUniqueId());
		if (data == null) {
			return Stream.empty();
		}
		return data.getServants();
	}

	public Stream<SerfData> getServants() {
		return playerData.values().stream().flatMap(PlayerData::getServants);
	}

	public SerfData getServant(UUID entityID) {
		return getServants().filter(x -> x.getEntityID().equals(entityID))
				.findFirst()
				.orElse(null);
	}

	public void registerEntity(Villager entity, Player owner) {
		World world = entity.getWorld();
		UUID entityID = entity.getUniqueId();

		PlayerData data = playerData.get(owner.getUniqueId());
		if (data == null) {
			data = new PlayerData(owner.getUniqueId());
			playerData.put(owner.getUniqueId(), data);
		}

		logger.info("Registering Serf with UUID: " + entityID);

		Bukkit.getMobGoals().removeAllGoals(entity);
		entity.setMemory(MemoryKey.HOME, null);
		entity.setPersistent(true);
		entity.setRemoveWhenFarAway(false);

		SerfData serf = new SerfData(entity, owner);
		serf.setBehavior(new NoJob(entityID, serf, entity.getLocation()));
		data.addSerf(serf);
		trackedSerfs.add(entityID);

		world.playSound(entity.getLocation(), Sound.ENTITY_VILLAGER_CELEBRATE, 1, 1);
		world.spawnParticle(Particle.HAPPY_VILLAGER, entity.getLocation(), 10);
	}

	public void unregisterEntity(UUID entityID) {
		trackedSerfs.remove(entityID);
		playerData.values().forEach(data -> data.removeSerf(entityID));
		logger.info("Unregistering Serf with UUID: " + entityID);
	}

	public void Update() {
		new BukkitRunnable() {
			@Override
			public void run() {
				getServants()
						.filter(serf -> serf != null && serf.getEntity() != null && serf.getOwner() != null)
						.forEach(serf -> {
							Entity entity = serf.getEntity();
							Job currentJob = serf.getBehavior();

							if (!currentJob.jobStarted) {
								currentJob.onBehaviorStart();
							}

							currentJob.onBehaviorTick();

							if (serf.isSelected()) {
								entity.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, entity.getLocation(), 10);
							}
							entity.setGlowing(serf.isSelected());
						});
			}
		}.runTaskTimer(Main.plugin, 0, 2);
	};
}
