package serfs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import serfs.Jobs.Farmer.HarvesterJob;
import serfs.Jobs.Fueler.FuelerJob;

public final class HireUtils {
	private HireUtils() {
	}

	public final static NamespacedKey flagKey = new NamespacedKey(Main.plugin, "trade_flag");;

	public final static HashMap<String, JobDescription> jobMap = new HashMap<>() {
		{
			put("hire_farmer", new JobDescription("Farmer", HarvesterJob.class, Profession.FARMER, 64));
			put("hire_fueler", new JobDescription("Fueler", FuelerJob.class, Profession.TOOLSMITH, 64));
		}
	};

	private static HashMap<Profession, String> descriptorMap = null;

	@Nullable
	private static JobDescription getDescription(Profession profession) {
		if (descriptorMap == null) {
			descriptorMap = jobMap.keySet().stream().collect(
					Collectors.toMap(key -> jobMap.get(key).targetProfession, key -> key, (e1, e2) -> e1, HashMap::new));
		}

		String key = descriptorMap.getOrDefault(profession, null);
		return key != null ? jobMap.get(key) : null;
	}

	public static void generateTrade(Villager villager, Player player) {
		Profession profession = villager.getProfession();
		JobDescription description = getDescription(profession);

		if (description == null) {
			return;
		}

		for (MerchantRecipe recipe : villager.getRecipes()) {
			ItemStack result = recipe.getResult();
			ItemMeta resultMeta = result.getItemMeta();
			if (resultMeta != null) {
				String flag = resultMeta.getPersistentDataContainer().get(flagKey, PersistentDataType.STRING);

				if (jobMap.containsKey(flag)) {
					// Don't create custom trade if it's already present.
					return;
				}
			}
		}

		// Create a custom trade
		ItemStack itemToSell = new ItemStack(Material.BOOK, 1);
		ItemMeta meta = itemToSell.getItemMeta();
		meta.setEnchantmentGlintOverride(true);

		String hireKey = descriptorMap.get(profession);
		meta.getPersistentDataContainer().set(flagKey, PersistentDataType.STRING, hireKey);
		meta.displayName(Component.text("Hire as " + description.jobName));
		itemToSell.setItemMeta(meta);

		ItemStack itemToBuy = new ItemStack(Material.EMERALD, description.hireCost);
		MerchantRecipe customTrade = new MerchantRecipe(itemToSell, 0, 1, true);
		customTrade.addIngredient(itemToBuy);

		// Add the custom trade to the villager
		List<MerchantRecipe> trades = new ArrayList<>(villager.getRecipes());
		trades.add(customTrade);
		villager.setRecipes(trades);
	}

	public static boolean hire(Villager villager, String flag, Player player) {
		Profession profession = villager.getProfession();
		JobDescription description = getDescription(profession);
		String hireKey = descriptorMap.get(profession);

		if (description == null) {
			return false;
		}

		if (flag.equals(hireKey)) {
			Main.manager.registerEntity(villager, player);
			player.getInventory().remove(new ItemStack(Material.EMERALD, description.hireCost));

			Main.plugin.getLogger()
					.info("Player " + player.getName() + " hired a " + description.jobName);
			return true;
		}

		return false;
	}
}
