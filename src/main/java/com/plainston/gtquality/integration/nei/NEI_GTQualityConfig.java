package com.plainston.gtquality.integration.nei;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import com.plainston.gtquality.GTQuality;
import com.plainston.gtquality.Tags;

import codechicken.nei.api.API;
import codechicken.nei.api.GuiInfo;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.event.NEIConfigsLoadedEvent;
import codechicken.nei.guihook.GuiContainerManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregapi.recipes.Recipe.RecipeMap;
import gregapi.tileentity.tools.MultiTileEntityAdvancedCraftingTable.MultiTileEntityGUIClientAdvancedCraftingTable;

public class NEI_GTQualityConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        if (GTQuality.neiWorldgenDisplay) WorldgenNEIHandler.register();
        API.registerNEIGuiHandler(new CreativeTankNEIHandler());
        FilterNEIHandler filterHandler = new FilterNEIHandler();
        // Run before NEI's CheatItemHandler, which can consume or modify the dragged stack.
        GuiInfo.writeLock.lock();
        try {
            GuiInfo.guiHandlers.addFirst(filterHandler);
        } finally {
            GuiInfo.writeLock.unlock();
        }
        GuiContainerManager.addTooltipHandler(filterHandler);
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
