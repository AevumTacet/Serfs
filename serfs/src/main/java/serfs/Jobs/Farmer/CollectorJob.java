package serfs.Jobs.Farmer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.inventory.ItemStack;

import serfs.SerfData;
import serfs.Jobs.Job;

public class CollectorJob extends Job {
	public CollectorJob(AbstractVillager entity, SerfData data, Location startLocation) {
		super(entity, data, startLocation);
	}

	@Override
	public void onBehaviorStart() {
	}

	@Override
	public void onBehaviorTick() {
		Block block = startLocation.getBlock();
		if (block.getType() == Material.CHEST) {
			Chest chest = (Chest) block.getState();
			double distance = entity.getLocation().distance(startLocation);

			if (distance < 1.5) {
				if (!inventory.isEmpty()) {
					chest.open();

					ItemStack item = inventory.getItem(0);
					entity.swingMainHand();
					chest.getInventory().addItem(item);
					inventory.remove(item);
					return; // In the next tick this function should be called again, but with a delay
				}

				entity.getPathfinder().stopPathfinding();
			} else {
				chest.close();
			}

		} else {
			entity.getPathfinder().moveTo(startLocation, 0.5);
		}

		if (getTime() > 1000 * 30) {
			nextJob();
		}
	}

	@Override
	protected void nextJob() {
		Job nextJob = new HarvesterJob(entity, data, startLocation);
		data.setBehavior(nextJob);
	}

}
