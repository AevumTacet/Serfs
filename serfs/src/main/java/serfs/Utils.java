package serfs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

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

	public static HashMap<Material, Material> seedToBlockMap = new HashMap<Material, Material>() {
		{
			put(Material.WHEAT_SEEDS, Material.WHEAT);
			put(Material.CARROT, Material.CARROTS);
			put(Material.POTATO, Material.POTATOES);
			put(Material.BEETROOT_SEEDS, Material.BEETROOTS);
			put(Material.MELON_SEEDS, Material.MELON);
			put(Material.PUMPKIN_SEEDS, Material.PUMPKIN);
		}
	};

	public static HashSet<Material> harvestableItems = new HashSet<Material>() {
		{
			add(Material.WHEAT);
			add(Material.CARROT);
			add(Material.POTATO);
			add(Material.BEETROOTS);
			add(Material.MELON);
			add(Material.PUMPKIN);
		}
	};

	public static boolean isCrop(Material material) {
		return harvestableItems.contains(material);
	}

	public static boolean isHarvestable(Material material) {
		return seedToBlockMap.containsValue(material);
	}

	public static boolean isSeed(Material material) {
		return seedToBlockMap.containsKey(material);
	}
}
