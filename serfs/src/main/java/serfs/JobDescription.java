package serfs;

import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;

import serfs.Jobs.Base.Job;

public class JobDescription {
	public Class<? extends Job> jobClass;
	public Villager.Profession targetProfession;
	public String jobName;
	public int hireCost;

	public JobDescription(String jobName, Class<? extends Job> startJobClass, Profession targetProfession,
			int hireCost) {
		this.jobClass = startJobClass;
		this.targetProfession = targetProfession;
		this.jobName = jobName;
		this.hireCost = hireCost;
	}

}
