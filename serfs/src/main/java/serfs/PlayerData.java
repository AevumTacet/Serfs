package serfs;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;

public class PlayerData {
	private UUID playerID;
	private HashMap<UUID, SerfData> serfs;

	public PlayerData(UUID playerID) {
		this.playerID = playerID;
		serfs = new HashMap<UUID, SerfData>();
		this.playerID = playerID;
	}

	public void addSerf(SerfData serf) {
		serfs.put(serf.getEntityID(), serf);
	}

	public void removeSerf(UUID entityID) {
		serfs.remove(entityID);
	}

	public int getSerfCount() {
		return serfs.size();
	}

	public Stream<SerfData> getServants() {
		return serfs.values().stream();
	}
}
