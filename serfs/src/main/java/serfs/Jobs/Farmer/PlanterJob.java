package serfs.Jobs.Farmer;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.inventory.ItemStack;

import serfs.SerfData;
import serfs.Utils;
import serfs.Jobs.Job;

public class PlanterJob extends Job {
	public PlanterJob(AbstractVillager entity, SerfData data, Location startLocation) {
		super(entity, data, startLocation);
	}

	@Override
	public void onBehaviorStart() {
		entity.getEquipment().setItemInMainHand(new ItemStack(Material.STONE_HOE));

	}

	@Override
	public void onBehaviorTick() {
		List<ItemStack> seeds = Stream.of(inventory.getContents())
				.filter(item -> item != null)
				.filter(item -> Utils.isSeed(item.getType()))
				.collect(Collectors.toList());

		if (seeds.size() == 0) {
			System.out.println("Skipping planting since Villager has no seeds left");
			nextJob();
			return;
		}

		if (target == null) {
			var nearbyBlocks = Utils.getNearbyBlocks(startLocation, 15, material -> material == Material.FARMLAND);
			target = nearbyBlocks.stream()
					.filter(block -> block.getRelative(BlockFace.UP).getType() == Material.AIR)
					.min(Comparator.comparingDouble(block -> block.getLocation().distance(entity.getLocation())))
					.orElse(null);

			if (target == null) {
				System.out.println("Skipping planting since no valid blocks were found");
				nextJob();
				return;
			}
		} else {
			double distance = entity.getLocation().distance(target.getLocation());
			if (distance < 1.5) {
				entity.swingMainHand();

				Block relative = target.getRelative(BlockFace.UP);
				if (relative.getType() == Material.AIR) {

					if (!seeds.isEmpty()) {
						Random random = new Random();
						int index = random.nextInt(seeds.size());

						ItemStack seed = seeds.get(index);
						if (seed != null && seed.getType() != null) {
							relative.setType(Utils.seedMap.getOrDefault(seed.getType(), Material.AIR));

							inventory.remove(seed);
							int amount = seed.getAmount() - 1;
							if (amount > 0) {
								seed.setAmount(amount);
								inventory.addItem(seed);
							}
						}
					}
					System.out.println("Seed count: " + seeds.size());
				}

				target = null;
			} else {
				entity.getPathfinder().moveTo(target.getLocation(), 0.5);
			}
		}

		if (getTime() > 1000 * 60) {
			nextJob();
		}
	}

	@Override
	protected void nextJob() {
		Job nextJob = new CollectorJob(entity, data, startLocation);
		data.setBehavior(nextJob);
	}

}
