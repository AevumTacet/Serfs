package serfs.Behaviors;

import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.LivingEntity;

import serfs.SerfData;

public abstract class Behavior {
	public int tickCount;
	public AbstractVillager entity;
	public SerfData data;

	public Behavior(AbstractVillager entity, SerfData data) {
		this.entity = entity;
		this.data = data;
		tickCount = 0;
	}

	public abstract void onBehaviorStart();

	public abstract void onBehaviorTick();

	public abstract void onBehaviorInteract();

	public abstract void onBehaviorEnd();
}
