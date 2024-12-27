package serfs;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import serfs.Behaviors.Behavior;

public class SerfData {
	private UUID entityID;

	public UUID getEntityID() {
		return entityID;
	}

	private UUID ownerID;

	public UUID getOwnerID() {
		return ownerID;
	}

	private Behavior behavior;

	public Behavior getBehavior() {
		return behavior;
	}

	public void setBehavior(Behavior behavior) {
		this.behavior = behavior;
	}

	public SerfData(LivingEntity entity, Player owner) {
		this.entityID = entity.getUniqueId();
		this.ownerID = owner.getUniqueId();
	}

	public LivingEntity getEntity() {
		return (LivingEntity) Bukkit.getEntity(entityID);
	}

	public Player getOwner() {
		return Bukkit.getPlayer(ownerID);
	}

	@Override
	public int hashCode() {
		return entityID.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SerfData) {
			SerfData other = (SerfData) obj;
			return entityID.equals(other.entityID);
		}
		return false;
	}

}
