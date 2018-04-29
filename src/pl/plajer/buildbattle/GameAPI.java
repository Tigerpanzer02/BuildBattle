package pl.plajer.buildbattle;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import pl.plajer.buildbattle.arena.Arena;
import pl.plajer.buildbattle.events.SetupInventoryEvents;
import pl.plajer.buildbattle.events.SpectatorEvents;
import pl.plajer.buildbattle.handlers.ChatManager;
import pl.plajer.buildbattle.handlers.ConfigurationManager;
import pl.plajer.buildbattle.arena.ArenaRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import pl.plajer.buildbattle.commands.InstanceCommands;
import pl.plajer.buildbattle.commands.SignCommands;

import java.io.IOException;

/**
 * Created by Tom on 25/07/2014.
 */
//todo remove me
public class GameAPI {

    private ArenaRegistry gameInstanceManager;
    private boolean bar = false;
    private Main plugin;
    private boolean needsMapRestore = false;

    public Main getPlugin() {
        return plugin;
    }

    public boolean isBarEnabled() {
        return bar;
    }

    public boolean needsMapRestore() {
        return needsMapRestore;
    }

    public void onSetup(Main plugin, CommandsInterface commandsInterface) {
        this.plugin = plugin;
        gameInstanceManager = new ArenaRegistry();
        bar = plugin.getConfig().getBoolean("bar");
        Arena.plugin = this;

        plugin.getServer().getPluginManager().registerEvents(new SpectatorEvents(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SetupInventoryEvents(this), plugin);

        loadLanguageFile();
        plugin.saveConfig();
        ChatManager.getFromLanguageConfig("Unlocks-at-level", ChatColor.GREEN + "Unlocks at level %NUMBER% ");
        if(plugin.getConfig().getBoolean("BungeeActivated")) {
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        }

        plugin.getCommand("buildbattle").setExecutor(new InstanceCommands(this, commandsInterface));
        plugin.getCommand("addsigns").setExecutor(new SignCommands(this));
    }

    public ArenaRegistry getGameInstanceManager() {
        return gameInstanceManager;
    }

    public void loadLanguageFile() {
        FileConfiguration config = ConfigurationManager.getConfig("language");
        try {
            config.save("language");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void saveLoc(String path, Location loc) {
        String location = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
        plugin.getConfig().set(path, location);
        plugin.saveConfig();
    }


    public Location getLocation(String path) {
        String[] loc = plugin.getConfig().getString(path).split("\\,");
        plugin.getServer().createWorld(new WorldCreator(loc[0]));
        World w = plugin.getServer().getWorld(loc[0]);
        Double x = Double.parseDouble(loc[1]);
        Double y = Double.parseDouble(loc[2]);
        Double z = Double.parseDouble(loc[3]);
        float yaw = Float.parseFloat(loc[4]);
        float pitch = Float.parseFloat(loc[5]);
        Location location = new Location(w, x, y, z, yaw, pitch);
        return location;
    }


    public WorldEditPlugin getWorldEditPlugin() {
        Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if(p instanceof WorldEditPlugin) return (WorldEditPlugin) p;
        return null;
    }

}