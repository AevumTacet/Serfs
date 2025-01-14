package serfs.Jobs.Fueler;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import serfs.SerfData;
import serfs.Utils;
import serfs.Jobs.Base.ISequentialJob;
import serfs.Jobs.Base.SingleLocationJob;
import serfs.Jobs.Storage.CollectorJob;
import serfs.Jobs.Storage.StockerJob;
import serfs.Jobs.Storage.StorageJob;

public final class FuelerJob extends SingleLocationJob implements ISequentialJob {
	protected List<Block> nearbyBlocks;
	private Furnace target;
	private List<ItemStack> fuel;
	private Random random = new Random();
	private int fuelCount;

	private static int horizontalDistance;
	private static int verticalDistance;

	public FuelerJob(SerfData data, Location startLocation) {
		super(data, startLocation);
	}

	protected static boolean canConsumeFuel(Material material) {
		return material == Material.FURNACE || material == Material.BLAST_FURNACE;
	}

	protected static boolean isFuel(Material material) {
		return material == Material.COAL || material == Material.CHARCOAL;
	}

	@Override
	public void onBehaviorStart() {
		nearbyBlocks = Utils.getNearbyBlocks(startLocation,
				horizontalDistance, verticalDistance, horizontalDistance, FuelerJob::canConsumeFuel);
	}

	@Override
	public void onBehaviorTick() {
		Villager villager = getEntity();
		Inventory inventory = getInventory();

		if (getTime() > 1000 * 30) {
			nextJob();
			return;
		}

		fuel = getEntity() != null ? Stream.of(getInventory().getContents())
				.filter(x -> FuelerJob.isFuel(x.getType()))
				.collect(Collectors.toList())
				: Collections.emptyList();

		fuelCount = fuel.size();
		if (fuelCount == 0) {
			nextJob();
			return;
		}

		if (target == null) {
			target = (Furnace) nearbyBlocks.stream()
					.filter(block -> {
						if (block.getState() instanceof Furnace) {
							Furnace furnace = (Furnace) block.getState();
							ItemStack item = furnace.getInventory().getFuel();
							return item == null || item.getAmount() < 64 - (fuelCount / nearbyBlocks.size());
						}
						return false;
					})
					.min(Comparator.comparingDouble(block -> block.getLocation().distance(villager.getLocation())))
					.map(x -> x.getState())
					.orElse(null);

			if (target == null) {
				nextJob();
				return;
			}
		} else {
			double distance = villager.getLocation().distance(target.getLocation());

			if (distance < 1.5) {

				ItemStack targetFuel = target.getInventory().getFuel();
				int index = targetFuel == null ? random.nextInt(fuel.size())
						: fuel.stream()
								.filter(item -> item.getType() == targetFuel.getType())
								.findFirst()
								.map(fuel::indexOf)
								.orElse(-1);

				if (index == -1) {
					logger.warning("No comaptible fuel was found in the villager inventory.. Going to get more fuel.");
					nextJob();
					return;
				}

				if (!fuel.isEmpty()) {
					ItemStack item = fuel.get(index);
					if (item != null && item.getType() != null) {

						if (targetFuel == null) {
							target.getInventory().setFuel(new ItemStack(item.getType(), 1));
						} else {
							targetFuel.setAmount(targetFuel.getAmount() + 1);
							target.getInventory().setFuel(targetFuel);
						}

						inventory.remove(item);
						int amount = item.getAmount() - 1;
						if (amount > 0) {
							item.setAmount(amount);
							inventory.addItem(item);
						}

						villager.getEquipment().setItemInMainHand(new ItemStack(item.getType()));
					}

					villager.swingMainHand();
					villager.getWorld().playSound(villager.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1, 1);
					return;
				}

				target = null;
			} else {
				villager.getPathfinder().moveTo(target.getLocation(), 0.5);
			}
		}
	}

	@Override
	protected String getJobID() {
		return "FUELER";
	}

	@Override
	public void nextJob() {
		StorageJob nextJob;
		if (fuelCount == 0) {
			nextJob = new CollectorJob(data, startLocation, x -> FuelerJob.isFuel(x.getType()), getJobID());
		} else {
			nextJob = new StockerJob(data, startLocation, x -> FuelerJob.isFuel(x.getType()), getJobID());
		}

		nextJob.setNextJob(() -> new FuelerJob(data, startLocation));
		data.setBehavior(nextJob);
	}

	@Override
	public void onBehaviorEnd() {
		super.onBehaviorEnd();

		Villager villager = getEntity();
		if (villager != null) {
			villager.getEquipment().setItemInMainHand(null);
		}
	}

}
