package serfs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Utils {
	public static List<Block> getNearbyBlocks(Location loc, int sizeX, int sizeY, int sizeZ,
			Predicate<Material> filter) {
		List<Block> blocks = new ArrayList<Block>();
		for (int x = -sizeX; x <= sizeX; x++) {
			for (int y = -sizeY; y <= sizeY; y++) {
				for (int z = -sizeZ; z <= sizeZ; z++) {
					Block block = loc.getWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
					if (filter.test(block.getType())) {
						blocks.add(block);
					}
				}
			}
		}
		return blocks;
	}

	public static boolean isDay() {
		long time = Bukkit.getWorld("world").getTime();
		return time >= 0 && time < 12300;
	}

}
