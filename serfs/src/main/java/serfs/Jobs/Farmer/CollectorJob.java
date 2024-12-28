package serfs.Jobs.Farmer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
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
	private int currentTick;
	private boolean canInteract;

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
		currentTick++;

		Block block = startLocation.getBlock();
		if (block.getType() != Material.CHEST) {
			System.out.println("Skipping collecting since no chest was found");
			nextJob();
			return;
		}

		Chest chest = (Chest) block.getState();
		double distance = entity.getLocation().distance(startLocation);

		if (getTime() > 1000 * 30) {
			nextJob();
			return;
		}

		if (!canInteract) {
			if (chest.isOpen()) {
				chest.close();
			}
			return;
		}

		List<ItemStack> inventoryList = Arrays.asList(inventory.getContents());
		long cropNumber = inventoryList.stream().filter(x -> x != null && Utils.isCrop(x.getType())).count();
		long seedNumber = inventoryList.stream().filter(x -> x != null && Utils.isSeed(x.getType())).count();

		if (distance < 1.5) {
			entity.lookAt(startLocation);
			entity.getPathfinder().stopPathfinding();

			if (currentTick % 10 != 0) {
				return;
			}

			if (cropNumber > 0 && canInteract) {
				if (!chest.isOpen()) {
					chest.open();
					return;
				}

				ItemStack item = inventoryList.stream()
						.filter(x -> x != null && Utils.isCrop(x.getType()))
						.findAny().orElse(null);

				System.out.println("Storing " + item + " in chest");
				if (item != null) {
					entity.swingMainHand();
					entity.shakeHead();
					chest.getInventory().addItem(item);
					inventory.remove(item);

					entity.getEquipment().setItemInMainHand(new ItemStack(item.getType()));
				}
			} else if (seedNumber == 0 && canInteract) {
				if (!chest.isOpen()) {
					chest.open();
					return;
				}
				ItemStack item = Stream.of(chest.getInventory().getContents())
						.filter(x -> x != null && Utils.isSeed(x.getType()))
						.findAny().orElse(null);

				System.out.println("Collecting " + item + " from chest");
				if (item != null) {
					entity.swingMainHand();
					entity.shakeHead();
					inventory.addItem(item);
					chest.getInventory().remove(item);

					entity.getEquipment().setItemInMainHand(new ItemStack(item.getType()));
					entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
				} else {
					canInteract = false;
				}
			} else {
				canInteract = false;
				entity.getEquipment().setItemInMainHand(null);
			}
			return; // In the next tick this function should be called again, but with a delay

		} else {
			entity.getPathfinder().moveTo(startLocation, 0.5);
		}

	}

	@Override
	protected void nextJob() {
		Job nextJob = new HarvesterJob(entity, data, startLocation);
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
