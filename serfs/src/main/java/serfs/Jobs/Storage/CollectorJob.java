package serfs.Jobs.Storage;

import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import serfs.SerfData;
import serfs.Jobs.NoJob;

public final class CollectorJob extends StorageJob {
	private int currentTick;
	private Random random = new Random();

	public CollectorJob(SerfData data, Location startLocation, Predicate<ItemStack> itemFilter, String jobID) {
		super(data, startLocation, itemFilter, jobID);
	}

	@Override
	public void onBehaviorStart() {
		currentTick = 0;
	}

	@Override
	public void onBehaviorTick() {
		Villager villager = getEntity();
		Inventory inventory = getInventory();
		currentTick++;

		Block block = startLocation.getBlock();
		if (block.getType() != Material.CHEST) {
			NoJob job = new NoJob(data, startLocation);
			job.canFollow = false;

			System.err.println("Villager chest was not found, reverting to NoJob.");
			villager.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, villager.getEyeLocation(), 10, 1, 1, 1, 0.1);
			data.setBehavior(job);
			return;
		}

		Chest chest = (Chest) block.getState();
		double distance = villager.getLocation().distance(startLocation);

		if (getTime() > 1000 * 15) {
			nextJob();
			return;
		}

		if (!canInteract) {
			villager.getEquipment().setItemInMainHand(null);
			if (chest.isOpen()) {
				chest.close();
			}
			return;
		}

		villager.lookAt(startLocation);

		if (distance < 1.5) {
			villager.getPathfinder().stopPathfinding();

			if (currentTick % 10 != 0) {
				return;
			}

			if (!chest.isOpen()) {
				chest.open();
				return;
			}

			var chestItems = Stream.of(chest.getInventory().getContents())
					.filter(x -> x != null)
					.filter(getItemFilter())
					.collect(Collectors.toList());

			ItemStack chestItem = chestItems.get(random.nextInt(chestItems.size()));

			if (chestItem != null) {
				villager.swingMainHand();

				int count = chestItem.getAmount() > 4 ? chestItem.getAmount() / 4 : 1;

				inventory.addItem(new ItemStack(chestItem.getType(), count));
				chestItem.setAmount(chestItem.getAmount() - count);

				villager.getEquipment().setItemInMainHand(new ItemStack(chestItem.getType()));
				villager.getWorld().playSound(villager.getLocation(),
						Sound.ENTITY_ITEM_PICKUP, 1, 1);

				canInteract = false;
				nextJob();
				return;
			}
		} else {
			villager.getPathfinder().moveTo(startLocation, 0.5);
		}

	}

	// @Override
	// protected void nextJob() {
	// Job nextJob = nextJobSupplier.get();
	// data.setBehavior(nextJob);

	// // Job nextJob;
	// // if (canCollect) {
	// // nextJob = new PlanterJob(entityID, data, startLocation);
	// // } else {
	// // nextJob = new HarvesterJob(entityID, data, startLocation);
	// // }
	// // data.setBehavior(nextJob);
	// }

}
