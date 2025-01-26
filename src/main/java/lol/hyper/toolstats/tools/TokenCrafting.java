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

import lol.hyper.toolstats.ToolStats;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TokenCrafting {

    private final ToolStats toolStats;
    private final Set<ShapedRecipe> recipes = new HashSet<>();
    private final ArrayList<String> tokenTypes = new ArrayList<>();

    public TokenCrafting(ToolStats toolStats) {
        this.toolStats = toolStats;
    }

    public void setup() {
        NamespacedKey playerKillsKey = new NamespacedKey(toolStats, "player-kills-token");
        ShapedRecipe playerKillRecipe = new ShapedRecipe(playerKillsKey, toolStats.tokenItems.playerKills());
        playerKillRecipe.shape(" P ", "PSP", " P ");
        playerKillRecipe.setIngredient('P', Material.PAPER);
        playerKillRecipe.setIngredient('S', Material.WOODEN_SWORD);
        recipes.add(playerKillRecipe);

        NamespacedKey mobKillsKey = new NamespacedKey(toolStats, "mob-kills-token");
        ShapedRecipe mobKillsRecipe = new ShapedRecipe(mobKillsKey, toolStats.tokenItems.mobKills());
        mobKillsRecipe.shape(" P ", "PRP", " P ");
        mobKillsRecipe.setIngredient('P', Material.PAPER);
        mobKillsRecipe.setIngredient('R', Material.ROTTEN_FLESH);
        recipes.add(mobKillsRecipe);

        NamespacedKey blocksMinedKey = new NamespacedKey(toolStats, "blocks-mined-token");
        ShapedRecipe blocksMinedRecipe = new ShapedRecipe(blocksMinedKey, toolStats.tokenItems.blocksMined());
        blocksMinedRecipe.shape(" P ", "PSP", " P ");
        blocksMinedRecipe.setIngredient('P', Material.PAPER);
        blocksMinedRecipe.setIngredient('S', Material.WOODEN_PICKAXE);
        recipes.add(blocksMinedRecipe);

        NamespacedKey cropsMinedKey = new NamespacedKey(toolStats, "crops-mined-token");
        ShapedRecipe cropsMinedRecipe = new ShapedRecipe(cropsMinedKey, toolStats.tokenItems.cropsMined());
        cropsMinedRecipe.shape(" P ", "PHP", " P ");
        cropsMinedRecipe.setIngredient('P', Material.PAPER);
        cropsMinedRecipe.setIngredient('H', Material.WOODEN_HOE);
        recipes.add(cropsMinedRecipe);

        NamespacedKey fishCaughtKey = new NamespacedKey(toolStats, "fish-caught-token");
        ShapedRecipe fishCaughtRecipe = new ShapedRecipe(fishCaughtKey, toolStats.tokenItems.fishCaught());
        fishCaughtRecipe.shape(" P ", "PCP", " P ");
        fishCaughtRecipe.setIngredient('P', Material.PAPER);
        fishCaughtRecipe.setIngredient('C', Material.COD);
        recipes.add(fishCaughtRecipe);

        NamespacedKey sheepShearedKey = new NamespacedKey(toolStats, "sheep-sheared-token");
        ShapedRecipe sheepShearedRecipe = new ShapedRecipe(sheepShearedKey, toolStats.tokenItems.sheepSheared());
        sheepShearedRecipe.shape(" P ", "PWP", " P ");
        sheepShearedRecipe.setIngredient('P', Material.PAPER);
        sheepShearedRecipe.setIngredient('W', Material.WHITE_WOOL);
        recipes.add(sheepShearedRecipe);

        NamespacedKey armorDamageKey = new NamespacedKey(toolStats, "damage-taken-token");
        ShapedRecipe armorDamageRecipe = new ShapedRecipe(armorDamageKey, toolStats.tokenItems.damageTaken());
        armorDamageRecipe.shape(" P ", "PCP", " P ");
        armorDamageRecipe.setIngredient('P', Material.PAPER);
        armorDamageRecipe.setIngredient('C', Material.LEATHER_CHESTPLATE);
        recipes.add(armorDamageRecipe);

        NamespacedKey arrowsShotKey = new NamespacedKey(toolStats, "arrows-shot-token");
        ShapedRecipe arrowsShotRecipe = new ShapedRecipe(arrowsShotKey, toolStats.tokenItems.arrowsShot());
        arrowsShotRecipe.shape(" P ", "PAP", " P ");
        arrowsShotRecipe.setIngredient('P', Material.PAPER);
        arrowsShotRecipe.setIngredient('A', Material.ARROW);
        recipes.add(arrowsShotRecipe);

        NamespacedKey flightTimeKey = new NamespacedKey(toolStats, "flight-time-token");
        ShapedRecipe flightTimeRecipe = new ShapedRecipe(flightTimeKey, toolStats.tokenItems.flightTime());
        flightTimeRecipe.shape(" P ", "PFP", " P ");
        flightTimeRecipe.setIngredient('P', Material.PAPER);
        flightTimeRecipe.setIngredient('F', Material.FEATHER);
        recipes.add(flightTimeRecipe);

        NamespacedKey resetKey = new NamespacedKey(toolStats, "reset-token");
        ShapedRecipe resetRecipe = new ShapedRecipe(resetKey, toolStats.tokenItems.resetToken());
        resetRecipe.shape(" P ", "PPP", " P ");
        resetRecipe.setIngredient('P', Material.PAPER);
        recipes.add(resetRecipe);

        tokenTypes.add("crops-mined");
        tokenTypes.add("blocks-mined");
        tokenTypes.add("damage-taken");
        tokenTypes.add("mob-kills");
        tokenTypes.add("player-kills");
        tokenTypes.add("arrows-shot");
        tokenTypes.add("sheep-sheared");
        tokenTypes.add("flight-time");
        tokenTypes.add("fish-caught");
        tokenTypes.add("reset");
    }

    public Set<ShapedRecipe> getRecipes() {
        return recipes;
    }

    public ArrayList<String> getTokenTypes() {
        return tokenTypes;
    }
}
