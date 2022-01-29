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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.*;

public class CraftItem implements Listener {

    private final ToolStats toolStats;
    public final String[] validItems = {
            "pickaxe", "sword", "shovel", "axe", "hoe", "bow", "helmet", "chestplate", "leggings", "boots", "fishing"
    };
    private final String timeCreatedLore = ChatColor.GRAY + "Crafted on: " + ChatColor.DARK_GRAY + "X";
    private final String ownerLore = ChatColor.GRAY + "Crafted by: " + ChatColor.DARK_GRAY + "X";
    private final SimpleDateFormat format = new SimpleDateFormat("M/dd/yyyy", Locale.ENGLISH);

    public CraftItem(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack itemStack = event.getCurrentItem();
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return;
        }
        String name = itemStack.getType().toString().toLowerCase(Locale.ROOT);
        for (String x : validItems) {
            if (name.contains(x)) {
                event.setCurrentItem(addLore(itemStack, player));
            }
        }
    }

    private ItemStack addLore(ItemStack itemStack, Player owner) {
        ItemStack newItem = itemStack.clone();
        ItemMeta meta = newItem.getItemMeta();
        if (meta == null) {
            return null;
        }
        long timeCreated = System.currentTimeMillis();
        Date finalDate = new Date(timeCreated);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(toolStats.craftedTime, PersistentDataType.LONG, timeCreated);
        container.set(toolStats.craftedOwner, new UUIDDataType(), owner.getUniqueId());
        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
        } else {
            lore = new ArrayList<>();
        }
        if (toolStats.checkConfig(itemStack, "crafted-date")) {
            lore.add(timeCreatedLore.replace("X", format.format(finalDate)));
        }
        if (toolStats.checkConfig(itemStack, "crafted-by")) {
            lore.add(ownerLore.replace("X", owner.getName()));
        }
        meta.setLore(lore);
        newItem.setItemMeta(meta);
        return newItem;
    }
}
