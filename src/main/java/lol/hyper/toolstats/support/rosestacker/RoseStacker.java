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
import org.bukkit.entity.LivingEntity;

public class RoseStacker {

    private final RoseStackerAPI rsAPI;

    public RoseStacker() {
        this.rsAPI = RoseStackerAPI.getInstance();
    }

    public int countMobs(LivingEntity entity) {
        if (!rsAPI.isEntityStacked(entity)) {
            // if the entity is not stacked, ignore
            return 1;
        }
        StackedEntity stackedEntity = rsAPI.getStackedEntity(entity);
        if (stackedEntity == null) {
            return 1;
        }

        boolean killAll = stackedEntity.getStackSettings().shouldKillEntireStackOnDeath();
        // if we kill the entire stack, add the entire stack to the count
        if (killAll) {
            return stackedEntity.getStackSize();
        }

        return 1;
    }
}
