package serfs;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    public static Plugin plugin;
    public static SerfManager manager;

    @Override
    public void onEnable() {
        plugin = this;
        super.onEnable();
        manager = new SerfManager(this.getLogger());
        manager.restorePlayers();

        EventsHandler handler = new EventsHandler(this.getLogger());
        getServer().getPluginManager().registerEvents(handler, this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

}