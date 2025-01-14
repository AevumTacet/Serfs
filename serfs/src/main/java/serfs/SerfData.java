package serfs;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

import de.tr7zw.nbtapi.NBTCompound;
import serfs.IO.Serializable;
import serfs.Jobs.NoJob;
import serfs.Jobs.Base.Job;
import serfs.Jobs.Farmer.HarvesterJob;
import serfs.Jobs.Fueler.FuelerJob;
import serfs.Jobs.Supplier.SingleLocationJobSupplier;

public class SerfData implements Serializable {
	private UUID entityID;
	private UUID ownerID;
	private boolean selected;
	private Job behavior;

	public SerfData(UUID entityID, UUID playerID) {
		this.entityID = entityID;
		this.ownerID = playerID;
	}

	public SerfData(LivingEntity entity, Player owner) {
		this(entity.getUniqueId(), owner.getUniqueId());
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
		if (newBehavior == null) {
			return;
		}

		if (getEntity() != null) {
			if (this.behavior != null) {
				this.behavior.onBehaviorEnd();
			}
			newBehavior.onBehaviorStart();
			newBehavior.jobStarted = true;
		}

		this.behavior = newBehavior;
	}

	public Entity getEntity() {
		return Bukkit.getEntity(entityID);
	}

	public Villager getVillager() {
		return (Villager) getEntity();
	}

	public Player getOwner() {
		return Bukkit.getPlayer(ownerID);
	}

	public boolean isValid() {
		return getEntity() != null && getOwner() != null && getEntity().isValid();
	}

	public Villager.Profession getProfession() {
		return ((Villager) getEntity()).getProfession();
	}

	public void assignJob(Location jobLocation) {
		Block block = jobLocation.getBlock();
		if (behavior != null && behavior instanceof NoJob) {
			Profession profession = getProfession();

			JobDescription description = HireUtils.getDescription(profession);
			if (description == null) {
				return;
			}

			getVillager().getInventory().clear();

			if (block.getType() == Material.CHEST) {
				var supplier = (SingleLocationJobSupplier) description.jobSupplier;
				Job job = supplier.get(this, jobLocation);
				setBehavior(job);
			}

		}

		getEntity().getWorld().playSound(getEntity().getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
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

	@Override
	public void export(NBTCompound nbt) {
		var comp = nbt.addCompound(entityID.toString());

		comp.setString("EntityID", entityID.toString());
		comp.setString("OwnerID", ownerID.toString());
		behavior.export(comp);
	}

}
