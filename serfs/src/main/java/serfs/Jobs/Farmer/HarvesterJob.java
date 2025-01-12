package serfs.Jobs.Farmer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import serfs.SerfData;
import serfs.Utils;
import serfs.Jobs.Storage.CollectorJob;

public final class HarvesterJob extends FarmerJob {
	public HarvesterJob(SerfData data, Location startLocation) {
		super(data, startLocation, x -> Utils.isHarvestable(x));
	}

	@Override
	public void onBehaviorTick() {
		Villager villager = getEntity();

		if (target == null) {
			target = nearbyBlocks.stream()
					.filter(block -> {
						if (block.getBlockData() instanceof Ageable) {
							Ageable ageable = (Ageable) block.getBlockData();
							return ageable.getAge() == ageable.getMaximumAge();
						}
						return false;
					})
					.min(Comparator.comparingDouble(block -> block.getLocation().distance(villager.getLocation())))
					.orElse(null);

			if (target == null) {
				nextJob();
				return;
			}
		} else {
			double distance = villager.getLocation().distance(target.getLocation());

			if (distance < 1.5) {
				villager.swingMainHand();
				target.breakNaturally();
				target = null;
			} else {
				villager.getPathfinder().moveTo(target.getLocation(), 0.5);
			}
		}

		if (getTime() > 1000 * 30) {
			nextJob();
		}
	}

	@Override
	public void nextJob() {
		Inventory inventory = getInventory();
		long seedNumber;
		if (inventory != null) {
			List<ItemStack> inventoryList = Arrays.asList(inventory.getContents());
			seedNumber = inventoryList.stream()
					.filter(x -> x != null)
					.filter(x -> Utils.isSeed(x.getType()))
					.count();
		} else {
			seedNumber = 0;
		}

		if (seedNumber == 0) {
			var nextJob = new CollectorJob(data, startLocation, x -> Utils.isSeed(x.getType()), getJobID());
			nextJob.setNextJob(() -> new PlanterJob(data, startLocation));
			data.setBehavior(nextJob);

			Villager villager = getEntity();
			if (villager != null) {
				villager.shakeHead();
			}

		} else {
			var nextJob = new PlanterJob(data, startLocation);
			data.setBehavior(nextJob);
		}
	}

}
