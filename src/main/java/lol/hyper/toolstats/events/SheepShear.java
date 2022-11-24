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
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class SheepShear implements Listener {

    private final ToolStats toolStats;

    public SheepShear(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onShear(PlayerInteractEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Sheep)) {
            return;
        }
        // check if the player is right-clicking with shears only
        int heldItemSlot = player.getInventory().getHeldItemSlot();
        ItemStack heldItem = player.getInventory().getItem(heldItemSlot);
        if (heldItem == null || heldItem.getType() == Material.AIR || heldItem.getType() != Material.SHEARS) {
            return;
        }

        Sheep sheep = (Sheep) entity;
        // make sure the sheep is not sheared
        if (!sheep.isSheared()) {
            ItemStack newShears = addLore(heldItem);
            if (newShears != null) {
                Bukkit.getScheduler().runTaskLater(toolStats, () -> player.getInventory().setItem(heldItemSlot, newShears), 1);
            }
        }
    }

    /**
     * Adds tags to shears.
     *
     * @param oldShears The shears.
     */
    private ItemStack addLore(ItemStack oldShears) {
        ItemStack newShears = oldShears.clone();
        ItemMeta meta = newShears.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(newShears + " does NOT have any meta! Unable to update stats.");
            return null;
        }
        Integer sheepSheared = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.shearsSheared, PersistentDataType.INTEGER)) {
            sheepSheared = container.get(toolStats.shearsSheared, PersistentDataType.INTEGER);
        }

        if (sheepSheared == null) {
            sheepSheared = 0;
            toolStats.logger.warning(newShears + " does not have valid sheared set! Resting to zero. This should NEVER happen.");
        }

        sheepSheared++;
        container.set(toolStats.shearsSheared, PersistentDataType.INTEGER, sheepSheared);

        String sheepShearedLore = toolStats.getLoreFromConfig("sheep-sheared", false);
        String sheepShearedLoreRaw = toolStats.getLoreFromConfig("sheep-sheared", true);

        if (sheepShearedLore == null || sheepShearedLoreRaw == null) {
            toolStats.logger.warning("There is no lore message for messages.sheep-sheared!");
            return null;
        }

        List<String> lore;
        String newLine = sheepShearedLoreRaw.replace("{sheep}", toolStats.numberFormat.formatInt(sheepSheared));
        if (meta.hasLore()) {
            lore = meta.getLore();
            boolean hasLore = false;
            // we do a for loop like this, we can keep track of index
            // this doesn't mess the lore up of existing items
            for (int x = 0; x < lore.size(); x++) {
                if (lore.get(x).contains(sheepShearedLore)) {
                    hasLore = true;
                    lore.set(x, newLine);
                    break;
                }
            }
            // if the item has lore but doesn't have the tag, add it
            if (!hasLore) {
                lore.add(newLine);
            }
        } else {
            // if the item has no lore, create a new list and add the string
            lore = new ArrayList<>();
            lore.add(newLine);
        }
        if (toolStats.config.getBoolean("enabled.sheep-sheared")) {
            meta.setLore(lore);
        }
        newShears.setItemMeta(meta);
        return newShears;
    }
}
