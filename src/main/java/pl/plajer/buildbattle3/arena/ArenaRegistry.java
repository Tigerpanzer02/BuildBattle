/*
 * BuildBattle 3 - Ultimate building competition minigame
 * Copyright (C) 2018  Plajer's Lair - maintained by Plajer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.plajer.buildbattle3.arena;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import pl.plajer.buildbattle3.Main;
import pl.plajer.buildbattle3.arena.plots.ArenaPlot;
import pl.plajer.buildbattle3.utils.Cuboid;
import pl.plajerlair.core.services.ReportedException;
import pl.plajerlair.core.utils.ConfigUtils;
import pl.plajerlair.core.utils.MinigameUtils;

/**
 * Created by Tom on 27/07/2014.
 */
public class ArenaRegistry {

  private static List<Arena> arenas = new ArrayList<>();
  private static Main plugin = JavaPlugin.getPlugin(Main.class);

  public static List<Arena> getArenas() {
    return arenas;
  }

  /**
   * Returns arena where the player is
   *
   * @param p target player
   * @return Arena or null if not playing
   */
  @Nullable
  public static Arena getArena(Player p) {
    if (p == null) return null;
    if (!p.isOnline()) return null;

    for (Arena arena : arenas) {
      for (Player player : arena.getPlayers()) {
        if (player.getUniqueId() == p.getUniqueId()) {
          return arena;
        }
      }
    }
    return null;
  }

  public static void registerArena(Arena arena) {
    Main.debug("Registering new game instance, " + arena.getID(), System.currentTimeMillis());
    arenas.add(arena);
  }

  public static void unregisterArena(Arena arena) {
    Main.debug("Unegistering game instance, " + arena.getID(), System.currentTimeMillis());
    arenas.remove(arena);
  }

  /**
   * Returns arena based by ID
   *
   * @param ID name of arena
   * @return Arena or null if not found
   */
  public static Arena getArena(String ID) {
    for (Arena arena : arenas) {
      if (arena.getID().equalsIgnoreCase(ID)) {
        return arena;
      }
    }
    return null;
  }

  public static void registerArenas() {
    try {
      Main.debug("Initial arenas registration", System.currentTimeMillis());
      ArenaRegistry.getArenas().clear();
      FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
      for (String ID : config.getConfigurationSection("instances").getKeys(false)) {
        Arena arena;
        String s = "instances." + ID + ".";
        if (s.contains("default")) continue;

        arena = new Arena(ID);

        if (config.contains(s + "minimumplayers")) arena.setMinimumPlayers(config.getInt(s + "minimumplayers"));
        else arena.setMinimumPlayers(config.getInt("instances.default.minimumplayers"));
        if (config.contains(s + "maximumplayers")) arena.setMaximumPlayers(config.getInt(s + "maximumplayers"));
        else arena.setMaximumPlayers(config.getInt("instances.default.maximumplayers"));
        if (config.contains(s + "mapname")) arena.setMapName(config.getString(s + "mapname"));
        else arena.setMapName(config.getString("instances.default.mapname"));
        if (config.contains(s + "lobbylocation")) arena.setLobbyLocation(MinigameUtils.getLocation(config.getString(s + "lobbylocation")));
        if (config.contains(s + "Endlocation")) arena.setEndLocation(MinigameUtils.getLocation(config.getString(s + "Endlocation")));
        else {
          if (!plugin.isBungeeActivated()) {
            System.out.print(ID + " doesn't contains an end location!");
            arena.setReady(false);
            ArenaRegistry.registerArena(arena);
            continue;
          }
        }
        if (config.contains(s + "gametype")) arena.setArenaType(Arena.ArenaType.valueOf(config.getString(s + "gametype").toUpperCase()));
          //assuming that arena is from 3.1.x releases we set arena type to SOLO by default
        else arena.setArenaType(Arena.ArenaType.SOLO);
        if (config.contains(s + "plots")) {
          if (config.isConfigurationSection(s + "plots")) {
            for (String plotName : config.getConfigurationSection(s + "plots").getKeys(false)) {
              if (config.isSet(s + "plots." + plotName + ".maxpoint") && config.isSet(s + "plots." + plotName + ".minpoint")) {
                ArenaPlot buildPlot = new ArenaPlot();
                buildPlot.setCuboid(new Cuboid(MinigameUtils.getLocation(config.getString(s + "plots." + plotName + ".minpoint")),
                        MinigameUtils.getLocation(config.getString(s + "plots." + plotName + ".maxpoint"))));
                buildPlot.fullyResetPlot();
                arena.getPlotManager().addBuildPlot(buildPlot);
              } else {
                System.out.println("Non configured plot instances found for arena " + ID);
                arena.setReady(false);
              }
            }
          } else {
            System.out.println("Non configured plots in arena " + ID);
            arena.setReady(false);
          }
        } else {
          System.out.print("Instance " + ID + " doesn't contains plots!");
          arena.setReady(false);
        }
        arena.setReady(config.getBoolean("instances." + ID + ".isdone"));
        ArenaRegistry.registerArena(arena);
        arena.start();
      }
      Main.debug("Arenas registration completed", System.currentTimeMillis());
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

}
