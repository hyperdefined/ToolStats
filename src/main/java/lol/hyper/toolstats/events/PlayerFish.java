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

    @EventHandler(priority = EventPriority.HIGHEST)
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
        ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
        int heldItemSlot = player.getInventory().getHeldItemSlot();
        if (heldItem == null || heldItem.getType() == Material.AIR || heldItem.getType() != Material.FISHING_ROD) {
            return;
        }
        // update the fishing rod to the new one
        ItemStack newRod = updateFishCount(heldItem);
        if (newRod != null) {
            player.getInventory().setItem(heldItemSlot, newRod);
        }
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
     * @param originalRod The fishing rod to update.
     * @return A new fishing rod with update counts.
     */
    private ItemStack updateFishCount(ItemStack originalRod) {
        ItemStack newRod = originalRod.clone();
        ItemMeta meta = newRod.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(originalRod + " does NOT have any meta! Unable to update stats.");
            return null;
        }
        Integer fishCaught;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.fishingRodCaught, PersistentDataType.INTEGER)) {
            fishCaught = container.get(toolStats.fishingRodCaught, PersistentDataType.INTEGER);
        } else {
            fishCaught = 0;
        }

        if (fishCaught == null) {
            fishCaught = 0;
            toolStats.logger.warning(originalRod + " does not have valid fish-caught set! Resting to zero. This should NEVER happen.");
        }

        fishCaught++;
        container.set(toolStats.fishingRodCaught, PersistentDataType.INTEGER, fishCaught);

        String fishCaughtLore = toolStats.getLoreFromConfig("fished.fish-caught", false);
        String fishCaughtLoreRaw = toolStats.getLoreFromConfig("fished.fish-caught", true);

        if (fishCaughtLore == null || fishCaughtLoreRaw == null) {
            toolStats.logger.warning("There is no lore message for messages.fish-caught!");
            return null;
        }

        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            boolean hasLore = false;
            // we do a for loop like this, we can keep track of index
            // this doesn't mess the lore up of existing items
            for (int x = 0; x < lore.size(); x++) {
                if (lore.get(x).contains(fishCaughtLore)) {
                    hasLore = true;
                    lore.set(x, fishCaughtLoreRaw.replace("{fish}", toolStats.commaFormat.format(fishCaught)));
                    break;
                }
            }
            // if the item has lore but doesn't have the tag, add it
            if (!hasLore) {
                lore.add(fishCaughtLoreRaw.replace("{fish}", toolStats.commaFormat.format(fishCaught)));
            }
        } else {
            // if the item has no lore, create a new list and add the string
            lore = new ArrayList<>();
            lore.add(fishCaughtLoreRaw.replace("{fish}", toolStats.commaFormat.format(fishCaught)));
        }
        if (toolStats.config.getBoolean("enabled.fish-caught")) {
            meta.setLore(lore);
        }
        newRod.setItemMeta(meta);
        return newRod;
    }

    /**
     * Add lore to newly caught item.
     * @param originalItem The original item to add lore.
     * @param owner The player who caught it.
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

        String caughtByLoreRaw = toolStats.getLoreFromConfig("fished.caught-by", true);
        String caughtOnLoreRaw = toolStats.getLoreFromConfig("fished.caught-on", true);

        if (caughtByLoreRaw == null || caughtOnLoreRaw == null) {
            toolStats.logger.warning("There is no lore message for messages.fished!");
            return null;
        }

        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
        } else {
            lore = new ArrayList<>();
        }
        if (toolStats.checkConfig(newItem, "fished-tag")) {
            lore.add(caughtOnLoreRaw.replace("{date}", toolStats.dateFormat.format(finalDate)));
            lore.add(caughtByLoreRaw.replace("{player}", owner.getName()));
            meta.setLore(lore);
        }
        newItem.setItemMeta(meta);
        return newItem;
    }
}
