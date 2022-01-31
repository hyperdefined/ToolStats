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
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
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

    @EventHandler
    public void onShear(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Sheep)) {
            return;
        }
        ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
        if (heldItem == null || heldItem.getType() == Material.AIR || heldItem.getType() != Material.SHEARS) {
            return;
        }

        Sheep sheep = (Sheep) entity;
        if (!sheep.isSheared()) {
            addLore(heldItem);
        }
    }

    private void addLore(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }
        Integer sheepSheared = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.shearsSheared, PersistentDataType.INTEGER)) {
            sheepSheared = container.get(toolStats.shearsSheared, PersistentDataType.INTEGER);
        }
        if (sheepSheared == null) {
            return;
        } else {
            sheepSheared++;
        }
        container.set(toolStats.shearsSheared, PersistentDataType.INTEGER, sheepSheared);

        String sheepShearedLore = toolStats.getLoreFromConfig("sheep-sheared", false);
        String sheepShearedLoreRaw = toolStats.getLoreFromConfig("sheep-sheared", true);

        if (sheepShearedLore == null || sheepShearedLoreRaw == null) {
            toolStats.logger.warning("There is no lore message for messages.sheep-sheared!");
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
                if (lore.get(x).contains(sheepShearedLore)) {
                    hasLore = true;
                    lore.set(x, sheepShearedLoreRaw.replace("{sheep}", Integer.toString(sheepSheared)));
                    break;
                }
            }
            // if the item has lore but doesn't have the tag, add it
            if (!hasLore) {
                lore.add(sheepShearedLoreRaw.replace("{sheep}", Integer.toString(sheepSheared)));
            }
        } else {
            // if the item has no lore, create a new list and add the string
            lore = new ArrayList<>();
            lore.add(sheepShearedLoreRaw.replace("{sheep}", Integer.toString(sheepSheared)));
        }
        if (toolStats.config.getBoolean("enabled.sheep-sheared")) {
            meta.setLore(lore);
        }
        itemStack.setItemMeta(meta);
    }
}
