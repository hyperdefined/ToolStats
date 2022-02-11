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
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlayerFish implements Listener {

    private final ToolStats toolStats;
    public final String[] validItems = {
            "pickaxe", "sword", "shovel", "axe", "hoe", "bow", "helmet", "chestplate", "leggings", "boots", "fishing"
    };
    private final SimpleDateFormat format = new SimpleDateFormat("M/dd/yyyy", Locale.ENGLISH);

    public PlayerFish(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.isCancelled()) {
            return;
        }
        // only listen to when a player catches a fish
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
        if (heldItem == null || heldItem.getType() == Material.AIR || heldItem.getType() != Material.FISHING_ROD) {
            return;
        }
        updateFishCount(heldItem);
        if (event.getCaught() == null) {
            return;
        }
        ItemStack caughtItem = ((Item) event.getCaught()).getItemStack();
        for (String x : validItems) {
            if (caughtItem.getType().toString().toLowerCase(Locale.ROOT).contains(x)) {
                addNewLore(caughtItem, player);
            }
        }
    }

    private void updateFishCount(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }
        Integer fishCaught = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.fishingRodCaught, PersistentDataType.INTEGER)) {
            fishCaught = container.get(toolStats.fishingRodCaught, PersistentDataType.INTEGER);
        }
        if (fishCaught == null) {
            return;
        } else {
            fishCaught++;
        }
        container.set(toolStats.fishingRodCaught, PersistentDataType.INTEGER, fishCaught);

        String fishCaughtLore = toolStats.getLoreFromConfig("fished.fish-caught", false);
        String fishCaughtLoreRaw = toolStats.getLoreFromConfig("fished.fish-caught", true);

        if (fishCaughtLore == null || fishCaughtLoreRaw == null) {
            toolStats.logger.warning("There is no lore message for messages.fish-caught!");
            return;
        }

        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
            boolean hasLore = false;
            // we do a for loop like this, we can keep track of index
            // this doesn't mess the lore up of existing items
            for (int x = 0; x < lore.size(); x++) {
                if (lore.get(x).contains(fishCaughtLore)) {
                    hasLore = true;
                    lore.set(x, fishCaughtLoreRaw.replace("{fish}", Integer.toString(fishCaught)));
                    break;
                }
            }
            // if the item has lore but doesn't have the tag, add it
            if (!hasLore) {
                lore.add(fishCaughtLoreRaw.replace("{fish}", Integer.toString(fishCaught)));
            }
        } else {
            // if the item has no lore, create a new list and add the string
            lore = new ArrayList<>();
            lore.add(fishCaughtLoreRaw.replace("{fish}", Integer.toString(fishCaught)));
        }
        if (toolStats.config.getBoolean("enabled.fish-caught")) {
            meta.setLore(lore);
        }
        itemStack.setItemMeta(meta);
    }

    private void addNewLore(ItemStack itemStack, Player owner) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }
        long timeCreated = System.currentTimeMillis();
        Date finalDate = new Date(timeCreated);
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(toolStats.timeCreated, PersistentDataType.LONG) || container.has(toolStats.genericOwner, PersistentDataType.LONG)) {
            return;
        }

        container.set(toolStats.timeCreated, PersistentDataType.LONG, timeCreated);
        container.set(toolStats.genericOwner, new UUIDDataType(), owner.getUniqueId());

        String caughtByLoreRaw = toolStats.getLoreFromConfig("fished.caught-by", true);
        String caughtOnLoreRaw = toolStats.getLoreFromConfig("fished.caught-on", true);

        if (caughtByLoreRaw == null || caughtOnLoreRaw == null) {
            toolStats.logger.warning("There is no lore message for messages.fished!");
            return;
        }

        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
        } else {
            lore = new ArrayList<>();
        }
        if (toolStats.checkConfig(itemStack, "fished-tag")) {
            lore.add(caughtOnLoreRaw.replace("{date}", format.format(finalDate)));
            lore.add(caughtByLoreRaw.replace("{player}", owner.getName()));
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }
}
