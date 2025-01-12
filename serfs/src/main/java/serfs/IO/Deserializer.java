package serfs.IO;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import de.tr7zw.nbtapi.NBTCompound;
import serfs.PlayerData;
import serfs.SerfData;
import serfs.Jobs.NoJob;
import serfs.Jobs.Base.Job;
import serfs.Jobs.Farmer.HarvesterJob;
import serfs.Main;

public interface Deserializer {
    public static PlayerData readPlayerData(NBTCompound nbt) {
        UUID playerID = UUID.fromString(nbt.getString("PlayerID"));
        NBTCompound serfs = nbt.getCompound("Serfs");

        PlayerData state = new PlayerData(playerID);

        serfs.getKeys().stream()
                .map(key -> serfs.getCompound(key))
                .map(comp -> readSerfData(comp))
                .forEach(serf -> state.addSerf(serf));

        return state;
    }

    public static SerfData readSerfData(NBTCompound nbt) {
        UUID entityID = UUID.fromString(nbt.getString("EntityID"));
        UUID ownerID = UUID.fromString(nbt.getString("OwnerID"));

        SerfData state = new SerfData(entityID, ownerID);
        Job behavior = readBehavior(nbt, state);
        state.setBehavior(behavior);

        return state;
    }

    public static Job readBehavior(NBTCompound nbt, SerfData data) {
        NBTCompound comp = nbt.getCompound("Job");
        String mode = comp.getString("CurrentBehavior");

        Location startLocation;
        if (comp.hasTag("StartLocationW") && comp.hasTag("StartLocationX") &&
                comp.hasTag("StartLocationY") && comp.hasTag("StartLocationZ")) {
            String locationW = comp.getString("StartLocationW");
            double locationX = comp.getDouble("StartLocationX");
            double locationY = comp.getDouble("StartLocationY");
            double locationZ = comp.getDouble("StartLocationZ");
            startLocation = new Location(Bukkit.getWorld(locationW), locationX, locationY, locationZ);
        } else {
            startLocation = null;
        }

        Job behavior = switch (mode) {
            case "FARMER":
                yield new HarvesterJob(data.getEntityID(), data, startLocation);

            case "FOLLOW":
                yield new NoJob(data.getEntityID(), data, startLocation);

            default:
                Main.plugin.getLogger().warning("Job state is unspecified, defaulting to follow");
                yield new NoJob(data.getEntityID(), data, startLocation);
        };

        return behavior;
    }
}
