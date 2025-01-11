package serfs.IO;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTFile;

public class NBTExporter {
    private Logger logger;
    private NBTFile file;

    public NBTExporter(Plugin plugin) {
        this.logger = plugin.getLogger();
        File worldDir = Bukkit.getWorlds().get(0).getWorldFolder();

        try {
            file = new NBTFile(new File(worldDir, "serf.dat"));
            this.getDataContainer();
        } catch (IOException e) {
            logger.warning("Serfs NBT IO could not be initialized!");
            e.printStackTrace();
        }
    }

    public NBTCompound getDataContainer() {
        var nbt = file.addCompound("PlayerData");
        return nbt;
    }

    public void writePlayer(Serializable player) {
        var nbt = getDataContainer();
        player.export(nbt);
    }

    public void save() {
        try {
            file.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        file.clearNBT();
    }
}
