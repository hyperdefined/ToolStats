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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Sheep)) {
            return;
        }

        ItemStack shears = getShears(player);
        // player swapped items?
        if (shears == null) {
            return;
        }

        // make sure the sheep is not sheared
        Sheep sheep = (Sheep) entity;
        if (sheep.isSheared()) {
            return;
        }

        // update the stats
        addLore(shears);
    }

    private static @Nullable ItemStack getShears(Player player) {
        PlayerInventory inventory = player.getInventory();
        boolean isMainHand = inventory.getItemInMainHand().getType() == Material.SHEARS;
        boolean isOffHand = inventory.getItemInOffHand().getType() == Material.SHEARS;
        ItemStack shears = null;
        if (isMainHand) {
            shears = inventory.getItemInMainHand();
        }
        if (isOffHand) {
            shears = inventory.getItemInOffHand();
        }

        // if the player is hold shears in both hands
        // default to main hand since that takes priority
        if (isMainHand && isOffHand) {
            shears = inventory.getItemInMainHand();
        }
        return shears;
    }

    /**
     * Adds tags to shears.
     *
     * @param newShears The shears.
     */
    private void addLore(ItemStack newShears) {
        ItemMeta meta = newShears.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(newShears + " does NOT have any meta! Unable to update stats.");
            return;
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

        container.set(toolStats.shearsSheared, PersistentDataType.INTEGER, sheepSheared + 1);

        if (toolStats.config.getBoolean("enabled.sheep-sheared")) {
            String oldSheepFormatted = toolStats.numberFormat.formatInt(sheepSheared);
            String newSheepFormatted = toolStats.numberFormat.formatInt(sheepSheared + 1);
            String oldLine = toolStats.configTools.formatLore("sheep-sheared", "{sheep}", oldSheepFormatted);
            String newLine = toolStats.configTools.formatLore("sheep-sheared", "{sheep}", newSheepFormatted);
            if (oldLine == null || newLine == null) {
                return;
            }
            List<String> newLore = toolStats.itemLore.updateItemLore(meta, oldLine, newLine);
            meta.setLore(newLore);
        }
        newShears.setItemMeta(meta);
    }
}
