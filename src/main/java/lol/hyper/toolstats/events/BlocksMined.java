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

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lol.hyper.toolstats.ToolStats;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class BlocksMined implements Listener {

    private final ToolStats toolStats;

    public BlocksMined(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        PlayerInventory inventory = player.getInventory();
        ItemStack heldItem = inventory.getItemInMainHand();
        Block block = event.getBlock();

        if (block.getType() == Material.CHEST) {
            toolStats.playerInteract.openedChests.put(block, player);
            Bukkit.getGlobalRegionScheduler().runDelayed(toolStats, scheduledTask -> toolStats.playerInteract.openedChests.remove(block), 20);
        }

        // only check certain items
        if (!toolStats.itemChecker.isMineTool(heldItem.getType())) {
            return;
        }

        if (heldItem.getType().toString().toLowerCase(Locale.ROOT).contains("hoe")) {
            // player is breaking crops with a hoe
            if (block.getBlockData() instanceof Ageable) {
                updateCropsMined(heldItem, (Ageable) block.getBlockData());
            }
        } else {
            // update the blocks mined
            updateBlocksMined(heldItem);
        }
    }

    private void updateBlocksMined(ItemStack playerTool) {
        ItemMeta meta = playerTool.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(playerTool + " does NOT have any meta! Unable to update stats.");
            return;
        }
        // read the current stats from the item
        // if they don't exist, then start from 0
        Integer blocksMined = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.genericMined, PersistentDataType.INTEGER)) {
            blocksMined = container.get(toolStats.genericMined, PersistentDataType.INTEGER);
        }

        if (blocksMined == null) {
            blocksMined = 0;
            toolStats.logger.warning(playerTool + " does not have valid generic-mined set! Resting to zero. This should NEVER happen.");
        }

        container.set(toolStats.genericMined, PersistentDataType.INTEGER, blocksMined + 1);

        // do we add the lore based on the config?
        if (toolStats.configTools.checkConfig(playerTool.getType(), "blocks-mined")) {
            String oldBlocksMinedFormatted = toolStats.numberFormat.formatInt(blocksMined);
            String newBlocksMinedFormatted = toolStats.numberFormat.formatInt(blocksMined + 1);
            Component oldLine = toolStats.configTools.formatLore("blocks-mined", "{blocks}", oldBlocksMinedFormatted);
            Component newLine = toolStats.configTools.formatLore("blocks-mined", "{blocks}", newBlocksMinedFormatted);
            if (oldLine == null || newLine == null) {
                return;
            }
            List<Component> newLore = toolStats.itemLore.updateItemLore(meta, oldLine, newLine);
            meta.lore(newLore);
        }
        playerTool.setItemMeta(meta);
    }

    private void updateCropsMined(ItemStack playerTool, Ageable block) {
        // ignore crops that are not fully grown
        if (block.getAge() != block.getMaximumAge()) {
            return;
        }

        ItemMeta meta = playerTool.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(playerTool + " does NOT have any meta! Unable to update stats.");
            return;
        }
        // read the current stats from the item
        // if they don't exist, then start from 0
        Integer cropsMined = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.cropsHarvested, PersistentDataType.INTEGER)) {
            cropsMined = container.get(toolStats.cropsHarvested, PersistentDataType.INTEGER);
        }

        if (cropsMined == null) {
            cropsMined = 0;
            toolStats.logger.warning(playerTool + " does not have valid crops-mined set! Resting to zero. This should NEVER happen.");
        }

        container.set(toolStats.cropsHarvested, PersistentDataType.INTEGER, cropsMined + 1);

        // do we add the lore based on the config?
        if (toolStats.configTools.checkConfig(playerTool.getType(), "blocks-mined")) {
            String oldCropsMinedFormatted = toolStats.numberFormat.formatInt(cropsMined);
            String newCropsMinedFormatted = toolStats.numberFormat.formatInt(cropsMined + 1);
            Component oldLine = toolStats.configTools.formatLore("crops-harvested", "{crops}", oldCropsMinedFormatted);
            Component newLine = toolStats.configTools.formatLore("crops-harvested", "{crops}", newCropsMinedFormatted);
            if (oldLine == null || newLine == null) {
                return;
            }
            List<Component> newLore = toolStats.itemLore.updateItemLore(meta, oldLine, newLine);
            meta.lore(newLore);
        }
        playerTool.setItemMeta(meta);
    }
}
