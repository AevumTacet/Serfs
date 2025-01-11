package serfs.Jobs.Farmer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import serfs.SerfData;
import serfs.Utils;
import serfs.Jobs.Job;

public class HarvesterJob extends Job {
	private List<Block> nearbyBlocks;

	public HarvesterJob(UUID entityID, SerfData data, Location startLocation) {
		super(entityID, data, startLocation);
	}

	@Override
	protected String getJobID() {
		return "FARMER";
	}

	@Override
	public void onBehaviorStart() {
		nearbyBlocks = Utils.getNearbyBlocks(startLocation, 20, 5, 20, material -> Utils.isHarvestable(material));
	}

	@Override
	public void onBehaviorTick() {
		if (!Utils.isDay()) {
			// Villager should not be working at night
			return;
		}

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
	protected void nextJob() {
		Inventory inventory = getInventory();
		long seedNumber;
		if (inventory != null) {
			List<ItemStack> inventoryList = Arrays.asList(inventory.getContents());
			seedNumber = inventoryList.stream().filter(x -> x != null && Utils.isSeed(x.getType())).count();
		} else {
			seedNumber = 0;
		}

		Job nextJob;
		if (seedNumber == 0) {
			nextJob = new CollectorJob(entityID, data, startLocation);
			((CollectorJob) nextJob).canCollect = true;
		} else {
			nextJob = new PlanterJob(entityID, data, startLocation);
		}
		data.setBehavior(nextJob);
	}

}
