package serfs.Jobs.Supplier;

import serfs.SerfData;
import serfs.Jobs.Base.Job;

@FunctionalInterface
public interface JobSupplier extends ISupplier {
	public Job get(SerfData data);
}
