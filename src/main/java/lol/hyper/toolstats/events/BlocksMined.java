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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class BlocksMined implements Listener {

    private final ToolStats toolStats;

    public BlocksMined(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        // if the player mines something with their fist
        ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
        if (heldItem == null || heldItem.getType() == Material.AIR) {
            return;
        }
        // only check certain items
        if (!ItemChecker.isMineTool(heldItem.getType())) {
            return;
        }
        // if it's an item we want, update the stats
        updateBlocksMined(heldItem);
    }

    private void updateBlocksMined(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }
        // read the current stats from the item
        // if they don't exist, then start from 0
        Integer blocksMined = null;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.genericMined, PersistentDataType.INTEGER)) {
            blocksMined = container.get(toolStats.genericMined, PersistentDataType.INTEGER);
        }
        if (blocksMined == null) {
            blocksMined = 0;
        }

        blocksMined++;
        container.set(toolStats.genericMined, PersistentDataType.INTEGER, blocksMined);

        String configLore = toolStats.getLoreFromConfig("blocks-mined", false);
        String configLoreRaw = toolStats.getLoreFromConfig("blocks-mined", true);

        if (configLore == null || configLoreRaw == null) {
            toolStats.logger.warning("There is no lore message for messages.blocks-mined!");
            return;
        }

        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            boolean hasLore = false;
            // we do a for loop like this, we can keep track of index
            // this doesn't mess the lore up of existing items
            for (int x = 0; x < lore.size(); x++) {
                if (lore.get(x).contains(configLore)) {
                    hasLore = true;
                    lore.set(x, configLoreRaw.replace("{blocks}", toolStats.commaFormat.format(blocksMined)));
                    break;
                }
            }
            // if the item has lore but doesn't have the tag, add it
            if (!hasLore) {
                lore.add(configLoreRaw.replace("{blocks}", toolStats.commaFormat.format(blocksMined)));
            }
        } else {
            // if the item has no lore, create a new list and add the string
            lore = new ArrayList<>();
            lore.add(configLoreRaw.replace("{blocks}", toolStats.commaFormat.format(blocksMined)));
        }
        // do we add the lore based on the config?
        if (toolStats.checkConfig(itemStack, "blocks-mined")) {
            meta.setLore(lore);
        }
        itemStack.setItemMeta(meta);
    }
}
