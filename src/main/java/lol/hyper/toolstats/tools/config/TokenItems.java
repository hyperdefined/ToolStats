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

package lol.hyper.toolstats.tools.config;

import lol.hyper.toolstats.ToolStats;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class TokenItems {

    private final ToolStats toolStats;

    public TokenItems(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    public ItemStack playerKills() {
        // set up the item
        ItemStack token = new ItemStack(Material.PAPER);
        ItemMeta tokenMeta = token.getItemMeta();
        PersistentDataContainer tokenData = tokenMeta.getPersistentDataContainer();

        // set the title and lore
        Component title = toolStats.configTools.format("tokens.data.player-kills.title");
        Component lore = toolStats.configTools.format("tokens.data.player-kills.lore");
        tokenMeta.displayName(title);
        List<Component> newLore = new ArrayList<>();
        newLore.add(lore);
        tokenMeta.lore(newLore);

        // set the PDC
        tokenData.set(toolStats.tokenType, PersistentDataType.STRING, "player-kills");
        token.setItemMeta(tokenMeta);
        return token;
    }

    public ItemStack mobKills() {
        // set up the item
        ItemStack token = new ItemStack(Material.PAPER);
        ItemMeta tokenMeta = token.getItemMeta();
        PersistentDataContainer tokenData = tokenMeta.getPersistentDataContainer();

        // set the title and lore
        Component title = toolStats.configTools.format("tokens.data.mob-kills.title");
        Component lore = toolStats.configTools.format("tokens.data.mob-kills.lore");
        tokenMeta.displayName(title);
        List<Component> newLore = new ArrayList<>();
        newLore.add(lore);
        tokenMeta.lore(newLore);

        // set the PDC
        tokenData.set(toolStats.tokenType, PersistentDataType.STRING, "mob-kills");
        token.setItemMeta(tokenMeta);
        return token;
    }

    public ItemStack blocksMined() {
        // set up the item
        ItemStack token = new ItemStack(Material.PAPER);
        ItemMeta tokenMeta = token.getItemMeta();
        PersistentDataContainer tokenData = tokenMeta.getPersistentDataContainer();

        // set the title and lore
        Component title = toolStats.configTools.format("tokens.data.blocks-mined.title");
        Component lore = toolStats.configTools.format("tokens.data.blocks-mined.lore");
        tokenMeta.displayName(title);
        List<Component> newLore = new ArrayList<>();
        newLore.add(lore);
        tokenMeta.lore(newLore);

        // set the PDC
        tokenData.set(toolStats.tokenType, PersistentDataType.STRING, "blocks-mined");
        token.setItemMeta(tokenMeta);
        return token;
    }

    public ItemStack cropsMined() {
        // set up the item
        ItemStack token = new ItemStack(Material.PAPER);
        ItemMeta tokenMeta = token.getItemMeta();
        PersistentDataContainer tokenData = tokenMeta.getPersistentDataContainer();

        // set the title and lore
        Component title = toolStats.configTools.format("tokens.data.crops-mined.title");
        Component lore = toolStats.configTools.format("tokens.data.crops-mined.lore");
        tokenMeta.displayName(title);
        List<Component> newLore = new ArrayList<>();
        newLore.add(lore);
        tokenMeta.lore(newLore);

        // set the PDC
        tokenData.set(toolStats.tokenType, PersistentDataType.STRING, "crops-mined");
        token.setItemMeta(tokenMeta);
        return token;
    }

    public ItemStack fishCaught() {
        // set up the item
        ItemStack token = new ItemStack(Material.PAPER);
        ItemMeta tokenMeta = token.getItemMeta();
        PersistentDataContainer tokenData = tokenMeta.getPersistentDataContainer();

        // set the title and lore
        Component title = toolStats.configTools.format("tokens.data.fish-caught.title");
        Component lore = toolStats.configTools.format("tokens.data.fish-caught.lore");
        tokenMeta.displayName(title);
        List<Component> newLore = new ArrayList<>();
        newLore.add(lore);
        tokenMeta.lore(newLore);

        // set the PDC
        tokenData.set(toolStats.tokenType, PersistentDataType.STRING, "fish-caught");
        token.setItemMeta(tokenMeta);
        return token;
    }

    public ItemStack sheepSheared() {
        // set up the item
        ItemStack token = new ItemStack(Material.PAPER);
        ItemMeta tokenMeta = token.getItemMeta();
        PersistentDataContainer tokenData = tokenMeta.getPersistentDataContainer();

        // set the title and lore
        Component title = toolStats.configTools.format("tokens.data.sheep-sheared.title");
        Component lore = toolStats.configTools.format("tokens.data.sheep-sheared.lore");
        tokenMeta.displayName(title);
        List<Component> newLore = new ArrayList<>();
        newLore.add(lore);
        tokenMeta.lore(newLore);

        // set the PDC
        tokenData.set(toolStats.tokenType, PersistentDataType.STRING, "sheep-sheared");
        token.setItemMeta(tokenMeta);
        return token;
    }

    public ItemStack damageTaken() {
        // set up the item
        ItemStack token = new ItemStack(Material.PAPER);
        ItemMeta tokenMeta = token.getItemMeta();
        PersistentDataContainer tokenData = tokenMeta.getPersistentDataContainer();

        // set the title and lore
        Component title = toolStats.configTools.format("tokens.data.damage-taken.title");
        Component lore = toolStats.configTools.format("tokens.data.damage-taken.lore");
        tokenMeta.displayName(title);
        List<Component> newLore = new ArrayList<>();
        newLore.add(lore);
        tokenMeta.lore(newLore);

        // set the PDC
        tokenData.set(toolStats.tokenType, PersistentDataType.STRING, "damage-taken");
        token.setItemMeta(tokenMeta);
        return token;
    }

    public ItemStack arrowsShot() {
        // set up the item
        ItemStack token = new ItemStack(Material.PAPER);
        ItemMeta tokenMeta = token.getItemMeta();
        PersistentDataContainer tokenData = tokenMeta.getPersistentDataContainer();

        // set the title and lore
        Component title = toolStats.configTools.format("tokens.data.arrows-shot.title");
        Component lore = toolStats.configTools.format("tokens.data.arrows-shot.lore");
        tokenMeta.displayName(title);
        List<Component> newLore = new ArrayList<>();
        newLore.add(lore);
        tokenMeta.lore(newLore);

        // set the PDC
        tokenData.set(toolStats.tokenType, PersistentDataType.STRING, "arrows-shot");
        token.setItemMeta(tokenMeta);
        return token;
    }

    public ItemStack flightTime() {
        // set up the item
        ItemStack token = new ItemStack(Material.PAPER);
        ItemMeta tokenMeta = token.getItemMeta();
        PersistentDataContainer tokenData = tokenMeta.getPersistentDataContainer();

        // set the title and lore
        Component title = toolStats.configTools.format("tokens.data.flight-time.title");
        Component lore = toolStats.configTools.format("tokens.data.flight-time.lore");
        tokenMeta.displayName(title);
        List<Component> newLore = new ArrayList<>();
        newLore.add(lore);
        tokenMeta.lore(newLore);

        // set the PDC
        tokenData.set(toolStats.tokenType, PersistentDataType.STRING, "flight-time");
        token.setItemMeta(tokenMeta);
        return token;
    }
}
