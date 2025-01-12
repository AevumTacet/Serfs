package serfs.Jobs.Farmer;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
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
import serfs.Utils;
import serfs.Jobs.Job;
import serfs.Jobs.NoJob;

public class CollectorJob extends Job {
	public boolean canStore;
	public boolean canCollect;

	private Predicate<ItemStack> itemFilter;
	private Supplier<Job> nextJobSupplier;
	private String jobID;

	private int currentTick;
	private boolean canInteract;
	private Random random = new Random();

	public CollectorJob(SerfData data, Location startLocation, String jobID, Supplier<Job> nextJob,
			Predicate<ItemStack> itemFilter) {
		super(data, startLocation);
		this.jobID = jobID;
		this.nextJobSupplier = nextJob;
		this.itemFilter = itemFilter;
	}

	@Override
	protected String getJobID() {
		return jobID;
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
				} else {
					canInteract = false;
					return;
				}
			}

			if (canCollect && canInteract) {

				var chestSeeds = Stream.of(chest.getInventory().getContents())
						.filter(x -> x != null)
						.filter(itemFilter)
						// .filter(x -> x != null && Utils.isSeed(x.getType()))
						// .filter(x -> x.getAmount() > 4)
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
		Job nextJob = nextJobSupplier.get();
		data.setBehavior(nextJob);

		// Job nextJob;
		// if (canCollect) {
		// nextJob = new PlanterJob(entityID, data, startLocation);
		// } else {
		// nextJob = new HarvesterJob(entityID, data, startLocation);
		// }
		// data.setBehavior(nextJob);
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
