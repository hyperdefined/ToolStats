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
import lol.hyper.toolstats.tools.UUIDDataType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VillagerTrade implements Listener {

    private final ToolStats toolStats;

    public VillagerTrade(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onTrade(InventoryClickEvent event) {
        if (event.isCancelled() || event.getCurrentItem() == null) {
            return;
        }
        Inventory inventory = event.getClickedInventory();
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        // only check villager inventories
        if (inventory instanceof MerchantInventory) {
            // only check the result slot (the item you receive)
            if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                ItemStack item = event.getCurrentItem();
                // only check items we want
                if (ItemChecker.isValidItem(item.getType())) {
                    // if the player shift clicks, show the warning
                    if (event.isShiftClick()) {
                        String configMessage = toolStats.config.getString("messages.shift-click-warning.trading");
                        if (configMessage != null) {
                            event.getWhoClicked().sendMessage(ChatColor.translateAlternateColorCodes('&', configMessage));
                        }
                    }
                    ItemStack newItem = addLore(item, player);
                    if (newItem != null) {
                        // this gets delayed since villager inventories suck for no reason
                        Bukkit.getScheduler().runTaskLater(toolStats, () -> event.setCurrentItem(newItem), 5);
                    }
                }
            }
        }
    }

    /**
     * Adds "traded by" tags to item.
     *
     * @param itemStack The item to add lore.
     * @param owner     The player who traded.
     * @return The item with lore.
     */
    private ItemStack addLore(ItemStack itemStack, Player owner) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
        long timeCreated = System.currentTimeMillis();
        Date finalDate = new Date(timeCreated);
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(toolStats.timeCreated, PersistentDataType.LONG) || container.has(toolStats.genericOwner, PersistentDataType.LONG)) {
            return null;
        }

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
            lore.add(tradedOnLoreRaw.replace("{date}", toolStats.dateFormat.format(finalDate)));
            lore.add(tradedByLoreRaw.replace("{player}", owner.getName()));
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
