package com.plainston.gtquality;

import net.minecraft.item.ItemStack;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import gregapi.recipes.Recipe.RecipeMap;

public class NEI_GTQualityConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        for (RecipeMap recipeMap : RecipeMap.RECIPE_MAP_LIST) {
            if (!recipeMap.mNEIAllowed) {
                continue;
            }

            for (ItemStack machine : recipeMap.mRecipeMachineList) {
                API.addRecipeCatalyst(machine, recipeMap.mNameNEI);
            }
        }
    }

    @Override
    public String getName() {
        return "GTQuality NEI Plugin";
    }

    @Override
    public String getVersion() {
        return Tags.VERSION;
    }
}
