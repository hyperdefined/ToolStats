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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VillagerTrade implements Listener {

    private final ToolStats toolStats;
    public final String[] validItems = {
            "pickaxe", "sword", "shovel", "axe", "hoe", "bow", "helmet", "chestplate", "leggings", "boots", "fishing"
    };
    private final SimpleDateFormat format = new SimpleDateFormat("M/dd/yyyy", Locale.ENGLISH);

    public VillagerTrade(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onTrade(InventoryClickEvent event) {
        if (event.isCancelled() || event.getCurrentItem() == null) {
            return;
        }
        Inventory inventory = event.getClickedInventory();
        if (inventory instanceof MerchantInventory) {
            if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                ItemStack item = event.getCurrentItem();
                for (String x : validItems) {
                    if (item.getType().toString().toLowerCase(Locale.ROOT).contains(x)) {
                        ItemStack newItem = addLore(item, (Player) event.getWhoClicked());
                        if (newItem == null) {
                            return;
                        }
                        Bukkit.getScheduler().runTaskLater(toolStats, ()-> event.setCurrentItem(newItem), 5);
                    }
                }
            }
        }
    }

    private ItemStack addLore(ItemStack itemStack, Player owner) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
        long timeCreated = System.currentTimeMillis();
        Date finalDate = new Date(timeCreated);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(toolStats.timeCreated, PersistentDataType.LONG, timeCreated);
        container.set(toolStats.genericOwner, new UUIDDataType(), owner.getUniqueId());

        String tradedByLoreRaw = toolStats.getLoreFromConfig("traded.traded-by", true);
        String tradedOnLoreRaw = toolStats.getLoreFromConfig("traded.traded-on", true);

        if (tradedByLoreRaw == null || tradedOnLoreRaw == null) {
            toolStats.logger.warning("There is no lore message for messages.traded!");
            return null;
        }

        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
        } else {
            lore = new ArrayList<>();
        }
        if (toolStats.checkConfig(itemStack, "traded-tag")) {
            lore.add(tradedOnLoreRaw.replace("{date}", format.format(finalDate)));
            lore.add(tradedByLoreRaw.replace("{player}", owner.getName()));
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
