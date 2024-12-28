package serfs.Jobs.Farmer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import serfs.SerfData;
import serfs.Utils;
import serfs.Jobs.Job;

public class HarvesterJob extends Job {
	private List<Block> nearbyBlocks;

	public HarvesterJob(Villager entity, SerfData data, Location startLocation) {
		super(entity, data, startLocation);
	}

	@Override
	public void onBehaviorStart() {
		nearbyBlocks = Utils.getNearbyBlocks(startLocation, 15, 5, 15, material -> Utils.isHarvestable(material));
	}

	@Override
	public void onBehaviorTick() {
		if (!Utils.isDay()) {
			// Villager should not be working at night
			return;
		}

		if (target == null) {
			target = nearbyBlocks.stream()
					.filter(block -> {
						if (block.getBlockData() instanceof Ageable) {
							Ageable ageable = (Ageable) block.getBlockData();
							return ageable.getAge() == ageable.getMaximumAge();
						}
						return false;
					})
					.min(Comparator.comparingDouble(block -> block.getLocation().distance(entity.getLocation())))
					.orElse(null);

			if (target == null) {
				System.out.println("Skipping Harvest since no valid blocks were found");
				nextJob();
				return;
			}
		} else {
			double distance = entity.getLocation().distance(target.getLocation());
			if (distance < 1.5) {
				entity.swingMainHand();
				target.breakNaturally();
				// inventory.addItem(target.getDrops().toArray(new ItemStack[0]));
				target = null;
			} else {
				entity.getPathfinder().moveTo(target.getLocation(), 0.5);
			}
		}

		if (getTime() > 1000 * 60) {
			nextJob();
		}
	}

	@Override
	protected void nextJob() {
		List<ItemStack> inventoryList = Arrays.asList(inventory.getContents());
		long seedNumber = inventoryList.stream().filter(x -> x != null && Utils.isSeed(x.getType())).count();

		Job nextJob;
		if (seedNumber == 0) {
			nextJob = new CollectorJob(entity, data, startLocation);
			((CollectorJob) nextJob).canCollect = true;
		} else {
			nextJob = new PlanterJob(entity, data, startLocation);
		}
		data.setBehavior(nextJob);
	}

}
