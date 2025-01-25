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
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class EntityDamage implements Listener {

    private final ToolStats toolStats;
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

        // mob is going to die
        if (mobBeingAttacked.getHealth() - event.getFinalDamage() <= 0) {
            // a player is killing something
            if (event.getDamager() instanceof Player attackingPlayer) {
                if (attackingPlayer.getGameMode() == GameMode.CREATIVE || attackingPlayer.getGameMode() == GameMode.SPECTATOR) {
                    return;
                }
                PlayerInventory attackingPlayerInventory = attackingPlayer.getInventory();
                ItemStack heldItem = attackingPlayerInventory.getItemInMainHand();
                // only check certain items
                if (!toolStats.itemChecker.isMeleeWeapon(heldItem.getType())) {
                    return;
                }
                // a player is killing another player
                if (mobBeingAttacked instanceof Player) {
                    ItemStack newItem = toolStats.itemLore.updatePlayerKills(heldItem, 1);
                    if (newItem != null) {
                        attackingPlayerInventory.setItemInMainHand(newItem);
                    }
                    return;
                }
                // player is killing regular mob
                ItemStack newItem = toolStats.itemLore.updateMobKills(heldItem, 1);
                if (newItem != null) {
                    attackingPlayerInventory.setItemInMainHand(newItem);
                }
                trackedMobs.add(mobBeingAttacked.getUniqueId());
            }
            // trident is being thrown at something
            if (event.getDamager() instanceof Trident trident) {
                ItemStack newTrident;
                // trident is killing player
                if (mobBeingAttacked instanceof Player) {
                    newTrident = toolStats.itemLore.updatePlayerKills(trident.getItemStack(), 1);
                } else {
                    // trident is killing a mob
                    newTrident = toolStats.itemLore.updateMobKills(trident.getItemStack(), 1);
                    trackedMobs.add(mobBeingAttacked.getUniqueId());
                }
                if (newTrident != null) {
                    trident.setItemStack(newTrident);
                }
            }
            // arrow is being shot
            if (event.getDamager() instanceof Arrow arrow) {
                // if the shooter is a player
                if (arrow.getShooter() instanceof Player shootingPlayer) {
                    if (shootingPlayer.getGameMode() == GameMode.CREATIVE || shootingPlayer.getGameMode() == GameMode.SPECTATOR) {
                        return;
                    }
                    PlayerInventory shootingPlayerInventory = shootingPlayer.getInventory();
                    ItemStack main = shootingPlayerInventory.getItemInMainHand();
                    ItemStack offHand = shootingPlayerInventory.getItemInOffHand();
                    boolean isMain = main.getType() == Material.BOW || main.getType() == Material.CROSSBOW;
                    boolean isOffHand = offHand.getType() == Material.BOW || offHand.getType() == Material.CROSSBOW;
                    ItemStack heldBow = null;
                    if (isMain) {
                        heldBow = main;
                    }
                    if (isOffHand) {
                        heldBow = offHand;
                    }

                    // player swapped
                    if (heldBow == null) {
                        return;
                    }

                    // player is shooting another player
                    if (mobBeingAttacked instanceof Player) {
                        ItemStack newItem = toolStats.itemLore.updatePlayerKills(heldBow, 1);
                        if (newItem != null) {
                            shootingPlayerInventory.setItemInMainHand(newItem);
                        }
                    } else {
                        // player is shooting a mob
                        ItemStack newItem = toolStats.itemLore.updateMobKills(heldBow, 1);
                        if (newItem != null) {
                            shootingPlayerInventory.setItemInMainHand(newItem);
                        }
                        trackedMobs.add(mobBeingAttacked.getUniqueId());
                    }
                }
            }
        }
        // player is taken damage but not being killed
        if (mobBeingAttacked instanceof Player playerTakingDamage) {
            if (playerTakingDamage.getGameMode() == GameMode.CREATIVE || playerTakingDamage.getGameMode() == GameMode.SPECTATOR) {
                return;
            }
            PlayerInventory playerInventory = playerTakingDamage.getInventory();
            ItemStack[] armorContents = playerInventory.getArmorContents();
            for (int i = 0; i < armorContents.length; i++) {
                ItemStack armorPiece = armorContents[i];
                if (armorPiece != null) {
                    if (toolStats.itemChecker.isArmor(armorPiece.getType())) {
                        ItemStack newItem = toolStats.itemLore.updateDamage(armorPiece, event.getFinalDamage());
                        if (newItem != null) {
                            armorContents[i] = newItem;
                        }
                    }
                }
            }

            // apply the new armor
            playerInventory.setArmorContents(armorContents);
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

        // player is taken damage but not being killed
        if (mobBeingAttacked instanceof Player playerTakingDamage) {
            if (playerTakingDamage.getGameMode() == GameMode.CREATIVE || playerTakingDamage.getGameMode() == GameMode.SPECTATOR) {
                return;
            }
            PlayerInventory playerInventory = playerTakingDamage.getInventory();
            ItemStack[] armorContents = playerInventory.getArmorContents();
            for (int i = 0; i < armorContents.length; i++) {
                ItemStack armorPiece = armorContents[i];
                if (armorPiece != null) {
                    if (toolStats.itemChecker.isArmor(armorPiece.getType())) {
                        ItemStack newItem = toolStats.itemLore.updateDamage(armorPiece, event.getFinalDamage());
                        if (newItem != null) {
                            armorContents[i] = newItem;
                        }
                    }
                }
            }

            // apply the new armor
            playerInventory.setArmorContents(armorContents);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByBlockEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mobBeingAttacked)) {
            return;
        }

        // ignore void and /kill damage
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (ignoredCauses.contains(cause)) {
            return;
        }

        // player is taken damage but not being killed
        if (mobBeingAttacked instanceof Player playerTakingDamage) {
            if (playerTakingDamage.getGameMode() == GameMode.CREATIVE || playerTakingDamage.getGameMode() == GameMode.SPECTATOR) {
                return;
            }
            PlayerInventory playerInventory = playerTakingDamage.getInventory();
            ItemStack[] armorContents = playerInventory.getArmorContents();
            for (int i = 0; i < armorContents.length; i++) {
                ItemStack armorPiece = armorContents[i];
                if (armorPiece != null) {
                    if (toolStats.itemChecker.isArmor(armorPiece.getType())) {
                        ItemStack newItem = toolStats.itemLore.updateDamage(armorPiece, event.getFinalDamage());
                        if (newItem != null) {
                            armorContents[i] = newItem;
                        }
                    }
                }
            }

            // apply the new armor
            playerInventory.setArmorContents(armorContents);
        }
    }
}
