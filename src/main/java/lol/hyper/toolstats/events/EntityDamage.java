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
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;

import java.util.*;

public class EntityDamage implements Listener {

    private final ToolStats toolStats;
    // track mobs that are killed by a player
    public final Set<UUID> trackedMobs = new HashSet<>();
    private final List<EntityDamageEvent.DamageCause> ignoredCauses = Arrays.asList(EntityDamageEvent.DamageCause.SUICIDE, EntityDamageEvent.DamageCause.VOID, EntityDamageEvent.DamageCause.CUSTOM, EntityDamageEvent.DamageCause.KILL);

    public EntityDamage(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity mobBeingAttacked)) {
            return;
        }

        // ignore void and /kill damage
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (ignoredCauses.contains(cause)) {
            return;
        }

        Entity damager = event.getDamager();
        boolean playerAttacking = damager instanceof Player;
        boolean playerBeingAttacked = mobBeingAttacked instanceof Player;
        double finalDamage = event.getFinalDamage();
        boolean modDied = mobBeingAttacked.getHealth() - finalDamage <= 0;

        // player attacks something
        if (playerAttacking) {
            PlayerInventory playerAttackingInventory = ((Player) damager).getInventory();
            // make sure the item the player used is an item we want
            if (!toolStats.itemChecker.isMeleeWeapon(playerAttackingInventory.getItemInMainHand().getType())) {
                return;
            }

            // update their weapon's damage
            updateWeaponDamage(playerAttackingInventory, event.getFinalDamage());

            // the mob the player attacked died
            if (modDied) {
                // player killed another player
                if (playerBeingAttacked) {
                    updateWeaponKills(playerAttackingInventory, "player");
                } else {
                    // player kills a regular mob
                    updateWeaponKills(playerAttackingInventory, "mob");
                }
            }

            trackedMobs.add(mobBeingAttacked.getUniqueId());
            Bukkit.getGlobalRegionScheduler().runDelayed(toolStats, scheduledTask -> trackedMobs.remove(mobBeingAttacked.getUniqueId()), 20);
        }

        // something was hit by a trident
        if (damager instanceof Trident trident) {
            ProjectileSource source = trident.getShooter();
            if (source instanceof Player) {
                // update the trident's tracked damage
                updateTridentDamage(trident, finalDamage);

                // if the mob died from the trident
                if (modDied) {
                    // if the trident killed a player, update the kills
                    if (playerBeingAttacked) {
                        updateTridentKills(trident, "player");
                    } else {
                        // the trident killed a mob, update the kills
                        updateTridentKills(trident, "mob");
                    }
                }

                trackedMobs.add(mobBeingAttacked.getUniqueId());
                Bukkit.getGlobalRegionScheduler().runDelayed(toolStats, scheduledTask -> trackedMobs.remove(mobBeingAttacked.getUniqueId()), 20);
            }
        }

        // something was hit by an arrow
        if (damager instanceof Arrow arrow) {
            ProjectileSource source = arrow.getShooter();

            // a player shot the arrow
            if (source instanceof Player shootingPlayer) {
                // update the player's bow damage
                updateBowDamage(shootingPlayer.getInventory(), finalDamage);

                // if the mob died from the arrow
                if (modDied) {
                    if (playerBeingAttacked) {
                        // player killed another player with an arrow
                        updateBowKills(shootingPlayer.getInventory(), "player");
                    } else {
                        // player killed mob with an arrow
                        updateBowKills(shootingPlayer.getInventory(), "mob");
                    }
                }

                trackedMobs.add(mobBeingAttacked.getUniqueId());
                Bukkit.getGlobalRegionScheduler().runDelayed(toolStats, scheduledTask -> trackedMobs.remove(mobBeingAttacked.getUniqueId()), 20);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mobBeingAttacked)) {
            return;
        }

        // ignore void and /kill damage
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (ignoredCauses.contains(cause)) {
            return;
        }

        // player is taking damage
        if (mobBeingAttacked instanceof Player playerTakingDamage) {
            if (playerTakingDamage.getGameMode() == GameMode.CREATIVE && !toolStats.config.getBoolean("allow-creative")) {
                return;
            }
            updateArmorDamage(playerTakingDamage.getInventory(), event.getFinalDamage());
        }
    }

    private void updateArmorDamage(PlayerInventory playerInventory, double damage) {
        ItemStack[] armorContents = playerInventory.getArmorContents();
        for (ItemStack armorPiece : armorContents) {
            if (armorPiece != null) {
                if (toolStats.itemChecker.isArmor(armorPiece.getType())) {
                    ItemMeta newItem = toolStats.itemLore.updateArmorDamage(armorPiece, damage, false);
                    if (newItem != null) {
                        armorPiece.setItemMeta(newItem);
                    }
                }
            }
        }
        // apply the new armor
        playerInventory.setArmorContents(armorContents);
    }

    private void updateBowDamage(PlayerInventory playerInventory, double damage) {
        ItemStack heldBow = toolStats.itemChecker.getBow(playerInventory);
        if (heldBow == null) {
            return;
        }

        boolean isMain = playerInventory.getItemInMainHand().getType() == Material.BOW || playerInventory.getItemInMainHand().getType() == Material.CROSSBOW;
        boolean isOffHand = playerInventory.getItemInOffHand().getType() == Material.BOW || playerInventory.getItemInOffHand().getType() == Material.CROSSBOW;
        ItemMeta newBowDamage = toolStats.itemLore.updateWeaponDamage(heldBow, damage, false);
        //toolStats.logger.info(newBowDamage.toString());

        // player is shooting another player
        if (newBowDamage != null) {
            if (isMain && isOffHand) {
                playerInventory.getItemInMainHand().setItemMeta(newBowDamage);
            } else if (isMain) {
                playerInventory.getItemInMainHand().setItemMeta(newBowDamage);
            } else if (isOffHand) {
                playerInventory.getItemInOffHand().setItemMeta(newBowDamage);
            }
        }
    }

    private void updateBowKills(PlayerInventory playerInventory, String type) {
        ItemStack heldBow = toolStats.itemChecker.getBow(playerInventory);
        if (heldBow == null) {
            return;
        }

        boolean isMain = playerInventory.getItemInMainHand().getType() == Material.BOW || playerInventory.getItemInMainHand().getType() == Material.CROSSBOW;
        boolean isOffHand = playerInventory.getItemInOffHand().getType() == Material.BOW || playerInventory.getItemInOffHand().getType() == Material.CROSSBOW;

        if (type.equalsIgnoreCase("mob")) {
            // player is shooting a mob
            ItemMeta newBow = toolStats.itemLore.updateMobKills(heldBow, 1);
            if (newBow != null) {
                if (isMain && isOffHand) {
                    playerInventory.getItemInMainHand().setItemMeta(newBow);
                } else if (isMain) {
                    playerInventory.getItemInMainHand().setItemMeta(newBow);
                } else if (isOffHand) {
                    playerInventory.getItemInOffHand().setItemMeta(newBow);
                }
            }
        }

        if (type.equalsIgnoreCase("player")) {
            ItemMeta newBowKills = toolStats.itemLore.updatePlayerKills(heldBow, 1);
            if (newBowKills != null) {
                if (isMain && isOffHand) {
                    playerInventory.getItemInMainHand().setItemMeta(newBowKills);
                } else if (isMain) {
                    playerInventory.getItemInMainHand().setItemMeta(newBowKills);
                } else if (isOffHand) {
                    playerInventory.getItemInOffHand().setItemMeta(newBowKills);
                }
            }
        }
    }

    private void updateTridentKills(Trident trident, String type) {
        ItemStack newTrident = trident.getItemStack();
        ItemMeta newKills;
        if (type.equalsIgnoreCase("player")) {
            newKills = toolStats.itemLore.updatePlayerKills(trident.getItemStack(), 1);
        } else {
            newKills = toolStats.itemLore.updateMobKills(trident.getItemStack(), 1);
        }
        if (newKills != null) {
            newTrident.setItemMeta(newKills);
            trident.setItemStack(newTrident);
        }
    }

    private void updateTridentDamage(Trident trident, double damage) {
        ItemStack newTrident = trident.getItemStack();
        ItemMeta newDamage = toolStats.itemLore.updateWeaponDamage(trident.getItemStack(), damage, false);
        if (newDamage != null) {
            newTrident.setItemMeta(newDamage);
            trident.setItemStack(newTrident);
        }
    }

    private void updateWeaponDamage(PlayerInventory playerInventory, double damage) {
        ItemStack heldWeapon = playerInventory.getItemInMainHand();
        ItemMeta newHeldWeaponMeta = toolStats.itemLore.updateWeaponDamage(heldWeapon, damage, false);
        if (newHeldWeaponMeta != null) {
            playerInventory.getItemInMainHand().setItemMeta(newHeldWeaponMeta);
        }
    }

    private void updateWeaponKills(PlayerInventory playerInventory, String type) {
        ItemStack heldWeapon = playerInventory.getItemInMainHand();
        ItemMeta newHeldWeaponMeta = null;
        if (type.equalsIgnoreCase("player")) {
            newHeldWeaponMeta = toolStats.itemLore.updatePlayerKills(heldWeapon, 1);
        }
        if (type.equalsIgnoreCase("mob")) {
            newHeldWeaponMeta = toolStats.itemLore.updateMobKills(heldWeapon, 1);
        }
        if (newHeldWeaponMeta != null) {
            playerInventory.getItemInMainHand().setItemMeta(newHeldWeaponMeta);
        }
    }
}
