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
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        // make sure the player is holding a fishing rod
        // player can fish with their offhand
        PlayerInventory inventory = player.getInventory();
        boolean isMainHand = inventory.getItemInMainHand().getType() == Material.FISHING_ROD;
        boolean isOffHand = inventory.getItemInOffHand().getType() == Material.FISHING_ROD;
        ItemStack fishingRod = null;
        if (isMainHand) {
            fishingRod = inventory.getItemInMainHand();
        }
        if (isOffHand) {
            fishingRod = inventory.getItemInOffHand();
        }

        // if the player is hold fishing rods in both hands
        // default to main hand since that takes priority
        if (isMainHand && isOffHand) {
            fishingRod = inventory.getItemInMainHand();
        }

        // player swapped items?
        if (fishingRod == null) {
            return;
        }

        // update the fishing rod!
        updateFishCount(fishingRod);

        // check if the player caught an item
        if (event.getCaught() == null) {
            return;
        }
        ItemStack caughtItem = ((Item) event.getCaught()).getItemStack();
        Item caughtItemEntity = (Item) event.getCaught();
        if (ItemChecker.isValidItem(caughtItem.getType())) {
            ItemStack newItem = addNewLore(caughtItem, player);
            if (newItem != null) {
                caughtItemEntity.setItemStack(newItem);
            }
        }
    }

    /**
     * Update a fishing rod's fish count.
     *
     * @param fishingRod The fishing rod to update.
     */
    private void updateFishCount(ItemStack fishingRod) {
        ItemMeta meta = fishingRod.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(fishingRod + " does NOT have any meta! Unable to update stats.");
            return;
        }
        Integer fishCaught = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.fishingRodCaught, PersistentDataType.INTEGER)) {
            fishCaught = container.get(toolStats.fishingRodCaught, PersistentDataType.INTEGER);
        }

        if (fishCaught == null) {
            fishCaught = 0;
            toolStats.logger.warning(fishingRod + " does not have valid fish-caught set! Resting to zero. This should NEVER happen.");
        }

        fishCaught++;
        container.set(toolStats.fishingRodCaught, PersistentDataType.INTEGER, fishCaught);

        String fishCaughtFormatted = toolStats.numberFormat.formatInt(fishCaught);
        List<String> newLore = toolStats.itemLore.addItemLore(meta, "{fish}", fishCaughtFormatted, "fished.fish-caught");

        if (toolStats.config.getBoolean("enabled.fish-caught")) {
            meta.setLore(newLore);
        }
        fishingRod.setItemMeta(meta);
    }

    /**
     * Add lore to newly caught item.
     *
     * @param originalItem The original item to add lore.
     * @param owner        The player who caught it.
     * @return A copy of the new item with lore.
     */
    private ItemStack addNewLore(ItemStack originalItem, Player owner) {
        ItemStack newItem = originalItem.clone();
        ItemMeta meta = originalItem.getItemMeta();
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

        String formattedDate = toolStats.numberFormat.formatDate(finalDate);
        List<String> newLore = toolStats.itemLore.addNewOwner(meta, owner.getName(), formattedDate, "FISHED");

        if (toolStats.checkConfig(newItem, "fished-tag")) {
            meta.setLore(newLore);
        }
        newItem.setItemMeta(meta);
        return newItem;
    }
}
