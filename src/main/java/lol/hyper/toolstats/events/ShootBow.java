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
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public class ShootBow implements Listener {

    private final ToolStats toolStats;

    public ShootBow(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onShoot(EntityShootBowEvent event) {
        Entity shooter = event.getEntity();
        // only listen for players
        if (!(shooter instanceof Player player)) {
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.ADVENTURE) {
            return;
        }

        ItemStack heldBow = toolStats.itemChecker.getBow(player.getInventory());
        // player swapped or we can't get the bow
        if (heldBow == null) {
            return;
        }

        ItemMeta newItem = toolStats.itemLore.updateArrowsShot(heldBow, 1);
        if (newItem != null) {
            PlayerInventory inventory = player.getInventory();
            boolean isMain = inventory.getItemInMainHand().getType() == Material.BOW || inventory.getItemInMainHand().getType() == Material.CROSSBOW;
            boolean isOffHand = inventory.getItemInOffHand().getType() == Material.BOW || inventory.getItemInOffHand().getType() == Material.CROSSBOW;
            if (isMain) {
                inventory.getItemInMainHand().setItemMeta(newItem);
            }
            if (isOffHand) {
                inventory.getItemInOffHand().setItemMeta(newItem);
            }
        }
    }
}
