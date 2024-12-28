package serfs.Jobs;

import org.bukkit.Location;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import serfs.SerfData;

public class NoJob extends Job {
	private Villager.Profession profession;

	public NoJob(AbstractVillager entity, SerfData data, Location startLocation) {
		super(entity, data, startLocation);
	}

	@Override
	public void onBehaviorStart() {
		profession = ((Villager) entity).getProfession();
	}

	@Override
	public void onBehaviorTick() {
		Player owner = data.getOwner();
		if (owner == null) {
			return;
		}
		entity.setTarget(owner);

		double distance = entity.getLocation().distance(owner.getLocation());
		if (distance > 30) {
			entity.teleport(owner.getLocation());
		} else if (distance > 5) {
			double speed = distance < 15 ? 0.5 : 1;
			entity.getPathfinder().moveTo(owner.getLocation(), speed);
		} else {
			entity.getPathfinder().stopPathfinding();
		}
	}

	@Override
	public void onBehaviorInteract() {
	}

	@Override
	public void onBehaviorEnd() {
	}

}
