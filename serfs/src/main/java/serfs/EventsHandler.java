package serfs;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EventsHandler implements Listener {
	private Logger logger;
	private SerfManager manager = Main.manager;
	private static NamespacedKey flag_key = new NamespacedKey(Main.plugin, "trade_flag");;

	public EventsHandler(Logger logger) {
		this.logger = logger;
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();
		if (entity == null) {
			return;
		}

		if (!(entity instanceof Villager)) {
			return;
		}

		Villager villager = (Villager) entity;
		// Player player = event.getPlayer();
		if (manager.isServant(villager)) {
			SerfData data = manager.getServant(villager.getUniqueId());
			data.getBehavior().onBehaviorInteract();
			event.setCancelled(true);
			return;
		}

		if (villager.getProfession() == Villager.Profession.FARMER) {

			for (MerchantRecipe recipe : villager.getRecipes()) {
				ItemStack result = recipe.getResult();
				ItemMeta resultMeta = result.getItemMeta();
				if (resultMeta != null) {
					String flag = resultMeta.getPersistentDataContainer().get(flag_key, PersistentDataType.STRING);
					if ("hire_farmer".equals(flag)) {
						return;
					}
				}
			}

			// Create a custom trade
			ItemStack itemToSell = new ItemStack(Material.BOOK, 1);
			ItemMeta meta = itemToSell.getItemMeta();
			meta.getPersistentDataContainer().set(flag_key, PersistentDataType.STRING, "hire_farmer");
			meta.displayName(Component.text("Hire Farmer"));
			itemToSell.setItemMeta(meta);

			ItemStack itemToBuy = new ItemStack(Material.EMERALD, 64);
			MerchantRecipe customTrade = new MerchantRecipe(itemToSell, 0, 1, true);
			customTrade.addIngredient(itemToBuy);

			// Add the custom trade to the villager
			List<MerchantRecipe> trades = new ArrayList<>(villager.getRecipes());
			trades.add(customTrade);
			villager.setRecipes(trades);

			logger.info("Added custom trade to villager: " + villager.getUniqueId());
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		HumanEntity player = event.getWhoClicked();

		if (!(event.getInventory().getHolder() instanceof Villager)) {
			return;
		}

		Villager villager = (Villager) event.getInventory().getHolder();

		if (villager.getProfession() == Villager.Profession.FARMER) {
			ItemStack item = event.getCurrentItem();
			if (item == null) {
				return;
			}

			ItemMeta meta = item.getItemMeta();
			if (meta == null) {
				return;
			}

			String flag = meta.getPersistentDataContainer().get(flag_key, PersistentDataType.STRING);
			if (flag == null) {
				return;
			}

			if (flag.equals("hire_farmer")) {
				Player playerEntity = (Player) player;
				Main.manager.registerEntity(villager, playerEntity);
				player.sendMessage("Hired farmer!");
				event.getInventory().close();
				event.setCancelled(true);
			}
		}
	}
}
