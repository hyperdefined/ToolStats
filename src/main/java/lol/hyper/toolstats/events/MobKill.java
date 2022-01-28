package lol.hyper.toolstats.events;

import lol.hyper.toolstats.ToolStats;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class MobKill implements Listener {

    private final ToolStats toolStats;
    private final String[] validTools = {"sword", "trident", "axe"};
    private final String playerKillsLore = ChatColor.GRAY + "Player kills: " + ChatColor.DARK_GRAY + "X";
    private final String mobKillsLore = ChatColor.GRAY + "Mob kills: " + ChatColor.DARK_GRAY + "X";
    public Set<UUID> trackedMobs = new HashSet<>();

    public MobKill(ToolStats toolStats) {
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
                    updatePlayerKills(heldItem);
                    return;
                }
                // player is killing regular mob
                updateMobKills(heldItem);
                trackedMobs.add(livingEntity.getUniqueId());
            }
        }
    }

    private void updatePlayerKills(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }
        Integer playerKills = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.swordPlayerKills, PersistentDataType.INTEGER)) {
            playerKills = container.get(toolStats.swordPlayerKills, PersistentDataType.INTEGER);
        }
        if (playerKills == null) {
            return;
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
                if (lore.get(x).contains("Mob kills")) {
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
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }

    private void updateMobKills(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }
        Integer mobKills = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.swordMobKills, PersistentDataType.INTEGER)) {
            mobKills = container.get(toolStats.swordMobKills, PersistentDataType.INTEGER);
        }
        if (mobKills == null) {
            return;
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
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }
}
