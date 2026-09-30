package com.plainston.gtquality.integration.nei;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import com.plainston.gtquality.Tags;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.event.NEIConfigsLoadedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregapi.recipes.Recipe.RecipeMap;
import gregapi.tileentity.tools.MultiTileEntityAdvancedCraftingTable.MultiTileEntityGUIClientAdvancedCraftingTable;

public class NEI_GTQualityConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        MinecraftForge.EVENT_BUS.register(this);
        for (RecipeMap recipeMap : RecipeMap.RECIPE_MAP_LIST) {
            if (!recipeMap.mNEIAllowed) {
                continue;
            }

            for (ItemStack machine : recipeMap.mRecipeMachineList) {
                API.addRecipeCatalyst(machine, recipeMap.mNameNEI);
            }
        }
    }

    @SubscribeEvent
    public void onNEIConfigsLoaded(NEIConfigsLoadedEvent event) {
        API.registerGuiOverlayHandler(
            MultiTileEntityGUIClientAdvancedCraftingTable.class,
            new AdvancedCraftingOverlayHandler(),
            "crafting");
        MinecraftForge.EVENT_BUS.unregister(this);
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
