package serfs.Jobs;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import serfs.SerfData;

public class NoJob extends Job {
	public boolean canFollow = true;

	public NoJob(SerfData data, Location startLocation) {
		super(data, startLocation);
	}

	@Override
	protected String getJobID() {
		return "FOLLOW";
	}

	@Override
	public void onBehaviorStart() {
	}

	@Override
	public void onBehaviorTick() {
		Player owner = data.getOwner();
		if (owner == null || !canFollow) {
			return;
		}
		Villager villager = getEntity();

		villager.setTarget(owner);
		double distance = villager.getLocation().distance(owner.getLocation());

		if (distance > 30) {
			villager.teleport(owner.getLocation());
		} else if (distance > 5) {
			double speed = distance < 15 ? 0.5 : 1;
			villager.getPathfinder().moveTo(owner.getLocation(), speed);
		} else {
			villager.getPathfinder().stopPathfinding();
		}
	}

	@Override
	public void onBehaviorInteract() {
		if (!canFollow) {
			Villager villager = getEntity();
			villager.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, villager.getEyeLocation(), 10, 1, 1, 1, 0.1);
			this.canFollow = true;
		}
	}

	@Override
	protected void nextJob() {

	}

}
