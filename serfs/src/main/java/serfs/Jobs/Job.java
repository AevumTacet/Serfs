package serfs.Jobs;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;

import serfs.SerfData;

public abstract class Job {
	protected UUID entityID;
	protected SerfData data;
	protected Location startLocation;
	protected long startTime;

	protected Block target;

	public Job(UUID entityID, SerfData data, Location startLocation) {
		this.entityID = entityID;
		this.data = data;
		this.startTime = System.currentTimeMillis();
		this.startLocation = startLocation;
		System.out.println("Starting Job: " + this.getClass().getName());
	}

	protected Villager getEntity() {
		return (Villager) Bukkit.getEntity(entityID);
	}

	protected Inventory getInventory() {
		Villager villager = getEntity();
		if (villager == null) {
			return null;
		}
		return villager.getInventory();
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
