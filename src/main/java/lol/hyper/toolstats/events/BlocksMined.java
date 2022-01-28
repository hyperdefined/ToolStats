package lol.hyper.toolstats.events;

import lol.hyper.toolstats.ToolStats;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlocksMined implements Listener {

    private final ToolStats toolStats;
    private final String[] validTools = {"pickaxe", "axe", "hoe", "shovel"};
    private final String blocksMinedLore = ChatColor.GRAY + "Blocks mined: " + ChatColor.DARK_GRAY + "X";

    public BlocksMined(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
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
        updateBlocksMined(heldItem);
    }

    private void updateBlocksMined(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }
        Integer blocksMined = 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(toolStats.genericMined, PersistentDataType.INTEGER)) {
            blocksMined = container.get(toolStats.genericMined, PersistentDataType.INTEGER);
        }
        if (blocksMined == null) {
            return;
        } else {
            blocksMined++;
        }
        container.set(toolStats.genericMined, PersistentDataType.INTEGER, blocksMined);

        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            assert lore != null;
            boolean hasLore = false;
            // we do a for loop like this, we can keep track of index
            // this doesn't mess the lore up of existing items
            for (int x = 0; x < lore.size(); x++) {
                if (lore.get(x).contains("Blocks mined")) {
                    hasLore = true;
                    lore.set(x, blocksMinedLore.replace("X", Integer.toString(blocksMined)));
                    break;
                }
            }
            // if the item has lore but doesn't have the tag, add it
            if (!hasLore) {
                lore.add(blocksMinedLore.replace("X", Integer.toString(blocksMined)));
            }
        } else {
            // if the item has no lore, create a new list and add the string
            lore = new ArrayList<>();
            lore.add(blocksMinedLore.replace("X", Integer.toString(blocksMined)));
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
    }
}
