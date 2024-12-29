package serfs.Jobs;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;

import de.tr7zw.nbtapi.NBTCompound;
import serfs.SerfData;
import serfs.IO.Serializable;

public abstract class Job implements Serializable {
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

	protected abstract String getJobID();

	public void onBehaviorInteract() {
	};

	public void onBehaviorEnd() {
	};

	protected void onSetPersistentData(NBTCompound nbt) {
	}

	@Override
	public void export(NBTCompound nbt) {
		var comp = nbt.addCompound("Job");
		comp.setString("CurrentBehavior", getJobID());

		if (startLocation != null) {
			nbt.setString("StartLocationW", startLocation.getWorld().getName());
			nbt.setDouble("StartLocationX", startLocation.getX());
			nbt.setDouble("StartLocationY", startLocation.getY());
			nbt.setDouble("StartLocationZ", startLocation.getZ());
		}
		this.onSetPersistentData(comp);
	}
}
