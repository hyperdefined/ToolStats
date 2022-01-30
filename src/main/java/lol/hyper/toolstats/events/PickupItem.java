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
import lol.hyper.toolstats.UUIDDataType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PickupItem implements Listener {

    private final ToolStats toolStats;
    private final String FOUND_BY = ChatColor.GRAY + "Found by: " + ChatColor.DARK_GRAY + "X";
    private final String FOUND_ON = ChatColor.GRAY + "Found on: " + ChatColor.DARK_GRAY + "X";
    private final SimpleDateFormat format = new SimpleDateFormat("M/dd/yyyy", Locale.ENGLISH);

    public PickupItem(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            ItemStack itemStack = event.getItem().getItemStack();
            if (itemStack.getType() == Material.ELYTRA) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta == null) {
                    return;
                }
                PersistentDataContainer container = meta.getPersistentDataContainer();
                // the elytra has the new key, set the lore to it
                if (container.has(toolStats.newElytra, PersistentDataType.INTEGER)) {
                    container.remove(toolStats.newElytra);
                    addLore(itemStack, (Player) event.getEntity());
                }
            }
        }
    }

    private void addLore(ItemStack itemStack, Player owner) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }
        long timeCreated = System.currentTimeMillis();
        Date finalDate = new Date(timeCreated);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(toolStats.timeCreated, PersistentDataType.LONG, timeCreated);
        container.set(toolStats.genericOwner, new UUIDDataType(), owner.getUniqueId());
        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
        } else {
            lore = new ArrayList<>();
        }
        if (toolStats.config.getBoolean("enabled.elytra-tag")) {
            lore.add(FOUND_ON.replace("X", format.format(finalDate)));
            lore.add(FOUND_BY.replace("X", owner.getName()));
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }
}
