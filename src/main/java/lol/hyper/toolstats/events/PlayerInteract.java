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
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayerInteract implements Listener {

    private final ToolStats toolStats;

    public final List<Block> openedChests = new ArrayList<>();
    public final List<StorageMinecart> openedMineCarts = new ArrayList<>();
    public final List<Inventory> chestInventories = new ArrayList<>();
    public final List<Inventory> mineCartChestInventories = new ArrayList<>();

    public PlayerInteract(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Player player = event.getPlayer();
        if (!toolStats.configTools.checkWorld(player.getWorld().getName())) {
            return;
        }
        if (player.getGameMode() == GameMode.CREATIVE && !toolStats.config.getBoolean("allow-creative")) {
            return;
        }
        // store when a player opens a chest
        BlockState state = block.getState();
        if (state instanceof InventoryHolder holder) {
            Inventory holderInventory = holder.getInventory();
            openedChests.add(block);
            chestInventories.add(holderInventory);
            Bukkit.getGlobalRegionScheduler().runDelayed(toolStats, _ -> openedChests.remove(block), 20);
        }
        // player right-clicked a log
        String blockType = block.getType().toString().toLowerCase(Locale.ROOT);
        if (blockType.endsWith("_log") && !blockType.contains("stripped")) {
            PlayerInventory playerInventory = player.getInventory();
            ItemStack axe = toolStats.itemChecker.getAxe(playerInventory);

            // not holding an axe
            if (axe == null) {
                return;
            }

            ItemMeta newAxe = toolStats.itemLore.updateLogsStripped(axe, 1);
            if (newAxe != null) {
                boolean isMain = playerInventory.getItemInMainHand().getType().toString().endsWith("_AXE");
                boolean isOffHand = playerInventory.getItemInOffHand().getType().toString().endsWith("_AXE");
                if (isMain && isOffHand) {
                    playerInventory.getItemInMainHand().setItemMeta(newAxe);
                } else if (isMain) {
                    playerInventory.getItemInMainHand().setItemMeta(newAxe);
                } else if (isOffHand) {
                    playerInventory.getItemInOffHand().setItemMeta(newAxe);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        Entity clicked = event.getRightClicked();
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && !toolStats.config.getBoolean("allow-creative")) {
            return;
        }
        // store when a player opens a minecart
        if (clicked.getType() == EntityType.CHEST_MINECART) {
            StorageMinecart storageMinecart = (StorageMinecart) clicked;
            Inventory mineCartInventory = storageMinecart.getInventory();
            mineCartChestInventories.add(mineCartInventory);
            openedMineCarts.add(storageMinecart);
            Bukkit.getGlobalRegionScheduler().runDelayed(toolStats, _ -> openedMineCarts.remove(storageMinecart), 20);
        }
    }
}
