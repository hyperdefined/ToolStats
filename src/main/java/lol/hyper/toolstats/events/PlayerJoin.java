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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

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
            // ignore items that are not the right type
            if (!toolStats.itemChecker.isValidItem(itemStack.getType())) {
                continue;
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta == null) {
                continue;
            }
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();

            // generate a hash if the item doesn't have one
            if (!container.has(toolStats.hash, PersistentDataType.STRING)) {
                // make sure the item has an owner
                if (!container.has(toolStats.genericOwner, new UUIDDataType())) {
                    continue;
                }
                UUID owner = container.get(toolStats.genericOwner, new UUIDDataType());
                if (owner == null) {
                    continue;
                }
                Long timestamp = container.get(toolStats.timeCreated, PersistentDataType.LONG);
                if (timestamp == null) {
                    continue;
                }
                String hash = toolStats.hashMaker.makeHash(itemStack.getType(), owner, timestamp);
                container.set(toolStats.hash, PersistentDataType.STRING, hash);
            }
            ItemMeta clone = itemMeta.clone();
            player.getScheduler().runDelayed(toolStats, scheduledTask -> itemStack.setItemMeta(clone), null, 1);
        }
    }
}
