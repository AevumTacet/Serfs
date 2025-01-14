package serfs;

import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

import serfs.Jobs.Supplier.ISupplier;
import serfs.Jobs.Supplier.JobSupplier;
import serfs.Jobs.Supplier.SingleLocationJobSupplier;

public class JobDescription {
	public Villager.Profession targetProfession;
	public ISupplier jobSupplier;
	public String jobName;
	public int hireCost;

	public JobDescription(String jobName, Profession targetProfession, int hireCost) {
		this.targetProfession = targetProfession;
		this.jobName = jobName;
		this.hireCost = hireCost;
	}

	public JobDescription(String jobName, JobSupplier jobSupplier, Profession targetProfession, int hireCost) {
		this(jobName, targetProfession, hireCost);
		this.jobSupplier = jobSupplier;
	}

	public JobDescription(String jobName, SingleLocationJobSupplier jobSupplier, Profession targetProfession,
			int hireCost) {
		this(jobName, targetProfession, hireCost);
		this.jobSupplier = jobSupplier;
	}

}
