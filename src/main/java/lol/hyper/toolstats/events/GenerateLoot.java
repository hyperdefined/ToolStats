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
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GenerateLoot implements Listener {

    private final ToolStats toolStats;
    public final String[] validItems = {
            "pickaxe", "sword", "shovel", "axe", "hoe", "bow", "helmet", "chestplate", "leggings", "boots", "fishing"
    };
    private final SimpleDateFormat format = new SimpleDateFormat("M/dd/yyyy", Locale.ENGLISH);

    public GenerateLoot(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onGenerateLoot(LootGenerateEvent event) {
        InventoryHolder inventoryHolder = event.getInventoryHolder();
        if (inventoryHolder == null) {
            return;
        }
        Inventory chest = inventoryHolder.getInventory();
        Bukkit.getScheduler().runTaskLater(toolStats, () -> {
            Player player = (Player) chest.getViewers().get(0);
            for (int i = 0; i < chest.getContents().length; i++) {
                ItemStack itemStack = chest.getItem(i);
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    continue;
                }
                String name = itemStack.getType().toString().toLowerCase(Locale.ROOT);
                for (String x : validItems) {
                    if (name.contains(x)) {
                        chest.setItem(i, addLore(itemStack, player));
                    }
                }
            }

        },1);
    }

    private ItemStack addLore(ItemStack itemStack, Player owner) {
        ItemStack newItem = itemStack.clone();
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
        long timeCreated = System.currentTimeMillis();
        Date finalDate = new Date(timeCreated);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(toolStats.timeCreated, PersistentDataType.LONG, timeCreated);
        container.set(toolStats.genericOwner, new UUIDDataType(), owner.getUniqueId());

        String foundByLoreRaw = toolStats.getLoreFromConfig("looted.found-by", true);
        String foundOnLoreRaw = toolStats.getLoreFromConfig("looted.found-on", true);

        if (foundByLoreRaw == null || foundOnLoreRaw == null) {
            toolStats.logger.warning("There is no lore message for messages.looted!");
            return null;
        }

        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
        } else {
            lore = new ArrayList<>();
        }
        if (toolStats.checkConfig(newItem, "looted-tag")) {
            lore.add(foundOnLoreRaw.replace("{date}", format.format(finalDate)));
            lore.add(foundByLoreRaw.replace("{player}", owner.getName()));
        }
        meta.setLore(lore);
        newItem.setItemMeta(meta);
        return newItem;
    }
}
