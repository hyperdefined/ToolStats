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
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlayerFish implements Listener {

    private final ToolStats toolStats;

    public PlayerFish(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFish(PlayerFishEvent event) {
        if (event.isCancelled()) {
            return;
        }
        // only listen to when a player catches a fish
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && !toolStats.config.getBoolean("allow-creative")) {
            return;
        }

        ItemStack fishingRod = toolStats.itemChecker.getFishingRod(player.getInventory());
        // player swapped items?
        if (fishingRod == null) {
            return;
        }

        // update the fishing rod!
        ItemMeta newFishingRod = toolStats.itemLore.updateFishCaught(fishingRod, 1);
        if (newFishingRod != null) {
            PlayerInventory inventory = player.getInventory();
            boolean isMain = inventory.getItemInMainHand().getType() == Material.FISHING_ROD;
            boolean isOffHand = inventory.getItemInOffHand().getType() == Material.FISHING_ROD;
            if (isMain && isOffHand) {
                inventory.getItemInMainHand().setItemMeta(newFishingRod);
            } else if (isMain) {
                inventory.getItemInMainHand().setItemMeta(newFishingRod);
            } else if (isOffHand) {
                inventory.getItemInOffHand().setItemMeta(newFishingRod);
            }
        }

        // check if the player caught an item
        if (event.getCaught() == null) {
            return;
        }
        ItemStack caughtItem = ((Item) event.getCaught()).getItemStack();
        Item caughtItemEntity = (Item) event.getCaught();
        if (toolStats.itemChecker.isValidItem(caughtItem.getType())) {
            ItemStack newItem = addFishedOrigin(caughtItem, player);
            if (newItem != null) {
                caughtItemEntity.setItemStack(newItem);
            }
        }
    }

    /**
     * Add lore to newly caught item.
     *
     * @param originalItem The original item to add lore.
     * @param owner        The player who caught it.
     * @return A copy of the new item with lore.
     */
    private ItemStack addFishedOrigin(ItemStack originalItem, Player owner) {
        ItemStack newItem = originalItem.clone();
        ItemMeta meta = originalItem.getItemMeta();
        if (meta == null) {
            return null;
        }
        long timeCreated = System.currentTimeMillis();
        Date finalDate;
        if (toolStats.config.getBoolean("normalize-time-creation")) {
            finalDate = toolStats.numberFormat.normalizeTime(timeCreated);
            timeCreated = finalDate.getTime();
        } else {
            finalDate = new Date(timeCreated);
        }
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(toolStats.timeCreated, PersistentDataType.LONG) || container.has(toolStats.itemOwner, PersistentDataType.LONG)) {
            return null;
        }

        // get the current lore the item
        List<Component> lore;
        if (meta.hasLore()) {
            lore = meta.lore();
        } else {
            lore = new ArrayList<>();
        }

        if (toolStats.configTools.checkConfig(newItem.getType(), "fished-on")) {
            container.set(toolStats.timeCreated, PersistentDataType.LONG, timeCreated);
            container.set(toolStats.originType, PersistentDataType.INTEGER, 5);

            String date = toolStats.numberFormat.formatDate(finalDate);
            Component newLine = toolStats.configTools.formatLore("fished.caught-on", "{date}", date);
            if (newLine == null) {
                return null;
            }
            lore.add(newLine);
            meta.lore(lore);
        }

        if (toolStats.configTools.checkConfig(newItem.getType(), "fished-by")) {
            container.set(toolStats.itemOwner, new UUIDDataType(), owner.getUniqueId());
            container.set(toolStats.originType, PersistentDataType.INTEGER, 5);

            Component newLine = toolStats.configTools.formatLore("fished.caught-by", "{player}", owner.getName());
            if (newLine == null) {
                return null;
            }
            lore.add(newLine);
            meta.lore(lore);
        }

        // if hash is enabled, add it
        if (toolStats.config.getBoolean("generate-hash-for-items")) {
            String hash = toolStats.hashMaker.makeHash(newItem.getType(), owner.getUniqueId(), timeCreated);
            container.set(toolStats.hash, PersistentDataType.STRING, hash);
        }

        newItem.setItemMeta(meta);
        return newItem;
    }
}
