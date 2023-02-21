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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class EntityDeath implements Listener {

    private final ToolStats toolStats;

    public EntityDeath(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player) {
            return;
        }
        UUID livingEntityUUID = event.getEntity().getUniqueId();
        // if it's a mob we are tracking that matters
        if (toolStats.mobKill.trackedMobs.contains(livingEntityUUID)) {
            for (int i = 0; i < event.getDrops().size(); i++) {
                ItemStack droppedItem = event.getDrops().get(i);
                ItemMeta droppedItemMeta = droppedItem.getItemMeta();
                if (droppedItemMeta != null) {
                    PersistentDataContainer container = droppedItemMeta.getPersistentDataContainer();
                    if (container.has(toolStats.timeCreated, PersistentDataType.LONG)) {
                        continue; // ignore any items that have our tags
                    }

                }
                if (ItemChecker.isValidItem(droppedItem.getType())) {
                    ItemStack newItem = addLore(droppedItem, livingEntity.getName());
                    if (newItem != null) {
                        event.getDrops().set(i, newItem);
                    }
                }
            }
            toolStats.mobKill.trackedMobs.remove(livingEntityUUID);
        }
    }

    /**
     * Adds "drop by" tag to item.
     *
     * @param oldItem The item to add lore to.
     * @param mob     The mob or player name.
     */
    private ItemStack addLore(ItemStack oldItem, String mob) {
        ItemStack newItem = oldItem.clone();
        ItemMeta meta = newItem.getItemMeta();
        if (meta == null) {
            return null;
        }

        List<String> newLore = toolStats.itemLore.addItemLore(meta, "{name}", mob, "dropped-by");

        if (toolStats.config.getBoolean("enabled.dropped-by")) {
            meta.setLore(newLore);
        }
        newItem.setItemMeta(meta);
        return newItem;
    }
}
