package serfs.Jobs.Farmer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import serfs.SerfData;
import serfs.Utils;
import serfs.Jobs.Job;

public class PlanterJob extends Job {
	public PlanterJob(UUID entityID, SerfData data, Location startLocation) {
		super(entityID, data, startLocation);
	}

	@Override
	protected String getJobID() {
		return "FARMER";
	}

	@Override
	public void onBehaviorStart() {
		Villager villager = getEntity();
		if (villager != null) {
			villager.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_HOE));
		}
	}

	@Override
	public void onBehaviorTick() {
		if (!Utils.isDay()) {
			// Villager should not be working at night
			return;
		}

		Villager villager = getEntity();
		Inventory inventory = getInventory();

		List<ItemStack> seeds = Stream.of(inventory.getContents())
				.filter(item -> item != null)
				.filter(item -> Utils.isSeed(item.getType()))
				.collect(Collectors.toList());

		if (seeds.size() == 0) {
			nextJob();
			return;
		}

		if (target == null) {
			var nearbyBlocks = Utils.getNearbyBlocks(startLocation, 20, 5, 20, material -> material == Material.FARMLAND);
			target = nearbyBlocks.stream()
					.filter(block -> block.getRelative(BlockFace.UP).getType() == Material.AIR)
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

				Block relative = target.getLocation().add(0, 1.5, 0).getBlock();
				if (relative.getType() == Material.AIR) {

					if (!seeds.isEmpty()) {
						Random random = new Random();
						int index = random.nextInt(seeds.size());

						ItemStack seed = seeds.get(index);
						if (seed != null && seed.getType() != null) {
							relative.setType(Utils.seedToBlockMap.getOrDefault(seed.getType(), Material.AIR));

							inventory.remove(seed);
							int amount = seed.getAmount() - 1;
							if (amount > 0) {
								seed.setAmount(amount);
								inventory.addItem(seed);
							}
						}
					}
				}

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
		long cropNumber;
		if (inventory != null) {
			List<ItemStack> inventoryList = Arrays.asList(inventory.getContents());
			cropNumber = inventoryList.stream().filter(x -> x != null && Utils.isCrop(x.getType())).count();
		} else {
			cropNumber = 0;
		}

		boolean canStore = cropNumber > 0;
		Job nextJob = new CollectorJob(entityID, data, startLocation);
		((CollectorJob) nextJob).canStore = canStore;
		data.setBehavior(nextJob);
	}

	@Override
	public void onBehaviorEnd() {
		Villager villager = getEntity();
		if (villager != null) {
			villager.getEquipment().setItemInMainHand(null);
		}
	}

}
