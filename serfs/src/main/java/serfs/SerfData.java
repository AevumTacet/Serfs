package serfs;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import serfs.Jobs.Job;

public class SerfData {
	private UUID entityID;
	private UUID ownerID;
	private boolean selected;
	private Job behavior;

	public SerfData(LivingEntity entity, Player owner) {
		this.entityID = entity.getUniqueId();
		this.ownerID = owner.getUniqueId();
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		if (selected && isValid()) {
			getEntity().getWorld().playSound(getEntity().getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1, 1);
		}
	}

	public UUID getEntityID() {
		return entityID;
	}

	public UUID getOwnerID() {
		return ownerID;
	}

	public Job getBehavior() {
		return behavior;
	}

	public void setBehavior(Job newBehavior) {
		this.behavior.onBehaviorEnd();

		newBehavior.onBehaviorStart();
		this.behavior = newBehavior;
	}

	public LivingEntity getEntity() {
		return (LivingEntity) Bukkit.getEntity(entityID);
	}

	public Player getOwner() {
		return Bukkit.getPlayer(ownerID);
	}

	public boolean isValid() {
		return getEntity() != null && getOwner() != null && getEntity().isValid();
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
