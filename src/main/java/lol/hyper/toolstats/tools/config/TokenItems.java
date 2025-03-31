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
        List<Component> lore = toolStats.configTools.getTokenLore("player-kills");
        tokenMeta.displayName(title);
        tokenMeta.lore(lore);

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
        List<Component> lore = toolStats.configTools.getTokenLore("mob-kills");
        tokenMeta.displayName(title);
        tokenMeta.lore(lore);

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
        List<Component> lore = toolStats.configTools.getTokenLore("blocks-mined");
        tokenMeta.displayName(title);
        tokenMeta.lore(lore);

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
        List<Component> lore = toolStats.configTools.getTokenLore("crops-mined");
        tokenMeta.displayName(title);
        tokenMeta.lore(lore);

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
        List<Component> lore = toolStats.configTools.getTokenLore("fight-caught");
        tokenMeta.displayName(title);
        tokenMeta.lore(lore);

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
        List<Component> lore = toolStats.configTools.getTokenLore("sheep-sheared");
        tokenMeta.displayName(title);
        tokenMeta.lore(lore);

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
        List<Component> lore = toolStats.configTools.getTokenLore("damage-taken");
        tokenMeta.displayName(title);
        tokenMeta.lore(lore);

        // set the PDC
        tokenData.set(toolStats.tokenType, PersistentDataType.STRING, "damage-taken");
        token.setItemMeta(tokenMeta);
        return token;
    }

    public ItemStack damageDone() {
        // set up the item
        ItemStack token = new ItemStack(Material.PAPER);
        ItemMeta tokenMeta = token.getItemMeta();
        PersistentDataContainer tokenData = tokenMeta.getPersistentDataContainer();

        // set the title and lore
        Component title = toolStats.configTools.format("tokens.data.damage-done.title");
        List<Component> lore = toolStats.configTools.getTokenLore("damage-done");
        tokenMeta.displayName(title);
        tokenMeta.lore(lore);

        // set the PDC
        tokenData.set(toolStats.tokenType, PersistentDataType.STRING, "damage-done");
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
        List<Component> lore = toolStats.configTools.getTokenLore("arrows-shot");
        tokenMeta.displayName(title);
        tokenMeta.lore(lore);

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
        List<Component> lore = toolStats.configTools.getTokenLore("flight-time");
        tokenMeta.displayName(title);
        tokenMeta.lore(lore);

        // set the PDC
        tokenData.set(toolStats.tokenType, PersistentDataType.STRING, "flight-time");
        token.setItemMeta(tokenMeta);
        return token;
    }

    public ItemStack resetToken() {
        // set up the item
        ItemStack token = new ItemStack(Material.PAPER);
        ItemMeta tokenMeta = token.getItemMeta();
        PersistentDataContainer tokenData = tokenMeta.getPersistentDataContainer();

        // set the title and lore
        Component title = toolStats.configTools.format("tokens.data.reset.title");
        List<Component> lore = toolStats.configTools.getTokenLore("reset");
        tokenMeta.displayName(title);
        tokenMeta.lore(lore);

        // set the PDC
        tokenData.set(toolStats.tokenType, PersistentDataType.STRING, "reset");
        token.setItemMeta(tokenMeta);
        return token;
    }

    public ItemStack removeToken() {
        // set up the item
        ItemStack token = new ItemStack(Material.PAPER);
        ItemMeta tokenMeta = token.getItemMeta();
        PersistentDataContainer tokenData = tokenMeta.getPersistentDataContainer();

        // set the title and lore
        Component title = toolStats.configTools.format("tokens.data.remove.title");
        List<Component> lore = toolStats.configTools.getTokenLore("remove");
        tokenMeta.displayName(title);
        tokenMeta.lore(lore);

        // set the PDC
        tokenData.set(toolStats.tokenType, PersistentDataType.STRING, "remove");
        token.setItemMeta(tokenMeta);
        return token;
    }
}
