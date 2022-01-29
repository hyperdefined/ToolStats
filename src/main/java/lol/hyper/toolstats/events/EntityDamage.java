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
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
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
    private final String playerKillsLore = ChatColor.GRAY + "Player kills: " + ChatColor.DARK_GRAY + "X";
    private final String mobKillsLore = ChatColor.GRAY + "Mob kills: " + ChatColor.DARK_GRAY + "X";
    private final String damageTakenLore = ChatColor.GRAY + "Damage taken: " + ChatColor.DARK_GRAY + "X";
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    public final Set<UUID> trackedMobs = new HashSet<>();

    public EntityDamage(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        LivingEntity livingEntity = (LivingEntity) event.getEntity();
        // mob is going to die
        if (livingEntity.getHealth() - event.getFinalDamage() <= 0) {
            // a player is killing something
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                if (player.getGameMode() != GameMode.SURVIVAL) {
                    return;
                }
                ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
                if (heldItem == null || heldItem.getType() == Material.AIR) {
                    return;
                }
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
            if (event.getDamager() instanceof Trident) {
                Trident trident = (Trident) event.getDamager();
                ItemStack clone;
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
        LivingEntity livingEntity = (LivingEntity) event.getEntity();
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
        LivingEntity livingEntity = (LivingEntity) event.getEntity();
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

        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
            boolean hasLore = false;
            // we do a for loop like this, we can keep track of index
            // this doesn't mess the lore up of existing items
            for (int x = 0; x < lore.size(); x++) {
                if (lore.get(x).contains("Player kills")) {
                    hasLore = true;
                    lore.set(x, playerKillsLore.replace("X", Integer.toString(playerKills)));
                    break;
                }
            }
            // if the item has lore but doesn't have the tag, add it
            if (!hasLore) {
                lore.add(playerKillsLore.replace("X", Integer.toString(playerKills)));
            }
        } else {
            // if the item has no lore, create a new list and add the string
            lore = new ArrayList<>();
            lore.add(playerKillsLore.replace("X", Integer.toString(playerKills)));
        }
        if (toolStats.checkConfig(itemStack, "player-kills")) {
            meta.setLore(lore);
        }
        finalItem.setItemMeta(meta);
        return finalItem;
    }

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

        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
            boolean hasLore = false;
            // we do a for loop like this, we can keep track of index
            // this doesn't mess the lore up of existing items
            for (int x = 0; x < lore.size(); x++) {
                if (lore.get(x).contains("Mob kills")) {
                    hasLore = true;
                    lore.set(x, mobKillsLore.replace("X", Integer.toString(mobKills)));
                    break;
                }
            }
            // if the item has lore but doesn't have the tag, add it
            if (!hasLore) {
                lore.add(mobKillsLore.replace("X", Integer.toString(mobKills)));
            }
        } else {
            // if the item has no lore, create a new list and add the string
            lore = new ArrayList<>();
            lore.add(mobKillsLore.replace("X", Integer.toString(mobKills)));
        }
        if (toolStats.checkConfig(itemStack, "mob-kills")) {
            meta.setLore(lore);
        }
        finalItem.setItemMeta(meta);
        return finalItem;
    }

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

        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
            boolean hasLore = false;
            // we do a for loop like this, we can keep track of index
            // this doesn't mess the lore up of existing items
            for (int x = 0; x < lore.size(); x++) {
                if (lore.get(x).contains("Damage taken")) {
                    hasLore = true;
                    lore.set(x, damageTakenLore.replace("X", decimalFormat.format(damageTaken)));
                    break;
                }
            }
            // if the item has lore but doesn't have the tag, add it
            if (!hasLore) {
                lore.add(damageTakenLore.replace("X", decimalFormat.format(damageTaken)));
            }
        } else {
            // if the item has no lore, create a new list and add the string
            lore = new ArrayList<>();
            lore.add(damageTakenLore.replace("X", decimalFormat.format(damageTaken)));
        }
        if (toolStats.config.getBoolean("enabled.armor-damage")) {
            meta.setLore(lore);
        }
        itemStack.setItemMeta(meta);
    }
}
