package serfs.Jobs.Farmer;

import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import serfs.SerfData;
import serfs.Utils;
import serfs.Jobs.Job;

public abstract class FarmerJob extends Job {
	public int horizontalDistance;
	public int verticalDistance;
	protected List<Block> nearbyBlocks;
	private Predicate<Material> blockFilter;

	public FarmerJob(SerfData data, Location startLocation, Predicate<Material> blockFilter) {
		super(data, startLocation);

		this.horizontalDistance = 20;
		this.verticalDistance = 5;
		this.blockFilter = blockFilter;
	}

	@Override
	protected final String getJobID() {
		return "FARMER";
	}

	@Override
	public void onBehaviorStart() {
		nearbyBlocks = Utils.getNearbyBlocks(startLocation,
				horizontalDistance, verticalDistance, horizontalDistance, blockFilter);
	}

}
