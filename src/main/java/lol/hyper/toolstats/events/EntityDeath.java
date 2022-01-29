package lol.hyper.toolstats.events;

import lol.hyper.toolstats.ToolStats;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.*;

public class EntityDeath implements Listener {

    private final ToolStats toolStats;
    private final String droppedLore = ChatColor.GRAY + "Dropped by: " + ChatColor.DARK_GRAY + "X";

    public EntityDeath(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        UUID livingEntityUUID = event.getEntity().getUniqueId();
        if (toolStats.mobKill.trackedMobs.contains(livingEntityUUID)) {
            for (ItemStack current : event.getDrops()) {
                ItemMeta meta = current.getItemMeta();
                boolean hasKey = false;
                // check if the mob has one of our keys
                // this will prevent the "dropped by" tag from being added
                if (meta != null) {
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    for (NamespacedKey key : container.getKeys()) {
                        if (toolStats.keys.contains(key)) {
                            hasKey = true;
                            break;
                        }
                    }
                }
                if (hasKey) {
                    continue;
                }
                String name = current.getType().toString().toLowerCase(Locale.ROOT);
                for (String item : toolStats.craftItem.validItems) {
                    if (name.contains(item)) {
                        addLore(current, livingEntity.getName());
                    }
                }
            }
            toolStats.mobKill.trackedMobs.remove(livingEntityUUID);
        }
    }

    private void addLore(ItemStack itemStack, String mob) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }
        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
        } else {
            // if the item has no lore, create a new list and add the string
            lore = new ArrayList<>();
        }
        lore.add(droppedLore.replace("X", mob));
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }
}
