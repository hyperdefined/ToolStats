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
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class EntityDamage implements Listener {

    private final ToolStats toolStats;
    public final Set<UUID> trackedMobs = new HashSet<>();
    private final List<EntityDamageEvent.DamageCause> ignoredCauses = Arrays.asList(EntityDamageEvent.DamageCause.SUICIDE, EntityDamageEvent.DamageCause.VOID, EntityDamageEvent.DamageCause.CUSTOM);

    public EntityDamage(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity mobBeingAttacked = (LivingEntity) event.getEntity();

        // ignore void and /kill damage
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (ignoredCauses.contains(cause)) {
            return;
        }

        // mob is going to die
        if (mobBeingAttacked.getHealth() - event.getFinalDamage() <= 0) {
            // a player is killing something
            if (event.getDamager() instanceof Player) {
                Player attackingPlayer = (Player) event.getDamager();
                if (attackingPlayer.getGameMode() == GameMode.CREATIVE || attackingPlayer.getGameMode() == GameMode.SPECTATOR) {
                    return;
                }
                PlayerInventory attackingPlayerInventory = attackingPlayer.getInventory();
                ItemStack heldItem = attackingPlayerInventory.getItemInMainHand();
                // only check certain items
                if (!ItemChecker.isMeleeWeapon(heldItem.getType())) {
                    return;
                }
                // a player is killing another player
                if (mobBeingAttacked instanceof Player) {
                    updatePlayerKills(heldItem);
                    return;
                }
                // player is killing regular mob
                updateMobKills(heldItem);
                trackedMobs.add(mobBeingAttacked.getUniqueId());
            }
            // trident is being thrown at something
            if (event.getDamager() instanceof Trident) {
                Trident trident = (Trident) event.getDamager();
                ItemStack newTrident;
                // trident is killing player
                if (mobBeingAttacked instanceof Player) {
                    newTrident = tridentPlayerKills(trident.getItem());
                } else {
                    // trident is killing a mob
                    newTrident = tridentMobKills(trident.getItem());
                    trackedMobs.add(mobBeingAttacked.getUniqueId());
                }
                if (newTrident != null) {
                    trident.setItem(newTrident);
                }
            }
            // arrow is being shot
            if (event.getDamager() instanceof Arrow) {
                Arrow arrow = (Arrow) event.getDamager();
                // if the shooter is a player
                if (arrow.getShooter() instanceof Player) {
                    Player shootingPlayer = (Player) arrow.getShooter();
                    if (shootingPlayer.getGameMode() == GameMode.CREATIVE || shootingPlayer.getGameMode() == GameMode.SPECTATOR) {
                        return;
                    }
                    PlayerInventory inventory = shootingPlayer.getInventory();
                    boolean isMainHand = inventory.getItemInMainHand().getType() == Material.BOW || inventory.getItemInMainHand().getType() == Material.CROSSBOW;
                    boolean isOffHand = inventory.getItemInOffHand().getType() == Material.BOW || inventory.getItemInMainHand().getType() == Material.CROSSBOW;
                    ItemStack heldBow = null;
                    if (isMainHand) {
                        heldBow = inventory.getItemInMainHand();
                    }
                    if (isOffHand) {
                        heldBow = inventory.getItemInOffHand();
                    }

                    // if the player is hold a bow in both hands
                    // default to main hand since that takes priority
                    if (isMainHand && isOffHand) {
                        heldBow = inventory.getItemInMainHand();
                    }

                    // player swapped
                    if (heldBow == null) {
                        return;
                    }

                    // player is shooting another player
                    if (mobBeingAttacked instanceof Player) {
                        updatePlayerKills(heldBow);
                    } else {
                        updateMobKills(heldBow);
                        trackedMobs.add(mobBeingAttacked.getUniqueId());
                    }
                }
            }
        }
        // player is taken damage but not being killed
        if (mobBeingAttacked instanceof Player) {
            Player playerTakingDamage = (Player) mobBeingAttacked;
            if (playerTakingDamage.getGameMode() == GameMode.CREATIVE || playerTakingDamage.getGameMode() == GameMode.SPECTATOR) {
                return;
            }
            PlayerInventory playerInventory = playerTakingDamage.getInventory();
            for (ItemStack armorPiece : playerInventory.getArmorContents()) {
                if (armorPiece != null) {
                    if (ItemChecker.isArmor(armorPiece.getType())) {
                        updateDamage(armorPiece, event.getFinalDamage());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        // ignore void and /kill damage
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (ignoredCauses.contains(cause)) {
            return;
        }

        LivingEntity mobBeingAttacked = (LivingEntity) event.getEntity();
        // player is taken damage but not being killed
        if (mobBeingAttacked instanceof Player) {
            Player playerTakingDamage = (Player) mobBeingAttacked;
            if (playerTakingDamage.getGameMode() == GameMode.CREATIVE || playerTakingDamage.getGameMode() == GameMode.SPECTATOR) {
                return;
            }
            PlayerInventory playerInventory = playerTakingDamage.getInventory();
            for (ItemStack armorPiece : playerInventory.getArmorContents()) {
                if (armorPiece != null) {
                    if (ItemChecker.isArmor(armorPiece.getType())) {
                        updateDamage(armorPiece, event.getFinalDamage());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByBlockEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        // ignore void and /kill damage
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (ignoredCauses.contains(cause)) {
            return;
        }

        LivingEntity mobBeingAttacked = (LivingEntity) event.getEntity();
        // player is taken damage but not being killed
        if (mobBeingAttacked instanceof Player) {
            Player playerTakingDamage = (Player) mobBeingAttacked;
            if (playerTakingDamage.getGameMode() == GameMode.CREATIVE || playerTakingDamage.getGameMode() == GameMode.SPECTATOR) {
                return;
            }
            PlayerInventory playerInventory = playerTakingDamage.getInventory();
            for (ItemStack armorPiece : playerInventory.getArmorContents()) {
                if (armorPiece != null) {
                    if (ItemChecker.isArmor(armorPiece.getType())) {
                        updateDamage(armorPiece, event.getFinalDamage());
                    }
                }
            }
        }
    }

    /**
     * Updates a weapon's player kills.
     *
     * @param itemStack The item to update.
     */
    private void updatePlayerKills(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(itemStack + " does NOT have any meta! Unable to update stats.");
            return;
        }
        Integer playerKills = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.swordPlayerKills, PersistentDataType.INTEGER)) {
            playerKills = container.get(toolStats.swordPlayerKills, PersistentDataType.INTEGER);
        }

        if (playerKills == null) {
            playerKills = 0;
            toolStats.logger.warning(itemStack + " does not have valid player-kills set! Resting to zero. This should NEVER happen.");
        }

        playerKills++;
        container.set(toolStats.swordPlayerKills, PersistentDataType.INTEGER, playerKills);

        String playerKillsFormatted = toolStats.numberFormat.formatInt(playerKills);
        List<String> newLore = toolStats.itemLore.addItemLore(meta, "{kills}", playerKillsFormatted, "kills.player");

        // do we add the lore based on the config?
        if (toolStats.checkConfig(itemStack, "player-kills")) {
            meta.setLore(newLore);
        }
        itemStack.setItemMeta(meta);
    }

    /**
     * Updates a weapon's mob kills.
     *
     * @param itemStack The item to update.
     */
    private void updateMobKills(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(itemStack + " does NOT have any meta! Unable to update stats.");
            return;
        }
        Integer mobKills = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.swordMobKills, PersistentDataType.INTEGER)) {
            mobKills = container.get(toolStats.swordMobKills, PersistentDataType.INTEGER);
        }

        if (mobKills == null) {
            mobKills = 0;
            toolStats.logger.warning(itemStack + " does not have valid mob-kills set! Resting to zero. This should NEVER happen.");
        }

        mobKills++;
        container.set(toolStats.swordMobKills, PersistentDataType.INTEGER, mobKills);

        String mobKillsFormatted = toolStats.numberFormat.formatInt(mobKills);
        List<String> newLore = toolStats.itemLore.addItemLore(meta, "{kills}", mobKillsFormatted, "kills.mob");

        // do we add the lore based on the config?
        if (toolStats.checkConfig(itemStack, "mob-kills")) {
            meta.setLore(newLore);
        }
        itemStack.setItemMeta(meta);
    }

    /**
     * Updates a player's armor damage stats.
     *
     * @param itemStack The armor piece.
     * @param damage    How much damage is being added.
     */
    private void updateDamage(ItemStack itemStack, double damage) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(itemStack + " does NOT have any meta! Unable to update stats.");
            return;
        }
        Double damageTaken = 0.0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.armorDamage, PersistentDataType.DOUBLE)) {
            damageTaken = container.get(toolStats.armorDamage, PersistentDataType.DOUBLE);
        }

        if (damageTaken == null) {
            damageTaken = 0.0;
            toolStats.logger.warning(itemStack + " does not have valid damage-taken set! Resting to zero. This should NEVER happen.");
        }

        damageTaken = damageTaken + damage;
        container.set(toolStats.armorDamage, PersistentDataType.DOUBLE, damageTaken);
        container.set(toolStats.armorDamageInt, PersistentDataType.INTEGER, damageTaken.intValue());

        String damageTakenFormatted = toolStats.numberFormat.formatDouble(damageTaken);
        List<String> newLore = toolStats.itemLore.addItemLore(meta, "{damage}", damageTakenFormatted, "damage-taken");

        if (toolStats.config.getBoolean("enabled.armor-damage")) {
            meta.setLore(newLore);
        }
        itemStack.setItemMeta(meta);
    }

    /**
     * Updates a trident's mob kills.
     *
     * @param trident The item to update.
     */
    private ItemStack tridentMobKills(ItemStack trident) {
        ItemStack newTrident = trident.clone();
        ItemMeta meta = newTrident.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(newTrident + " does NOT have any meta! Unable to update stats.");
            return null;
        }
        Integer mobKills = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.swordMobKills, PersistentDataType.INTEGER)) {
            mobKills = container.get(toolStats.swordMobKills, PersistentDataType.INTEGER);
        }

        if (mobKills == null) {
            mobKills = 0;
            toolStats.logger.warning(newTrident + " does not have valid mob-kills set! Resting to zero. This should NEVER happen.");
        }

        mobKills++;
        container.set(toolStats.swordMobKills, PersistentDataType.INTEGER, mobKills);

        String mobKillsFormatted = toolStats.numberFormat.formatInt(mobKills);
        List<String> newLore = toolStats.itemLore.addItemLore(meta, "{kills}", mobKillsFormatted, "kills.mob");

        // do we add the lore based on the config?
        if (toolStats.checkConfig(newTrident, "mob-kills")) {
            meta.setLore(newLore);
        }
        newTrident.setItemMeta(meta);
        return newTrident;
    }

    /**
     * Updates a trident's player kills.
     *
     * @param trident The item to update.
     */
    private ItemStack tridentPlayerKills(ItemStack trident) {
        ItemStack newTrident = trident.clone();
        ItemMeta meta = newTrident.getItemMeta();
        if (meta == null) {
            toolStats.logger.warning(newTrident + " does NOT have any meta! Unable to update stats.");
            return null;
        }
        Integer playerKills = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.swordPlayerKills, PersistentDataType.INTEGER)) {
            playerKills = container.get(toolStats.swordPlayerKills, PersistentDataType.INTEGER);
        }

        if (playerKills == null) {
            playerKills = 0;
            toolStats.logger.warning(newTrident + " does not have valid player-kills set! Resting to zero. This should NEVER happen.");
        }

        playerKills++;
        container.set(toolStats.swordPlayerKills, PersistentDataType.INTEGER, playerKills);

        String playerKillsFormatted = toolStats.numberFormat.formatInt(playerKills);
        List<String> newLore = toolStats.itemLore.addItemLore(meta, "{kills}", playerKillsFormatted, "kills.player");

        // do we add the lore based on the config?
        if (toolStats.checkConfig(newTrident, "player-kills")) {
            meta.setLore(newLore);
        }
        newTrident.setItemMeta(meta);
        return newTrident;
    }
}
