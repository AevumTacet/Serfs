package serfs;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class EventsHandler implements Listener {
	private Logger logger;

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

		if (villager.getProfession() == Villager.Profession.FARMER) {
			// Create a custom trade
			ItemStack itemToSell = new ItemStack(Material.BOOK, 1);
			ItemMeta meta = itemToSell.getItemMeta();
			meta.displayName(Component.text("Contract"));
			itemToSell.setItemMeta(meta);

			ItemStack itemToBuy = new ItemStack(Material.EMERALD, 64);
			MerchantRecipe customTrade = new MerchantRecipe(itemToSell, 0, 10, true);
			customTrade.addIngredient(itemToBuy);

			// Add the custom trade to the villager
			List<MerchantRecipe> trades = new ArrayList<>(villager.getRecipes());
			trades.add(customTrade);
			villager.setRecipes(trades);

			logger.info("Added custom trade to villager: " + villager.getUniqueId());
		}
	}
}
