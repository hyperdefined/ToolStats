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
import net.kyori.adventure.text.Component;
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
        if (!(entity instanceof Sheep sheep)) {
            return;
        }

        ItemStack heldShears = getShears(player.getInventory());
        // player swapped or we can't get the shears
        if (heldShears == null) {
            return;
        }

        // make sure the sheep is not sheared
        if (sheep.isSheared()) {
            return;
        }

        // update the stats
        addLore(heldShears);
    }

    private static @Nullable ItemStack getShears(PlayerInventory inventory) {
        ItemStack main = inventory.getItemInMainHand();
        ItemStack offHand = inventory.getItemInOffHand();

        boolean isMain = main.getType() == Material.SHEARS;
        boolean isOffHand = offHand.getType() == Material.SHEARS;

        // if the player is holding shears in their main hand, use that one
        // if the shears are in their offhand instead, use that one after checking main hand
        // Minecraft prioritizes main hand if the player holds in both hands
        if (isMain) {
            return main;
        }
        if (isOffHand) {
            return offHand;
        }

        return null;
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
            Component oldLine = toolStats.configTools.formatLore("sheep-sheared", "{sheep}", oldSheepFormatted);
            Component newLine = toolStats.configTools.formatLore("sheep-sheared", "{sheep}", newSheepFormatted);
            if (oldLine == null || newLine == null) {
                return;
            }
            List<Component> newLore = toolStats.itemLore.updateItemLore(meta, oldLine, newLine);
            meta.lore(newLore);
        }
        newShears.setItemMeta(meta);
    }
}
