package serfs.Jobs.Fueler;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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
	private int currentTick;
	private Material requiredFuel;

	private static int horizontalDistance = 16;
	private static int verticalDistance = 4;

	public FuelerJob(SerfData data, Location startLocation) {
		super(data, startLocation);
	}

	protected static boolean canConsumeFuel(Material material) {
		return material == Material.FURNACE || material == Material.BLAST_FURNACE;
	}

	protected static boolean isFuel(Material material) {
		return material == Material.COAL || material == Material.CHARCOAL;
	}

	private static int getFuelAmount(Furnace furnace) {
		return furnace == null || furnace.getInventory().getFuel() == null ? 0
				: furnace.getInventory().getFuel().getAmount();
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
		currentTick++;

		if (getTime() > 1000 * 60) {
			nextJob();
			return;
		}

		fuel = getEntity() != null ? Stream.of(inventory.getContents())
				.filter(x -> x != null)
				.filter(x -> FuelerJob.isFuel(x.getType()))
				.collect(Collectors.toList())
				: Collections.emptyList();

		fuelCount = fuel.size();
		requiredFuel = null;

		if (fuelCount == 0) {
			logger.warning("Skipping Job sinnce there is no fuel left");
			nextJob();
			return;
		}

		if (target == null) {
			target = (Furnace) nearbyBlocks.stream()
					.filter(block -> block != null && canConsumeFuel(block.getType()))
					.filter(block -> {
						if (block.getState() instanceof Furnace) {
							Furnace furnace = (Furnace) block.getState();
							ItemStack item = furnace.getInventory().getFuel();
							return item == null || item.getAmount() < 64 / nearbyBlocks.size();
						}
						return false;
					})
					.min(Comparator.comparingDouble(block -> block.getLocation().distance(villager.getLocation())))
					.map(x -> x.getState())
					.orElse(null);

			if (target == null) {
				logger.warning("Skipping job since no valid blocks were found");
				nextJob();
				return;
			}
		} else {
			double distance = villager.getLocation().distance(target.getLocation());

			if (distance < 1.5) {

				villager.getPathfinder().stopPathfinding();
				villager.lookAt(target.getLocation());

				if (currentTick % 10 != 0) {
					return;
				}

				ItemStack targetFuel = target.getInventory().getFuel();
				int index = targetFuel == null ? random.nextInt(fuel.size())
						: fuel.stream()
								.filter(item -> item.getType() == targetFuel.getType())
								.findFirst()
								.map(fuel::indexOf)
								.orElse(-1);

				if (index == -1) {
					logger.warning("No compatible fuel was found in the villager's inventory.. Going to get more fuel.");
					requiredFuel = targetFuel != null ? targetFuel.getType() : null;
					fuelCount = 0;
					nextJob();
					return;
				}

				if (getFuelAmount(target) >= 64 / nearbyBlocks.size() + 1) {
					target = null;
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

						villager.getEquipment().setItemInMainHand(item);
						villager.getWorld().playSound(villager.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1, 1);
						villager.getWorld().spawnParticle(Particle.SMOKE, target.getLocation(), 10, 0.25, 0.25, 0.25, 0.05);
						villager.swingMainHand();
						return;
					}
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
			nextJob = new CollectorJob(data, startLocation,
					x -> requiredFuel != null ? x.getType() == requiredFuel : FuelerJob.isFuel(x.getType()), getJobID());
			((CollectorJob) nextJob).greedy = true;
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
