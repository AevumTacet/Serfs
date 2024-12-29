package serfs;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;

import de.tr7zw.nbtapi.NBTCompound;
import serfs.IO.Serializable;

public class PlayerData implements Serializable {
	private UUID playerID;

	public UUID getPlayerID() {
		return playerID;
	}

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

	@Override
	public void export(NBTCompound nbt) {
		var comp = nbt.addCompound(playerID.toString());
		comp.setString("PlayerID", playerID.toString());

		var serfComp = comp.addCompound("Serfs");
		getServants().forEach(state -> state.export(serfComp));
	}
}
