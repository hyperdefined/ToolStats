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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ShootBow implements Listener {

    private final ToolStats toolStats;

    public ShootBow(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onShoot(EntityShootBowEvent event) {
        Entity shooter = event.getEntity();
        // only listen for players
        if (!(shooter instanceof Player)) {
            return;
        }

        Player player = (Player) shooter;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.ADVENTURE) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        boolean isMainHand = inventory.getItemInMainHand().getType() == Material.BOW || inventory.getItemInMainHand().getType() == Material.CROSSBOW;
        boolean isOffHand = inventory.getItemInOffHand().getType() == Material.BOW || inventory.getItemInMainHand().getType() == Material.CROSSBOW;
        ItemStack heldBow = null;
        if (isMainHand) {
            heldBow = inventory.getItemInMainHand();
        }
        if (isOffHand) {
            heldBow = inventory.getItemInOffHand();
        }

        // if the player is holding a bow in both hands
        // default to main hand since that takes priority
        if (isMainHand && isOffHand) {
            heldBow = inventory.getItemInMainHand();
        }

        // player swapped
        if (heldBow == null) {
            return;
        }

        updateArrowsShot(heldBow);
    }

    private void updateArrowsShot(ItemStack bow) {
        ItemMeta meta = bow.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(bow + " does NOT have any meta! Unable to update stats.");
            return;
        }
        // read the current stats from the item
        // if they don't exist, then start from 0
        Integer arrowsShot = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.arrowsShot, PersistentDataType.INTEGER)) {
            arrowsShot = container.get(toolStats.arrowsShot, PersistentDataType.INTEGER);
        }

        if (arrowsShot == null) {
            arrowsShot = 0;
            toolStats.logger.warning(arrowsShot + " does not have valid arrows-shot set! Resting to zero. This should NEVER happen.");
        }

        arrowsShot++;
        container.set(toolStats.arrowsShot, PersistentDataType.INTEGER, arrowsShot);

        // do we add the lore based on the config?
        if (toolStats.config.getBoolean("enabled.arrows-shot")) {
            String arrowsShotFormatted = toolStats.numberFormat.formatInt(arrowsShot);
            List<String> newLore = toolStats.itemLore.addItemLore(meta, "{arrows}", arrowsShotFormatted, "arrows-shot");
            meta.setLore(newLore);
        }
        bow.setItemMeta(meta);
    }
}
