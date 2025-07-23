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
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
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
        if (entity instanceof Player player) {
            if (player.getGameMode() == GameMode.CREATIVE && !toolStats.config.getBoolean("allow-creative")) {
                return;
            }
            Item item = event.getItem();
            if (item.getType() == EntityType.ITEM) {
                ItemStack itemStack = event.getItem().getItemStack();
                ItemMeta meta = itemStack.getItemMeta();
                if (meta == null) {
                    return;
                }
                PersistentDataContainer container = meta.getPersistentDataContainer();
                if (itemStack.getType() == Material.ELYTRA) {
                    // the elytra has the new key, set the lore to it
                    if (container.has(toolStats.newElytra, PersistentDataType.INTEGER)) {
                        ItemStack newElytra = addElytraOrigin(itemStack, (Player) event.getEntity());
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
    private ItemStack addElytraOrigin(ItemStack itemStack, Player owner) {
        ItemStack finalItem = itemStack.clone();
        ItemMeta meta = finalItem.getItemMeta();
        if (meta == null) {
            return null;
        }
        long timeCreated = System.currentTimeMillis();
        Date finalDate;
        if (toolStats.config.getBoolean("normalize-time-creation")) {
            finalDate = toolStats.numberFormat.normalizeTime(timeCreated);
            timeCreated = finalDate.getTime();
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (!toolStats.config.getBoolean("enabled.elytra-tag")) {
            return null;
        }

        // only make the hash if it's enabled
        if (toolStats.config.getBoolean("generate-hash-for-items")) {
            String hash = toolStats.hashMaker.makeHash(finalItem.getType(), owner.getUniqueId(), timeCreated);
            container.set(toolStats.hash, PersistentDataType.STRING, hash);
        }

        // get the current lore the item
        List<Component> lore;
        if (meta.hasLore()) {
            lore = meta.lore();
        } else {
            lore = new ArrayList<>();
        }

        container.set(toolStats.timeCreated, PersistentDataType.LONG, timeCreated);
        container.set(toolStats.itemOwner, new UUIDDataType(), owner.getUniqueId());
        container.set(toolStats.originType, PersistentDataType.INTEGER, 4);
        container.remove(toolStats.newElytra);

        Component creationDate = toolStats.itemLore.formatCreationTime(timeCreated, 4, finalItem);
        if (creationDate != null) {
            container.set(toolStats.timeCreated, PersistentDataType.LONG, timeCreated);
            container.set(toolStats.originType, PersistentDataType.INTEGER, 4);
            lore.add(creationDate);
            meta.lore(lore);
        }

        Component itemOwner = toolStats.itemLore.formatOwner(owner.getName(), 4, finalItem);
        if (itemOwner != null) {
            container.set(toolStats.itemOwner, new UUIDDataType(), owner.getUniqueId());
            container.set(toolStats.originType, PersistentDataType.INTEGER, 4);
            lore.add(itemOwner);
            meta.lore(lore);
        }

        finalItem.setItemMeta(meta);
        return finalItem;
    }
}
