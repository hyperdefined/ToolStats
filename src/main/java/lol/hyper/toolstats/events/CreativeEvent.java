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
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Date;
import java.util.List;

public class CreativeEvent implements Listener {

    private final ToolStats toolStats;

    public CreativeEvent(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onCreativeEvent(InventoryCreativeEvent event) {
        Player player = (Player) event.getWhoClicked();
        // make sure they are in creative mode
        if (player.getGameMode() != GameMode.CREATIVE) {
            return;
        }
        ItemStack spawnedItem = event.getCursor();
        if (!toolStats.itemChecker.isValidItem(spawnedItem.getType())) {
            return;
        }

        ItemMeta spawnedItemMeta = spawnedItem.getItemMeta();
        if (spawnedItemMeta == null) {
            return;
        }
        // if the item already has an origin set, don't add it again
        // this is needed since you can spam click an item and the event will fire again
        PersistentDataContainer container = spawnedItemMeta.getPersistentDataContainer();
        if (container.has(toolStats.originType, PersistentDataType.INTEGER)) {
            return;
        }

        // add the tags to the item
        ItemStack newItem = addLore(spawnedItem, player);
        if (newItem != null) {
            event.setCursor(newItem);
        }
    }

    /**
     * Adds tags to newly spawned items in creative.
     *
     * @param spawnedItem The item.
     */
    private ItemStack addLore(ItemStack spawnedItem, Player owner) {
        ItemStack newSpawnedItem = spawnedItem.clone();
        ItemMeta meta = newSpawnedItem.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(newSpawnedItem + " does NOT have any meta! Unable to update stats.");
            return null;
        }

        long timeCreated = System.currentTimeMillis();
        Date finalDate = new Date(timeCreated);
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // only make the hash if it's enabled
        if (toolStats.config.getBoolean("generate-hash-for-items")) {
            String hash = toolStats.hashMaker.makeHash(spawnedItem.getType(), owner.getUniqueId(), timeCreated);
            container.set(toolStats.hash, PersistentDataType.STRING, hash);
        }

        container.set(toolStats.timeCreated, PersistentDataType.LONG, timeCreated);
        container.set(toolStats.genericOwner, new UUIDDataType(), owner.getUniqueId());
        container.set(toolStats.originType, PersistentDataType.INTEGER, 6);

        String formattedDate = toolStats.numberFormat.formatDate(finalDate);
        List<String> newLore = toolStats.itemLore.addNewOwner(meta, owner.getName(), formattedDate);

        if (toolStats.checkConfig(newSpawnedItem.getType(), "spawned-in")) {
            meta.setLore(newLore);
        }

        newSpawnedItem.setItemMeta(meta);
        return newSpawnedItem;
    }
}
