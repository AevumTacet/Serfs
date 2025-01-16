package serfs.Jobs.Farmer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import serfs.SerfData;
import serfs.Utils;
import serfs.Jobs.Base.ISequentialJob;
import serfs.Jobs.Base.SingleLocationJob;

public abstract class FarmerJob extends SingleLocationJob implements ISequentialJob {
	private static int horizontalDistance = 20;
	private static int verticalDistance = 5;
	protected List<Block> nearbyBlocks;
	private Predicate<Material> blockFilter;
	protected Block target;

	public FarmerJob(SerfData data, Location startLocation, Predicate<Material> blockFilter) {
		super(data, startLocation);
		this.blockFilter = blockFilter;
	}

	@Override
	public final String getJobID() {
		return "FARMER";
	}

	@Override
	public void onBehaviorStart() {
		nearbyBlocks = Utils.getNearbyBlocks(startLocation,
				horizontalDistance, verticalDistance, horizontalDistance, blockFilter);
	}

	public abstract void nextJob();

	protected static HashMap<Material, Material> seedToBlockMap = new HashMap<Material, Material>() {
		{
			put(Material.WHEAT_SEEDS, Material.WHEAT);
			put(Material.CARROT, Material.CARROTS);
			put(Material.POTATO, Material.POTATOES);
			put(Material.BEETROOT_SEEDS, Material.BEETROOTS);
			put(Material.MELON_SEEDS, Material.MELON);
			put(Material.PUMPKIN_SEEDS, Material.PUMPKIN);
		}
	};

	protected static HashSet<Material> harvestableItems = new HashSet<Material>() {
		{
			add(Material.WHEAT);
			add(Material.CARROT);
			add(Material.POTATO);
			add(Material.BEETROOTS);
			add(Material.MELON);
			add(Material.PUMPKIN);
		}
	};

	protected static boolean isCrop(Material material) {
		return harvestableItems.contains(material);
	}

	protected static boolean isHarvestable(Material material) {
		return seedToBlockMap.containsValue(material);
	}

	protected static boolean isSeed(Material material) {
		return seedToBlockMap.containsKey(material);
	}

}
