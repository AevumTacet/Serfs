package serfs.Jobs.Farmer;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Villager;
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

	public CollectorJob(Villager entity, SerfData data, Location startLocation) {
		super(entity, data, startLocation);
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

		currentTick++;

		Block block = startLocation.getBlock();
		if (block.getType() != Material.CHEST) {
			System.out.println("Skipping collecting since no chest was found");
			nextJob();
			return;
		}

		Chest chest = (Chest) block.getState();
		double distance = entity.getLocation().distance(startLocation);

		if (getTime() > 1000 * 15) {
			nextJob();
			return;
		}

		if (!canInteract) {
			entity.getEquipment().setItemInMainHand(null);
			if (chest.isOpen()) {
				chest.close();
			}
			return;
		}

		entity.lookAt(startLocation);
		List<ItemStack> inventoryList = Arrays.asList(inventory.getContents());

		if (distance < 1.5) {
			entity.getPathfinder().stopPathfinding();

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
					entity.swingMainHand();
					// entity.shakeHead();
					chest.getInventory().addItem(item);
					inventory.remove(item);

					entity.getEquipment().setItemInMainHand(new ItemStack(item.getType()));
					entity.getWorld().playSound(entity.getLocation(), Sound.ITEM_BOOK_PUT, 1, 1);
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
					entity.swingMainHand();
					entity.shakeHead();

					int count = chestItem.getAmount() > 4 ? chestItem.getAmount() / 4 : 1;

					inventory.addItem(new ItemStack(chestItem.getType(), count));
					chestItem.setAmount(chestItem.getAmount() - count);
					// chest.getInventory().remove(chestItem);

					entity.getEquipment().setItemInMainHand(new ItemStack(chestItem.getType()));
					entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);

					System.out.println("Collecting " + chestItem.getType() + " x " + count + " from chest");
					canInteract = false;
					nextJob();
					return;
				}
			}

			// In the next tick this function should be called again, but with a delay
			return;

		} else {
			if (canInteract) {
				entity.getPathfinder().moveTo(startLocation, 0.5);
			}
		}

	}

	@Override
	protected void nextJob() {
		Job nextJob;
		if (canCollect) {
			nextJob = new PlanterJob(entity, data, startLocation);
		} else {
			nextJob = new HarvesterJob(entity, data, startLocation);
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

		entity.getEquipment().setItemInMainHand(null);
	}

}
