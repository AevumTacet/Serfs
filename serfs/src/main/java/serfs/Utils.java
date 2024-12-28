package serfs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Utils {
	public static List<Block> getNearbyBlocks(Location loc, int radius, Predicate<Material> filter) {
		List<Block> blocks = new ArrayList<Block>();
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					Block block = loc.getWorld().getBlockAt(loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
					if (filter.test(block.getType())) {
						blocks.add(block);
					}
				}
			}
		}
		return blocks;
	}

	public static HashMap<Material, Material> seedMap = new HashMap<Material, Material>() {
		{
			put(Material.WHEAT_SEEDS, Material.WHEAT);
			put(Material.CARROT, Material.CARROTS);
			put(Material.POTATO, Material.POTATOES);
			put(Material.BEETROOT_SEEDS, Material.BEETROOTS);
			put(Material.MELON_SEEDS, Material.MELON);
			put(Material.PUMPKIN_SEEDS, Material.PUMPKIN);
		}
	};

	public static boolean isHarvestable(Material material) {
		return seedMap.containsValue(material);
	}

	public static boolean isSeed(Material material) {
		return seedMap.containsKey(material);
	}
}
