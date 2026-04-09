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

package lol.hyper.toolstats.support.rosestacker;

import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedEntity;
import lol.hyper.toolstats.ToolStats;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.function.Consumer;

public class RoseStacker {

    private final ToolStats toolStats;
    private final RoseStackerAPI rsAPI;

    public RoseStacker(ToolStats toolStats) {
        this.toolStats = toolStats;
        this.rsAPI = RoseStackerAPI.getInstance();
    }

    public void countMobs(LivingEntity entity, Consumer<Integer> callback) {
        if (!rsAPI.isEntityStacked(entity)) {
            // if the entity is not stacked, ignore
            callback.accept(1);
            return;
        }
        StackedEntity stackedEntity = rsAPI.getStackedEntity(entity);
        if (stackedEntity == null) {
            callback.accept(1);
            return;
        }

        int before = stackedEntity.getStackSize();
        boolean killAll = stackedEntity.getStackSettings().shouldKillEntireStackOnDeath();
        // if we kill the entire stack, add the entire stack to the count
        if (killAll) {
            callback.accept(before);
            return;
        }

        Location stackedLocation = stackedEntity.getLocation();
        Chunk stackedChunk = stackedEntity.getLocation().getChunk();
        // check the stack size after a tick to see the difference
        Bukkit.getRegionScheduler().runDelayed(toolStats, stackedLocation.getWorld(), stackedChunk.getX(), stackedChunk.getZ(), _ -> {
            int after = stackedEntity.getStackSize();
            int difference = before - after;
            // if the diff goes negative, we killed more than the stack
            // we killed the entire stack, so return the size
            if (difference <= 0) {
                difference = before;
            }

            callback.accept(difference);
        }, 1);
    }
}
