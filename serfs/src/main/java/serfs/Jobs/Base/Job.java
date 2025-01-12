package serfs.Jobs.Base;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.Inventory;
import de.tr7zw.nbtapi.NBTCompound;
import serfs.SerfData;
import serfs.IO.Serializable;

public abstract class Job implements Serializable {
	public boolean jobStarted;

	protected UUID entityID;
	protected SerfData data;
	protected long startTime;
	protected Block target;

	public Job(SerfData data) {
		this.entityID = data.getEntityID();
		this.data = data;
		this.startTime = System.currentTimeMillis();
		this.jobStarted = false;
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

		this.onSetPersistentData(comp);
	}
}
