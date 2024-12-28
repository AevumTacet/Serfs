package serfs.Behaviors;

import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Player;

import serfs.SerfData;

public class FollowBehavior extends Behavior {
	public FollowBehavior(AbstractVillager entity, SerfData data) {
		super(entity, data);
	}

	@Override
	public void onBehaviorStart() {
	}

	@Override
	public void onBehaviorTick() {
		Player owner = data.getOwner();
		if (owner == null) {
			return;
		}

		double distance = entity.getLocation().distance(owner.getLocation());
		if (distance > 30) {
			entity.teleport(owner.getLocation());
		} else if (distance > 5) {
			entity.getPathfinder().moveTo(owner.getLocation(), 0.5);
		} else {
			entity.getPathfinder().stopPathfinding();
		}
	}

	@Override
	public void onBehaviorInteract() {
		System.out.println("Clicked");
	}

	@Override
	public void onBehaviorEnd() {
	}

}
