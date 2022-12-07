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
import lol.hyper.toolstats.tools.ItemChecker;
import org.bukkit.GameMode;
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

import java.util.List;

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
        // if the player mines something with their fist
        PlayerInventory inventory = player.getInventory();
        ItemStack heldItem = inventory.getItemInMainHand();
        // only check certain items
        if (!ItemChecker.isMineTool(heldItem.getType())) {
            return;
        }
        // update the blocks mined
        updateBlocksMined(heldItem);
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

        blocksMined++;
        container.set(toolStats.genericMined, PersistentDataType.INTEGER, blocksMined);

        String blocksMinedFormatted = toolStats.numberFormat.formatInt(blocksMined);
        List<String> newLore = toolStats.itemLore.addItemLore(meta, "{blocks}", blocksMinedFormatted, "blocks-mined");

        // if the list returned null, don't add it
        if (newLore == null) {
            return;
        }

        // do we add the lore based on the config?
        if (toolStats.checkConfig(playerTool, "blocks-mined")) {
            meta.setLore(newLore);
        }
        playerTool.setItemMeta(meta);
    }
}
