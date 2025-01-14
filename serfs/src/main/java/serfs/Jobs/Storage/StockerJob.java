package serfs.Jobs.Storage;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
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

public final class StockerJob extends StorageJob {
	private int currentTick;

	public StockerJob(SerfData data, Location startLocation, Predicate<ItemStack> itemFilter, String jobID) {
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
			NoJob job = new NoJob(data);
			job.canFollow = false;

			logger.warning("Villager chest was not found, reverting to NoJob.");
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
		List<ItemStack> inventoryList = Arrays.asList(inventory.getContents());

		if (distance < 1.5) {
			villager.getPathfinder().stopPathfinding();

			if (currentTick % 10 != 0) {
				return;
			}

			if (!chest.isOpen()) {
				chest.open();
				return;
			}

			ItemStack item = inventoryList.stream()
					.filter(x -> x != null)
					.filter(getItemFilter())
					.findAny().orElse(null);

			if (item != null) {
				villager.swingMainHand();
				chest.getInventory().addItem(item);
				inventory.remove(item);

				villager.getEquipment().setItemInMainHand(new ItemStack(item.getType()));
				villager.getWorld().playSound(villager.getLocation(), Sound.ITEM_BOOK_PUT, 1, 1);
			} else {
				canInteract = false;
				return;
			}

		} else {
			villager.getPathfinder().moveTo(startLocation, 0.5);
		}

	}

}
