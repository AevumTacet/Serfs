package serfs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.scheduler.BukkitRunnable;

import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.VanillaGoal;

import serfs.Jobs.NoJob;

public class SerfManager {
	private Logger logger;
	private HashMap<UUID, PlayerData> playerData = new HashMap<UUID, PlayerData>();
	private static HashSet<UUID> trackedSerfs = new HashSet<UUID>();

	public SerfManager(Logger logger) {
		this.logger = logger;
		Update();
	}

	public boolean isServant(Entity entity) {
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
		PlayerData data = playerData.get(owner.getUniqueId());
		if (data == null) {
			data = new PlayerData(owner.getUniqueId());
			playerData.put(owner.getUniqueId(), data);
		}

		// Supress default villager behaviors
		Bukkit.getMobGoals().removeGoal(entity, VanillaGoal.MOVE_BACK_TO_VILLAGE);
		Bukkit.getMobGoals().removeGoal(entity, VanillaGoal.TRADE_WITH_PLAYER);
		entity.setMemory(MemoryKey.HOME, null);
		// entity.setMemory(MemoryKey.JOB_SITE, null);
		// entity.setProfession(Villager.Profession.FARMER);

		SerfData serf = new SerfData(entity, owner);
		serf.setBehavior(new NoJob(entity, serf, entity.getLocation()));
		data.addSerf(serf);
		trackedSerfs.add(entity.getUniqueId());

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
						.filter(serf -> serf != null && serf.getEntity() != null)
						.forEach(serf -> {
							Entity entity = serf.getEntity();

							serf.getBehavior().onBehaviorTick();
							serf.getBehavior().tickCount++;

							if (serf.isSelected()) {
								entity.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, entity.getLocation(), 10);
							}
							entity.setGlowing(serf.isSelected());
						});
			}
		}.runTaskTimer(Main.plugin, 0, 2);
	};
}
