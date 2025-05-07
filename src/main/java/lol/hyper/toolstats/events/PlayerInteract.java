/*
 * This file is part of ToolStats.
 *
 * ToolStats is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ToolStats is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ToolStats.  If not, see <https://www.gnu.org/licenses/>.
 */

package lol.hyper.toolstats.events;

import lol.hyper.toolstats.ToolStats;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerInteract implements Listener {

    private final ToolStats toolStats;

    public final Map<Block, Player> openedChests = new HashMap<>();
    public final Map<StorageMinecart, Player> openedMineCarts = new HashMap<>();

    public PlayerInteract(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && !toolStats.config.getBoolean("allow-creative")) {
            return;
        }
        // store when a player opens a chest
        if (block.getType() != Material.AIR && block.getType() == Material.CHEST) {
            openedChests.put(block, player);
            Bukkit.getGlobalRegionScheduler().runDelayed(toolStats, scheduledTask -> openedChests.remove(block), 20);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && !toolStats.config.getBoolean("allow-creative")) {
            return;
        }
        // store when a player opens a minecart
        if (clicked.getType() == EntityType.CHEST_MINECART) {
            StorageMinecart storageMinecart = (StorageMinecart) clicked;
            openedMineCarts.put(storageMinecart, player);
            Bukkit.getGlobalRegionScheduler().runDelayed(toolStats, scheduledTask -> openedMineCarts.remove(storageMinecart), 20);
        }
    }
}
