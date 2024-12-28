package serfs.Jobs;

import org.bukkit.Location;
import org.bukkit.entity.AbstractVillager;

import serfs.SerfData;

public abstract class Job {
	public int tickCount;
	public AbstractVillager entity;
	public SerfData data;
	public Location startLocation;

	public Job(AbstractVillager entity, SerfData data, Location startLocation) {
		this.entity = entity;
		this.data = data;
		this.tickCount = 0;
		this.startLocation = startLocation;
	}

	public abstract void onBehaviorStart();

	public abstract void onBehaviorTick();

	public abstract void onBehaviorInteract();

	public abstract void onBehaviorEnd();
}
