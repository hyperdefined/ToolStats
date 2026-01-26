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

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import lol.hyper.toolstats.ToolStats;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
;

public class ProjectileShoot implements Listener {

    private final ToolStats toolStats;

    public ProjectileShoot(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(PlayerLaunchProjectileEvent event) {
        if (!(event.getProjectile() instanceof Trident tridentEntity)) {
            return;
        }

        if (!toolStats.configTools.checkWorld(tridentEntity.getWorld().getName())) {
            return;
        }

        tridentEntity.getScheduler().runDelayed(toolStats, scheduledTask -> {
            ItemStack tridentStack = tridentEntity.getItemStack();
            ItemMeta newTridentMeta = toolStats.itemLore.updateTridentThrows(tridentStack, 1);
            if (newTridentMeta == null) {
                return;
            }

            tridentStack.setItemMeta(newTridentMeta);
            tridentEntity.setItemStack(tridentStack);
        }, null, 1);
    }
}
