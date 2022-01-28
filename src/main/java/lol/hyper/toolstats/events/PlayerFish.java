package lol.hyper.toolstats.events;

import lol.hyper.toolstats.ToolStats;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class PlayerFish implements Listener {

    private final ToolStats toolStats;
    private final String fishCaughtLore = ChatColor.GRAY + "Fish caught: " + ChatColor.DARK_GRAY + "X";

    public PlayerFish(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        // only listen to when a player catches a fish
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
        if (heldItem == null || heldItem.getType() == Material.AIR || heldItem.getType() != Material.FISHING_ROD) {
            return;
        }
        addLore(heldItem);
    }

    private void addLore(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }
        Integer fishCaught = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.fishingRodCaught, PersistentDataType.INTEGER)) {
            fishCaught = container.get(toolStats.fishingRodCaught, PersistentDataType.INTEGER);
        }
        if (fishCaught == null) {
            return;
        } else {
            fishCaught++;
        }
        container.set(toolStats.fishingRodCaught, PersistentDataType.INTEGER, fishCaught);
        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
            boolean hasLore = false;
            // we do a for loop like this, we can keep track of index
            // this doesn't mess the lore up of existing items
            for (int x = 0; x < lore.size(); x++) {
                if (lore.get(x).contains("Fish caught")) {
                    hasLore = true;
                    lore.set(x, fishCaughtLore.replace("X", Integer.toString(fishCaught)));
                    break;
                }
            }
            // if the item has lore but doesn't have the tag, add it
            if (!hasLore) {
                lore.add(fishCaughtLore.replace("X", Integer.toString(fishCaught)));
            }
        } else {
            // if the item has no lore, create a new list and add the string
            lore = new ArrayList<>();
            lore.add(fishCaughtLore.replace("X", Integer.toString(fishCaught)));
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }
}
