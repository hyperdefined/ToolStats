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

package lol.hyper.toolstats.tools;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import lol.hyper.toolstats.ToolStats;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class TokenData {

    private final ToolStats toolStats;
    private final Set<ShapedRecipe> recipes = new HashSet<>();
    private final ArrayList<String> tokenTypes = new ArrayList<>();

    public TokenData(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    public void setup() {
        NamespacedKey playerKillsKey = new NamespacedKey(toolStats, "player-kills-token");
        ShapedRecipe playerKillRecipe = new ShapedRecipe(playerKillsKey, createToken("player-kills"));
        playerKillRecipe.shape(" P ", "PSP", " P ");
        playerKillRecipe.setIngredient('P', Material.PAPER);
        playerKillRecipe.setIngredient('S', Material.WOODEN_SWORD);
        recipes.add(playerKillRecipe);

        NamespacedKey mobKillsKey = new NamespacedKey(toolStats, "mob-kills-token");
        ShapedRecipe mobKillsRecipe = new ShapedRecipe(mobKillsKey, createToken("mob-kills"));
        mobKillsRecipe.shape(" P ", "PRP", " P ");
        mobKillsRecipe.setIngredient('P', Material.PAPER);
        mobKillsRecipe.setIngredient('R', Material.ROTTEN_FLESH);
        recipes.add(mobKillsRecipe);

        NamespacedKey blocksMinedKey = new NamespacedKey(toolStats, "blocks-mined-token");
        ShapedRecipe blocksMinedRecipe = new ShapedRecipe(blocksMinedKey, createToken("blocks-mined"));
        blocksMinedRecipe.shape(" P ", "PSP", " P ");
        blocksMinedRecipe.setIngredient('P', Material.PAPER);
        blocksMinedRecipe.setIngredient('S', Material.WOODEN_PICKAXE);
        recipes.add(blocksMinedRecipe);

        NamespacedKey cropsMinedKey = new NamespacedKey(toolStats, "crops-mined-token");
        ShapedRecipe cropsMinedRecipe = new ShapedRecipe(cropsMinedKey, createToken("crops-mined"));
        cropsMinedRecipe.shape(" P ", "PHP", " P ");
        cropsMinedRecipe.setIngredient('P', Material.PAPER);
        cropsMinedRecipe.setIngredient('H', Material.WOODEN_HOE);
        recipes.add(cropsMinedRecipe);

        NamespacedKey fishCaughtKey = new NamespacedKey(toolStats, "fish-caught-token");
        ShapedRecipe fishCaughtRecipe = new ShapedRecipe(fishCaughtKey, createToken("fish-caught"));
        fishCaughtRecipe.shape(" P ", "PCP", " P ");
        fishCaughtRecipe.setIngredient('P', Material.PAPER);
        fishCaughtRecipe.setIngredient('C', Material.COD);
        recipes.add(fishCaughtRecipe);

        NamespacedKey sheepShearedKey = new NamespacedKey(toolStats, "sheep-sheared-token");
        ShapedRecipe sheepShearedRecipe = new ShapedRecipe(sheepShearedKey, createToken("sheep-sheared"));
        sheepShearedRecipe.shape(" P ", "PWP", " P ");
        sheepShearedRecipe.setIngredient('P', Material.PAPER);
        sheepShearedRecipe.setIngredient('W', Material.WHITE_WOOL);
        recipes.add(sheepShearedRecipe);

        NamespacedKey armorDamageKey = new NamespacedKey(toolStats, "damage-taken-token");
        ShapedRecipe armorDamageRecipe = new ShapedRecipe(armorDamageKey, createToken("damage-taken"));
        armorDamageRecipe.shape(" P ", "PCP", " P ");
        armorDamageRecipe.setIngredient('P', Material.PAPER);
        armorDamageRecipe.setIngredient('C', Material.LEATHER_CHESTPLATE);
        recipes.add(armorDamageRecipe);

        NamespacedKey damageDoneKey = new NamespacedKey(toolStats, "damage-done-token");
        ShapedRecipe damageDoneRecipe = new ShapedRecipe(damageDoneKey, createToken("damage-done"));
        damageDoneRecipe.shape(" P ", "PSP", " P ");
        damageDoneRecipe.setIngredient('P', Material.PAPER);
        damageDoneRecipe.setIngredient('S', Material.SHIELD);
        recipes.add(damageDoneRecipe);

        NamespacedKey arrowsShotKey = new NamespacedKey(toolStats, "arrows-shot-token");
        ShapedRecipe arrowsShotRecipe = new ShapedRecipe(arrowsShotKey, createToken("arrows-shot"));
        arrowsShotRecipe.shape(" P ", "PAP", " P ");
        arrowsShotRecipe.setIngredient('P', Material.PAPER);
        arrowsShotRecipe.setIngredient('A', Material.ARROW);
        recipes.add(arrowsShotRecipe);

        NamespacedKey flightTimeKey = new NamespacedKey(toolStats, "flight-time-token");
        ShapedRecipe flightTimeRecipe = new ShapedRecipe(flightTimeKey, createToken("flight-time"));
        flightTimeRecipe.shape(" P ", "PFP", " P ");
        flightTimeRecipe.setIngredient('P', Material.PAPER);
        flightTimeRecipe.setIngredient('F', Material.FEATHER);
        recipes.add(flightTimeRecipe);

        NamespacedKey resetKey = new NamespacedKey(toolStats, "reset-token");
        ShapedRecipe resetRecipe = new ShapedRecipe(resetKey, createToken("reset"));
        resetRecipe.shape(" P ", "PPP", " P ");
        resetRecipe.setIngredient('P', Material.PAPER);
        recipes.add(resetRecipe);

        NamespacedKey removeKey = new NamespacedKey(toolStats, "remove-token");
        ShapedRecipe removeRecipe = new ShapedRecipe(removeKey, createToken("remove"));
        removeRecipe.shape(" P ", "P P", " P ");
        removeRecipe.setIngredient('P', Material.PAPER);
        recipes.add(removeRecipe);

        tokenTypes.add("crops-mined");
        tokenTypes.add("blocks-mined");
        tokenTypes.add("damage-taken");
        tokenTypes.add("damage-done");
        tokenTypes.add("mob-kills");
        tokenTypes.add("player-kills");
        tokenTypes.add("arrows-shot");
        tokenTypes.add("sheep-sheared");
        tokenTypes.add("flight-time");
        tokenTypes.add("fish-caught");
        tokenTypes.add("reset");
        tokenTypes.add("remove");
    }

    public Set<ShapedRecipe> getRecipes() {
        return recipes;
    }

    public ArrayList<String> getTokenTypes() {
        return tokenTypes;
    }

    public ItemStack createToken(String tokenType) {
        // we don't have to check if the token exists
        // we do that prior
        ConfigurationSection tokenConfig = toolStats.config.getConfigurationSection("tokens.data." + tokenType);

        String materialFromConfig = tokenConfig.getString("material");
        if (materialFromConfig == null) {
            toolStats.logger.warning("Could not find material config for token " + tokenType);
            toolStats.logger.warning("Using PAPER as default.");
            materialFromConfig = "PAPER";
        }
        Material material = Material.getMaterial(materialFromConfig);
        if (material == null) {
            toolStats.logger.warning("Material " + materialFromConfig + " is not a valid Minecraft material.");
            toolStats.logger.warning("Using PAPER as default.");
            material = Material.PAPER;
        }

        ItemStack token = new ItemStack(material);
        ItemMeta tokenMeta = token.getItemMeta();
        PersistentDataContainer tokenData = tokenMeta.getPersistentDataContainer();

        // set the title and lore
        Component title = toolStats.configTools.format("tokens.data." + tokenType + ".title");
        List<Component> lore = toolStats.configTools.getTokenLore(tokenType);
        tokenMeta.displayName(title);
        tokenMeta.lore(lore);

        // set the custom model data
        if (tokenConfig.getBoolean("custom-model-data.enabled")) {
            String type = tokenConfig.getString("custom-model-data.type");
            Object value = tokenConfig.get("custom-model-data.value");
            if (type == null || value == null) {
                toolStats.logger.info("Could not find custom model data for token " + tokenType);
                toolStats.logger.info("Type: " + type);
                toolStats.logger.info("Value: " + value);
                return null;
            }
            CustomModelData data = setData(type, value);
            if (data != null) {
                token.setData(DataComponentTypes.CUSTOM_MODEL_DATA, data);
            } else {
                return null;
            }
        }

        // set the PDC
        tokenData.set(toolStats.tokenType, PersistentDataType.STRING, tokenType);
        token.setItemMeta(tokenMeta);
        return token;
    }

    private CustomModelData setData(String type, Object data) {
        switch (type.toLowerCase(Locale.ROOT)) {
            case "float": {
                Float f;
                if (data instanceof Float) {
                    f = (Float) data;
                } else {
                    toolStats.logger.info(type + " is not a valid float!");
                    return null;
                }
                return CustomModelData.customModelData().addFloat(f).build();
            }
            case "string": {
                String s;
                if (data instanceof String) {
                    s = (String) data;
                } else {
                    toolStats.logger.info(type + " is not a valid string!");
                    return null;
                }
                return CustomModelData.customModelData().addString(s).build();
            }
            default: {
                toolStats.logger.info(type + " is not a valid data type!");
                return null;
            }
        }
    }
}
