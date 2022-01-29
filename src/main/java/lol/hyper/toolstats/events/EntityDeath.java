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
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EntityDeath implements Listener {

    private final ToolStats toolStats;
    private final String droppedLore = ChatColor.GRAY + "Dropped by: " + ChatColor.DARK_GRAY + "X";

    public EntityDeath(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player) {
            return;
        }
        UUID livingEntityUUID = event.getEntity().getUniqueId();
        if (toolStats.mobKill.trackedMobs.contains(livingEntityUUID)) {
            for (ItemStack current : event.getDrops()) {
                String name = current.getType().toString().toLowerCase(Locale.ROOT);
                for (String item : toolStats.craftItem.validItems) {
                    if (name.contains(item)) {
                        addLore(current, livingEntity.getName());
                    }
                }
            }
            toolStats.mobKill.trackedMobs.remove(livingEntityUUID);
        }
    }

    private void addLore(ItemStack itemStack, String mob) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }
        boolean hasTag = false;
        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
            for (int x = 0; x < lore.size(); x++) {
                if (lore.get(x).contains("Dropped by")) {
                    // replace existing tag
                    lore.set(x, droppedLore.replace("X", mob));
                    hasTag = true;
                }
            }
        } else {
            // if the item has no lore, create a new list and add the string
            lore = new ArrayList<>();
        }
        if (!hasTag) {
            lore.add(droppedLore.replace("X", mob));
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }
}
