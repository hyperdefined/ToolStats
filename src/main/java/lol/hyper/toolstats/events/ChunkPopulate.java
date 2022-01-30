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
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ChunkPopulate implements Listener {

    // this tags all elytras with a "new" tag
    // this let's use tag any new elytras player loot

    private final ToolStats toolStats;

    public ChunkPopulate(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onPopulate(ChunkPopulateEvent event) {
        if (event.getChunk().getWorld().getEnvironment() != World.Environment.THE_END) {
            return;
        }
        // this is delayed because entities are not loaded instantly
        // we just check 1 second later
        Bukkit.getScheduler().runTaskLater(toolStats, () -> {
            Chunk chunk = event.getChunk();
            for (Entity entity : chunk.getEntities()) {
                if (entity instanceof ItemFrame) {
                    ItemFrame itemFrame = (ItemFrame) entity;
                    if (itemFrame.getItem().getType() == Material.ELYTRA) {
                        ItemStack elytraCopy = itemFrame.getItem();
                        ItemMeta meta = elytraCopy.getItemMeta();
                        if (meta == null) {
                            return;
                        }
                        PersistentDataContainer container = meta.getPersistentDataContainer();
                        container.set(toolStats.newElytra, PersistentDataType.INTEGER, 1);
                        elytraCopy.setItemMeta(meta);
                        itemFrame.setItem(elytraCopy);
                    }
                }
            }
        }, 20);
    }
}
