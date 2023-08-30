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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoin implements Listener {

    private final ToolStats toolStats;

    public PlayerJoin(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Inventory inventory = player.getInventory();
        for (ItemStack itemStack : inventory) {
            if (itemStack == null) {
                continue;
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                continue;
            }
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            // ignore any items that already have the origin tag
            if (container.has(toolStats.originType, PersistentDataType.INTEGER)) {
                continue;
            }
            // ignore items that are not the right type
            if (!ItemChecker.isValidItem(itemStack.getType())) {
                continue;
            }

            ItemMeta newMeta = toolStats.itemLore.getOrigin(itemMeta, itemStack.getType() == Material.ELYTRA);
            if (newMeta == null) {
                continue;
            }
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    itemStack.setItemMeta(newMeta);
                }
            };
            Location location = inventory.getLocation();
            // only run for actual inventories
            if (location != null) {
                toolStats.scheduleRegion(runnable, location.getWorld(), location.getChunk(), 1);
            }
        }
    }
}
