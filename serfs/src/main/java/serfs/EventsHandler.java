package serfs;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.logging.Logger;
import java.util.stream.Collectors;

public class EventsHandler implements Listener {
	private Logger logger;
	private SerfManager manager = Main.manager;

	public EventsHandler(Logger logger) {
		this.logger = logger;
		this.logger.info(("Started Serf event handler.."));
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

		HireUtils.generateTrade(villager, player);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		HumanEntity player = event.getWhoClicked();

		if (!(event.getInventory().getHolder() instanceof Villager)) {
			return;
		}
		Villager villager = (Villager) event.getInventory().getHolder();

		if (event.getAction() == InventoryAction.PICKUP_ALL) {
			ItemStack item = event.getCurrentItem();
			if (item == null || item.getItemMeta() == null) {
				return;
			}

			String flag = item.getItemMeta().getPersistentDataContainer().get(HireUtils.flagKey,
					PersistentDataType.STRING);
			if (flag == null) {
				return;
			}

			event.getInventory().close();
			boolean hired = HireUtils.hire(villager, flag, (Player) player);
			if (hired) {
				player.sendMessage("Villager hired!");
				Main.manager.registerEntity(villager, (Player) player);
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
				serf.getOwner().sendMessage("Your Villager has died!");
			}

			manager.unregisterEntity(entity.getUniqueId());
		}
	}

}
