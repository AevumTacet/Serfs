package serfs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SerfManager {
	private Logger logger;
	private HashMap<UUID, PlayerData> playerData = new HashMap<UUID, PlayerData>();
	private static HashSet<UUID> trackedSerfs = new HashSet<UUID>();

	public SerfManager(Logger logger) {
		this.logger = logger;
		Update();
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

	public void registerEntity(LivingEntity entity, Player owner) {
		SerfData serf = new SerfData(entity, owner);
		PlayerData data = playerData.get(owner.getUniqueId());
		if (data == null) {
			data = new PlayerData(owner.getUniqueId());
			playerData.put(owner.getUniqueId(), data);
		}
		data.addSerf(serf);
		trackedSerfs.add(entity.getUniqueId());
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
						.filter(serf -> serf.getEntity() != null)
						.forEach(serf -> {
							serf.getBehavior().onBehaviorTick();
							serf.getBehavior().tickCount++;
						});
			}
		}.runTaskTimer(Main.plugin, 0, 5);
	};
}
