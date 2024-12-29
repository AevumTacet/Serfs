package serfs.Jobs;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;

import serfs.SerfData;

public abstract class Job {
	protected long startTime;
	protected Villager entity;
	protected SerfData data;
	protected Location startLocation;

	protected Inventory inventory;
	protected Block target;

	public Job(Villager entity, SerfData data, Location startLocation) {
		this.entity = entity;
		this.data = data;
		this.startTime = System.currentTimeMillis();
		this.startLocation = startLocation;
		inventory = entity.getInventory();

		System.out.println("Starting Job: " + this.getClass().getName());
	}

	protected long getTime() {
		return System.currentTimeMillis() - startTime;
	}

	public abstract void onBehaviorStart();

	public abstract void onBehaviorTick();

	protected abstract void nextJob();

	public void onBehaviorInteract() {
	};

	public void onBehaviorEnd() {
	};
}
