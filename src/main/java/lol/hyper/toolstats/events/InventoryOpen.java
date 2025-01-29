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
import lol.hyper.toolstats.tools.UUIDDataType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class InventoryOpen implements Listener {

    private final ToolStats toolStats;

    public InventoryOpen(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Inventory inventory = event.getInventory();
        Player player = (Player) event.getPlayer();
        for (ItemStack itemStack : inventory) {
            if (itemStack == null) {
                continue;
            }
            // ignore items that are not the right type
            if (!toolStats.itemChecker.isValidItem(itemStack.getType())) {
                continue;
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                continue;
            }
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();

            if (toolStats.config.getBoolean("tokens.enabled")) {
                // if the token system is on and the item doesn't have stat keys
                if (toolStats.itemChecker.keyCheck(container) && !container.has(toolStats.tokenType)) {
                    // add the tokens
                    String newTokens = toolStats.itemChecker.addTokensToExisting(itemStack);
                    if (newTokens == null) {
                        return;
                    }
                    container.set(toolStats.tokenApplied, PersistentDataType.STRING, newTokens);
                    itemStack.setItemMeta(itemMeta);
                }
            }

            // generate a hash if the item doesn't have one (and enabled)
            // if hashes are disabled and the item has one, remove it.
            if (toolStats.config.getBoolean("generate-hash-for-items")) {
                if (!container.has(toolStats.hash, PersistentDataType.STRING)) {
                    UUID owner = null;
                    // get the current owner if there is one.
                    if (container.has(toolStats.itemOwner, new UUIDDataType())) {
                        owner = container.get(toolStats.itemOwner, new UUIDDataType());
                    }
                    // if there is no owner, use the player holding it
                    if (owner == null) {
                        owner = player.getUniqueId();
                    }
                    Long timestamp = container.get(toolStats.timeCreated, PersistentDataType.LONG);
                    if (timestamp == null) {
                        // if there is no time created, use now
                        timestamp = System.currentTimeMillis();
                    }
                    String hash = toolStats.hashMaker.makeHash(itemStack.getType(), owner, timestamp);
                    container.set(toolStats.hash, PersistentDataType.STRING, hash);
                    itemStack.setItemMeta(itemMeta);
                }
            } else {
                // if hashes are disabled but the item has one, remove it.
                if (container.has(toolStats.hash, PersistentDataType.STRING)) {
                    container.remove(toolStats.hash);
                    itemStack.setItemMeta(itemMeta);
                }
            }
        }
    }
}
