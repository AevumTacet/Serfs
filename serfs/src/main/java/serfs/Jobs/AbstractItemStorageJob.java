package serfs.Jobs;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;

import serfs.SerfData;

public abstract class AbstractItemStorageJob extends Job {

	private Predicate<ItemStack> itemFilter;
	private Supplier<Job> nextJobSupplier;
	private String jobID;
	protected boolean canInteract;

	public boolean CanInteract() {
		return canInteract;
	}

	public void setCanInteract(boolean canInteract) {
		this.canInteract = canInteract;
	}

	public AbstractItemStorageJob(SerfData data, Location startLocation, Predicate<ItemStack> itemFilter, String jobID) {
		super(data, startLocation);
		this.itemFilter = itemFilter;
		this.jobID = jobID;
	}

	public final void setNextJob(Supplier<Job> supplier) {
		this.nextJobSupplier = supplier;
	}

	protected Predicate<ItemStack> getItemFilter() {
		return itemFilter;
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

	@Override
	protected final void nextJob() {
		if (nextJobSupplier != null) {
			Job nextJob = nextJobSupplier.get();
			data.setBehavior(nextJob);
		}
	}

	@Override
	protected final String getJobID() {
		return jobID;
	}

}
