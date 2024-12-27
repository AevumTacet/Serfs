package serfs.Behaviors;

import org.bukkit.entity.LivingEntity;

import serfs.SerfData;

public abstract class Behavior {
	public int tickCount;
	public LivingEntity entity;
	public SerfData data;

	public Behavior(LivingEntity entity, SerfData data) {
		this.entity = entity;
		this.data = data;
		tickCount = 0;
	}

	public abstract void onBehaviorStart();

	public abstract void onBehaviorTick();

	public abstract void onBehaviorEnd();
}
