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
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class EntityDamage implements Listener {

    private final ToolStats toolStats;
    private final String[] validTools = {"sword", "trident", "axe"};
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    public final Set<UUID> trackedMobs = new HashSet<>();

    public EntityDamage(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity) event.getEntity();
        // mob is going to die
        if (livingEntity.getHealth() - event.getFinalDamage() <= 0) {
            // a player is killing something
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                if (player.getGameMode() != GameMode.SURVIVAL) {
                    return;
                }
                // a player killed something with their fist
                ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
                if (heldItem == null || heldItem.getType() == Material.AIR) {
                    return;
                }
                // check items we want
                String itemName = heldItem.getType().toString().toLowerCase();
                if (Arrays.stream(validTools).noneMatch(itemName::contains)) {
                    return;
                }
                // a player is killing another player
                if (livingEntity instanceof Player) {
                    player.getInventory().setItem(player.getInventory().getHeldItemSlot(), updatePlayerKills(heldItem));
                    return;
                }
                // player is killing regular mob
                player.getInventory().setItem(player.getInventory().getHeldItemSlot(), updateMobKills(heldItem));
                trackedMobs.add(livingEntity.getUniqueId());
            }
            // trident is being thrown at something
            if (event.getDamager() instanceof Trident) {
                Trident trident = (Trident) event.getDamager();
                ItemStack clone;
                // trident is killing player
                if (livingEntity instanceof Player) {
                    clone = updatePlayerKills(trident.getItem());
                } else {
                    clone = updateMobKills(trident.getItem());
                }
                if (clone == null) {
                    return;
                }
                trident.setItem(clone);
            }
            // arrow is being shot
            if (event.getDamager() instanceof Arrow) {
                Arrow arrow = (Arrow) event.getDamager();
                // if the shooter is a player
                if (arrow.getShooter() instanceof Player) {
                    Player player = (Player) arrow.getShooter();
                    ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
                    if (heldItem == null) {
                        return;
                    }
                    // if the player is holding the bow/crossbow
                    // if they switch then oh well
                    if (heldItem.getType() == Material.BOW || heldItem.getType() == Material.CROSSBOW) {
                        if (livingEntity instanceof Player) {
                            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), updatePlayerKills(heldItem));
                        } else {
                            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), updateMobKills(heldItem));
                        }
                    }
                }
            }
        }
        // player is taken damage but not being killed
        if (livingEntity instanceof Player) {
            Player player = (Player) livingEntity;
            PlayerInventory inventory = player.getInventory();
            for (ItemStack armor : inventory.getArmorContents()) {
                if (armor != null) {
                    updateArmorDamage(armor, event.getDamage());
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity) event.getEntity();
        // player is taken damage but not being killed
        if (livingEntity instanceof Player) {
            Player player = (Player) livingEntity;
            PlayerInventory inventory = player.getInventory();
            for (ItemStack armor : inventory.getArmorContents()) {
                if (armor != null) {
                    updateArmorDamage(armor, event.getDamage());
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByBlockEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity) event.getEntity();
        // player is taken damage but not being killed
        if (livingEntity instanceof Player) {
            Player player = (Player) livingEntity;
            PlayerInventory inventory = player.getInventory();
            for (ItemStack armor : inventory.getArmorContents()) {
                if (armor != null) {
                    updateArmorDamage(armor, event.getFinalDamage());
                }
            }
        }
    }

    /**
     * Updates a weapon's player kills.
     * @param itemStack The item to update.
     * @return A copy of the item.
     */
    private ItemStack updatePlayerKills(ItemStack itemStack) {
        ItemStack finalItem = itemStack.clone();
        ItemMeta meta = finalItem.getItemMeta();
        if (meta == null) {
            return null;
        }
        Integer playerKills = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.swordPlayerKills, PersistentDataType.INTEGER)) {
            playerKills = container.get(toolStats.swordPlayerKills, PersistentDataType.INTEGER);
        }
        if (playerKills == null) {
            return null;
        } else {
            playerKills++;
        }
        container.set(toolStats.swordPlayerKills, PersistentDataType.INTEGER, playerKills);

        String playerKillsLore = toolStats.getLoreFromConfig("kills.player", false);
        String playerKillsLoreRaw = toolStats.getLoreFromConfig("kills.player", true);

        if (playerKillsLore == null || playerKillsLoreRaw == null) {
            toolStats.logger.warning("There is no lore message for messages.kills.player!");
            return null;
        }

        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
            boolean hasLore = false;
            // we do a for loop like this, we can keep track of index
            // this doesn't mess the lore up of existing items
            for (int x = 0; x < lore.size(); x++) {
                if (lore.get(x).contains(playerKillsLore)) {
                    hasLore = true;
                    lore.set(x, playerKillsLoreRaw.replace("{kills}", Integer.toString(playerKills)));
                    break;
                }
            }
            // if the item has lore but doesn't have the tag, add it
            if (!hasLore) {
                lore.add(playerKillsLoreRaw.replace("{kills}", Integer.toString(playerKills)));
            }
        } else {
            // if the item has no lore, create a new list and add the string
            lore = new ArrayList<>();
            lore.add(playerKillsLoreRaw.replace("{kills}", Integer.toString(playerKills)));
        }
        // do we add the lore based on the config?
        if (toolStats.checkConfig(itemStack, "player-kills")) {
            meta.setLore(lore);
        }
        finalItem.setItemMeta(meta);
        return finalItem;
    }

    /**
     * Updates a weapon's mob kills.
     * @param itemStack The item to update.
     * @return A copy of the item.
     */
    private ItemStack updateMobKills(ItemStack itemStack) {
        ItemStack finalItem = itemStack.clone();
        ItemMeta meta = finalItem.getItemMeta();
        if (meta == null) {
            return null;
        }
        Integer mobKills = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.swordMobKills, PersistentDataType.INTEGER)) {
            mobKills = container.get(toolStats.swordMobKills, PersistentDataType.INTEGER);
        }
        if (mobKills == null) {
            return null;
        } else {
            mobKills++;
        }
        container.set(toolStats.swordMobKills, PersistentDataType.INTEGER, mobKills);

        String mobKillsLore = toolStats.getLoreFromConfig("kills.mob", false);
        String mobKillsLoreRaw = toolStats.getLoreFromConfig("kills.mob", true);

        if (mobKillsLore == null || mobKillsLoreRaw == null) {
            toolStats.logger.warning("There is no lore message for messages.kills.mob!");
            return null;
        }

        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
            boolean hasLore = false;
            // we do a for loop like this, we can keep track of index
            // this doesn't mess the lore up of existing items
            for (int x = 0; x < lore.size(); x++) {
                if (lore.get(x).contains(mobKillsLore)) {
                    hasLore = true;
                    lore.set(x, mobKillsLoreRaw.replace("{kills}", Integer.toString(mobKills)));
                    break;
                }
            }
            // if the item has lore but doesn't have the tag, add it
            if (!hasLore) {
                lore.add(mobKillsLoreRaw.replace("{kills}", Integer.toString(mobKills)));
            }
        } else {
            // if the item has no lore, create a new list and add the string
            lore = new ArrayList<>();
            lore.add(mobKillsLoreRaw.replace("{kills}", Integer.toString(mobKills)));
        }
        // do we add the lore based on the config?
        if (toolStats.checkConfig(itemStack, "mob-kills")) {
            meta.setLore(lore);
        }
        finalItem.setItemMeta(meta);
        return finalItem;
    }

    /**
     * Updates a player's armor damage stats.
     * @param itemStack The armor piece.
     * @param damage How much damage is being added.
     */
    private void updateArmorDamage(ItemStack itemStack, double damage) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }
        Double damageTaken = 0.0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.armorDamage, PersistentDataType.DOUBLE)) {
            damageTaken = container.get(toolStats.armorDamage, PersistentDataType.DOUBLE);
        }
        if (damageTaken == null) {
            return;
        } else {
            damageTaken = damageTaken + damage;
        }
        decimalFormat.setRoundingMode(RoundingMode.DOWN);
        container.set(toolStats.armorDamage, PersistentDataType.DOUBLE, damageTaken);

        String damageTakenLore = toolStats.getLoreFromConfig("damage-taken", false);
        String damageTakenLoreRaw = toolStats.getLoreFromConfig("damage-taken", true);

        if (damageTakenLore == null || damageTakenLoreRaw == null) {
            toolStats.logger.warning("There is no lore message for messages.damage-taken!");
            return;
        }

        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
            boolean hasLore = false;
            // we do a for loop like this, we can keep track of index
            // this doesn't mess the lore up of existing items
            for (int x = 0; x < lore.size(); x++) {
                if (lore.get(x).contains(damageTakenLore)) {
                    hasLore = true;
                    lore.set(x, damageTakenLoreRaw.replace("{damage}", decimalFormat.format(damageTaken)));
                    break;
                }
            }
            // if the item has lore but doesn't have the tag, add it
            if (!hasLore) {
                lore.add(damageTakenLoreRaw.replace("{damage}", decimalFormat.format(damageTaken)));
            }
        } else {
            // if the item has no lore, create a new list and add the string
            lore = new ArrayList<>();
            lore.add(damageTakenLoreRaw.replace("{damage}", decimalFormat.format(damageTaken)));
        }
        if (toolStats.config.getBoolean("enabled.armor-damage")) {
            meta.setLore(lore);
        }
        itemStack.setItemMeta(meta);
    }
}
