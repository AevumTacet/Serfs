package serfs.Jobs.Supplier;

import org.bukkit.Location;
import serfs.SerfData;
import serfs.Jobs.Base.SingleLocationJob;

@FunctionalInterface
public interface SingleLocationJobSupplier extends ISupplier {

	public SingleLocationJob get(SerfData data, Location startLocation);

}
