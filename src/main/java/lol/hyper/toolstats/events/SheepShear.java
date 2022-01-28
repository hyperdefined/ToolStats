package lol.hyper.toolstats.events;

import lol.hyper.toolstats.ToolStats;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class SheepShear implements Listener {

    private final ToolStats toolStats;
    private final String sheepShearLore = ChatColor.GRAY + "Sheep sheared: " + ChatColor.DARK_GRAY + "X";

    public SheepShear(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onShear(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Sheep)) {
            return;
        }
        ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
        if (heldItem == null || heldItem.getType() == Material.AIR || heldItem.getType() != Material.SHEARS) {
            return;
        }
        addLore(heldItem);
    }

    private void addLore(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }
        Integer sheepSheared = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.shearsSheared, PersistentDataType.INTEGER)) {
            sheepSheared = container.get(toolStats.shearsSheared, PersistentDataType.INTEGER);
        }
        if (sheepSheared == null) {
            return;
        } else {
            sheepSheared++;
        }
        container.set(toolStats.shearsSheared, PersistentDataType.INTEGER, sheepSheared);
        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
            boolean hasLore = false;
            // we do a for loop like this, we can keep track of index
            // this doesn't mess the lore up of existing items
            for (int x = 0; x < lore.size(); x++) {
                if (lore.get(x).contains("Sheep sheared")) {
                    hasLore = true;
                    lore.set(x, sheepShearLore.replace("X", Integer.toString(sheepSheared)));
                    break;
                }
            }
            // if the item has lore but doesn't have the tag, add it
            if (!hasLore) {
                lore.add(sheepShearLore.replace("X", Integer.toString(sheepSheared)));
            }
        } else {
            // if the item has no lore, create a new list and add the string
            lore = new ArrayList<>();
            lore.add(sheepShearLore.replace("X", Integer.toString(sheepSheared)));
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }
}
