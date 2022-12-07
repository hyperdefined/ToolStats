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
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Date;
import java.util.List;

public class PickupItem implements Listener {

    private final ToolStats toolStats;

    public PickupItem(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
                return;
            }
            Item item = event.getItem();
            if (item.getType() == EntityType.DROPPED_ITEM) {
                ItemStack itemStack = event.getItem().getItemStack();
                ItemMeta meta = itemStack.getItemMeta();
                if (meta == null) {
                    return;
                }
                PersistentDataContainer container = meta.getPersistentDataContainer();
                if (itemStack.getType() == Material.ELYTRA) {
                    // the elytra has the new key, set the lore to it
                    if (container.has(toolStats.newElytra, PersistentDataType.INTEGER)) {
                        ItemStack newElytra = addLore(itemStack, (Player) event.getEntity());
                        if (newElytra != null) {
                            item.setItemStack(newElytra);
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds "looted by" tags for elytras.
     *
     * @param itemStack The elytra to add lore to.
     * @param owner     The player who found it.
     */
    private ItemStack addLore(ItemStack itemStack, Player owner) {
        ItemStack finalItem = itemStack.clone();
        ItemMeta meta = finalItem.getItemMeta();
        if (meta == null) {
            return null;
        }
        long timeCreated = System.currentTimeMillis();
        Date finalDate = new Date(timeCreated);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(toolStats.timeCreated, PersistentDataType.LONG, timeCreated);
        container.set(toolStats.genericOwner, new UUIDDataType(), owner.getUniqueId());
        container.remove(toolStats.newElytra);

        String formattedDate = toolStats.numberFormat.formatDate(finalDate);
        List<String> newLore = toolStats.itemLore.addNewOwner(meta, owner.getName(), formattedDate, "LOOTED");

        if (toolStats.config.getBoolean("enabled.elytra-tag")) {
            meta.setLore(newLore);
        }
        finalItem.setItemMeta(meta);
        return finalItem;
    }
}
