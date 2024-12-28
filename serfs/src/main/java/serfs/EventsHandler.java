package serfs;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
		Player player = event.getPlayer();

		if (manager.isServant(villager)) {
			SerfData data = manager.getServant(villager.getUniqueId());

			if (player.equals(data.getOwner())) {
				data.setSelected(!data.isSelected());
				data.getBehavior().onBehaviorInteract();
			}
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
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		HumanEntity player = event.getWhoClicked();

		if (!(event.getInventory().getHolder() instanceof Villager)) {
			return;
		}

		Villager villager = (Villager) event.getInventory().getHolder();

		if (villager.getProfession() == Villager.Profession.FARMER
				&& event.getAction() == InventoryAction.PICKUP_ALL) {

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

				event.getInventory().close();
				player.getInventory().remove(new ItemStack(Material.EMERALD, 64));

				player.sendMessage("Hired farmer!");
			}
		}
	}

	@EventHandler
	public void onPlayerClickBlock(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		Block block = event.getClickedBlock();
		Player player = event.getPlayer();

		var selected = manager.getServants(player).filter(serf -> serf.isSelected()).collect(Collectors.toList());
		selected.forEach(serf -> {
			serf.assignJob(block.getLocation());
			serf.setSelected(false);
		});

		if (selected.size() > 0) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if (manager.isServant(entity)) {
			SerfData serf = manager.getServant(entity.getUniqueId());
			if (serf.getOwner() != null) {
				serf.getOwner().sendMessage("Your serf has died!");
			}

			manager.unregisterEntity(entity.getUniqueId());
		}
	}

}
