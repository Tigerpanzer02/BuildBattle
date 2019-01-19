/*
 * BuildBattle - Ultimate building competition minigame
 * Copyright (C) 2019  Plajer's Lair - maintained by Plajer and Tigerpanzer
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

package pl.plajer.buildbattle.events;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import pl.plajer.buildbattle.Main;
import pl.plajer.buildbattle.arena.ArenaRegistry;
import pl.plajer.buildbattle.arena.impl.BaseArena;
import pl.plajerlair.core.services.exception.ReportedException;

/**
 * @author Plajer
 * <p>
 * Created at 31.12.2018
 */
public class ChatEvents implements Listener {

  private Main plugin;

  public ChatEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onChatIngame(AsyncPlayerChatEvent event) {
    try {
      BaseArena arena = ArenaRegistry.getArena(event.getPlayer());
      if (arena == null) {
        for (BaseArena loopArena : ArenaRegistry.getArenas()) {
          for (Player player : loopArena.getPlayers()) {
            event.getRecipients().remove(player);
          }
        }
        return;
      }
      event.getRecipients().clear();
      event.getRecipients().addAll(new ArrayList<>(arena.getPlayers()));
    } catch (Exception ex) {
      new ReportedException(plugin, ex);
    }
  }

}
