package serfs.Jobs.Farmer;

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
	public int horizontalDistance;
	public int verticalDistance;
	protected Location startLocation;
	protected List<Block> nearbyBlocks;
	private Predicate<Material> blockFilter;
	protected Block target;

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

	public abstract void nextJob();

}
