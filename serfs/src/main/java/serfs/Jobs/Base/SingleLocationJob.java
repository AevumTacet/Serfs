package serfs.Jobs.Base;

import org.bukkit.Location;

import de.tr7zw.nbtapi.NBTCompound;
import serfs.SerfData;

public abstract class SingleLocationJob extends Job {
	protected Location startLocation;

	public SingleLocationJob(SerfData data, Location startLocation) {
		super(data);
		this.startLocation = startLocation;
	}

	@Override
	protected void onSetPersistentData(NBTCompound nbt) {
		super.onSetPersistentData(nbt);

		if (startLocation != null) {
			nbt.setString("StartLocationW", startLocation.getWorld().getName());
			nbt.setDouble("StartLocationX", startLocation.getX());
			nbt.setDouble("StartLocationY", startLocation.getY());
			nbt.setDouble("StartLocationZ", startLocation.getZ());
		}
	}

}
