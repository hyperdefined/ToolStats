package lol.hyper.toolstats.events;

import lol.hyper.toolstats.ToolStats;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class AnvilCombine implements Listener {

    private final ToolStats toolStats;

    public AnvilCombine(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    @EventHandler
    public void onCombine(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack firstSlot = inventory.getItem(0);
        ItemStack secondSlot = inventory.getItem(1);
        if (firstSlot == null || secondSlot == null) {
            return;
        }

        if (firstSlot.getType() == secondSlot.getType()) {
            // combine the tool stats
        }
    }
}
