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
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class SheepShear implements Listener {

    private final ToolStats toolStats;

    public SheepShear(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onShear(PlayerInteractEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE && !toolStats.config.getBoolean("allow-creative")) {
            return;
        }
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Sheep sheep)) {
            return;
        }

        ItemStack heldShears = toolStats.itemChecker.getShears(player.getInventory());
        // player swapped or we can't get the shears
        if (heldShears == null) {
            return;
        }

        // make sure the sheep is not sheared
        if (sheep.isSheared()) {
            return;
        }

        // update the stats
        ItemMeta newShears = toolStats.itemLore.updateSheepSheared(heldShears, 1);
        if (newShears != null) {
            PlayerInventory inventory = player.getInventory();
            boolean isMain = inventory.getItemInMainHand().getType() == Material.SHEARS;
            boolean isOffHand = inventory.getItemInOffHand().getType() == Material.SHEARS;
            if (isMain && isOffHand) {
                inventory.getItemInMainHand().setItemMeta(newShears);
            } else if (isMain) {
                inventory.getItemInMainHand().setItemMeta(newShears);
            } else if (isOffHand) {
                inventory.getItemInOffHand().setItemMeta(newShears);
            }
        }
    }
}
