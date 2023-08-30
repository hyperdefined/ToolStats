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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

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

        Player player = (Player) event.getPlayer();

        Inventory inventory = event.getInventory();
        for (ItemStack itemStack : inventory) {
            if (itemStack == null) {
                continue;
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                continue;
            }
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            // ignore any items that already have the origin tag
            if (container.has(toolStats.originType, PersistentDataType.INTEGER)) {
                continue;
            }
            // ignore items that are not the right type
            if (!ItemChecker.isValidItem(itemStack.getType())) {
                continue;
            }

            ItemMeta newMeta = getOrigin(itemMeta, itemStack.getType() == Material.ELYTRA);
            if (newMeta == null) {
                continue;
            }
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    itemStack.setItemMeta(newMeta);
                }
            };
            toolStats.scheduleEntity(runnable, player, 1);
        }
    }

    /**
     * Determine an item's origin based on lore.
     *
     * @param itemMeta The item's meta.
     * @param elytra   If they item is an elytra.
     * @return The new item meta with the new origin tag. Returns null if origin cannot be determined.
     */
    private ItemMeta getOrigin(ItemMeta itemMeta, boolean elytra) {
        List<String> lore;
        if (!itemMeta.hasLore()) {
            return null;
        }
        lore = itemMeta.getLore();
        Integer origin = null;

        for (String line : lore) {
            // this is the worst code I have ever written
            String createdBy = toolStats.getLoreFromConfig("created.created-by", false);
            String createdOn = toolStats.getLoreFromConfig("created.created-on", false);
            String caughtBy = toolStats.getLoreFromConfig("fished.caught-by", false);
            String lootedBy = toolStats.getLoreFromConfig("looted.looted-by", false);
            String foundBy = toolStats.getLoreFromConfig("looted.found-by", false);
            String tradedBy = toolStats.getLoreFromConfig("traded.traded-by", false);

            if (createdBy != null && line.contains(createdBy)) {
                origin = 0;
            }
            if (createdOn != null && line.contains(createdOn)) {
                origin = 0;
            }
            if (caughtBy != null && line.contains(caughtBy)) {
                origin = 5;
            }
            if (lootedBy != null && line.contains(lootedBy)) {
                origin = 2;
            }
            // because the config changed, "found-by" was being used for ALL looted items
            // this includes elytras, so we have to check for this mistake
            if (foundBy != null && line.contains(foundBy)) {
                if (elytra) {
                    origin = 4;
                } else {
                    origin = 5;
                }
            }
            if (tradedBy != null && line.contains(tradedBy)) {
                origin = 3;
            }
        }

        if (origin == null) {
            return null;
        }

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(toolStats.originType, PersistentDataType.INTEGER, origin);
        return itemMeta;
    }
}
