package serfs.Jobs.Farmer;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import serfs.SerfData;
import serfs.Utils;
import serfs.Jobs.Job;

public class CollectorJob extends Job {
	public boolean canStore;
	public boolean canCollect;

	private int currentTick;
	private boolean canInteract;
	private Random random = new Random();

	public CollectorJob(UUID entityID, SerfData data, Location startLocation) {
		super(entityID, data, startLocation);
	}

	@Override
	protected String getJobID() {
		return "FARMER";
	}

	@Override
	public void onBehaviorStart() {
		currentTick = 0;
		canInteract = true;
	}

	@Override
	public void onBehaviorTick() {
		if (!Utils.isDay()) {
			// Villager should not be working at night
			return;
		}
		Villager villager = getEntity();
		Inventory inventory = getInventory();

		currentTick++;

		Block block = startLocation.getBlock();
		if (block.getType() != Material.CHEST) {
			System.err.println("Skipping collecting since no chest was found");
			nextJob();
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

			if (!chest.isOpen() && (canCollect || canStore) && canInteract) {
				chest.open();
				return;
			}

			if (canStore && canInteract) {
				ItemStack item = inventoryList.stream()
						.filter(x -> x != null)
						.findAny().orElse(null);

				if (item != null) {
					villager.swingMainHand();
					// entity.shakeHead();
					chest.getInventory().addItem(item);
					inventory.remove(item);

					villager.getEquipment().setItemInMainHand(new ItemStack(item.getType()));
					villager.getWorld().playSound(villager.getLocation(), Sound.ITEM_BOOK_PUT, 1, 1);
					System.out.println("Storing " + item + " in chest");
				} else {
					canInteract = false;
					return;
				}
			}

			if (canCollect && canInteract) {

				var chestSeeds = Stream.of(chest.getInventory().getContents())
						.filter(x -> x != null && Utils.isSeed(x.getType()))
						.filter(x -> x.getAmount() > 4)
						.collect(Collectors.toList());

				ItemStack chestItem = chestSeeds.get(random.nextInt(chestSeeds.size()));

				if (chestItem != null) {
					villager.swingMainHand();
					villager.shakeHead();

					int count = chestItem.getAmount() > 4 ? chestItem.getAmount() / 4 : 1;

					inventory.addItem(new ItemStack(chestItem.getType(), count));
					chestItem.setAmount(chestItem.getAmount() - count);
					// chest.getInventory().remove(chestItem);

					villager.getEquipment().setItemInMainHand(new ItemStack(chestItem.getType()));
					villager.getWorld().playSound(villager.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);

					System.out.println("Collecting " + chestItem.getType() + " x " + count + " from chest");
					canInteract = false;
					nextJob();
					return;
				}
			}

			// In the next tick this function should be called again, but with a delay
			return;

		} else {
			if (canInteract && (canCollect || canStore)) {
				villager.getPathfinder().moveTo(startLocation, 0.5);
			}
		}

	}

	@Override
	protected void nextJob() {
		Job nextJob;
		if (canCollect) {
			nextJob = new PlanterJob(entityID, data, startLocation);
		} else {
			nextJob = new HarvesterJob(entityID, data, startLocation);
		}
		data.setBehavior(nextJob);
	}

	@Override
	public void onBehaviorEnd() {
		Block block = startLocation.getBlock();
		if (block.getType() == Material.CHEST) {
			Chest chest = (Chest) block.getState();
			if (chest.isOpen()) {
				chest.close();
			}
		}

		Villager villager = getEntity();
		if (villager != null) {
			villager.getEquipment().setItemInMainHand(null);
		}
	}

}
