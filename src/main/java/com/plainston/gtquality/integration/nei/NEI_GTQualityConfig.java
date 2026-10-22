package com.plainston.gtquality.integration.nei;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import com.plainston.gtquality.GTQuality;
import com.plainston.gtquality.Tags;
import com.plainston.gtquality.integration.blockrenderer.GT6MultiblockHandler;

import codechicken.nei.api.API;
import codechicken.nei.api.GuiInfo;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.event.NEIConfigsLoadedEvent;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.RecipeCatalysts;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregapi.recipes.Recipe.RecipeMap;
import gregapi.tileentity.tools.MultiTileEntityAdvancedCraftingTable.MultiTileEntityGUIClientAdvancedCraftingTable;

public class NEI_GTQualityConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        GT6ItemGroups.register();
        if (GTQuality.neiWorldgenDisplay) WorldgenNEIHandler.register();
        if (GTQuality.neiMultiblockPreview && Loader.isModLoaded("blockrenderer6343")) {
            GT6MultiblockHandler.register();
        }
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
    }

    @SubscribeEvent
    public void onNEIConfigsLoaded(NEIConfigsLoadedEvent event) {
        // NEI loads its default catalysts after loadConfig(). Replace them only once that load has completed.
        Map<String, List<ItemStack>> machinesByHandler = new LinkedHashMap<>();
        for (RecipeMap recipeMap : RecipeMap.RECIPE_MAP_LIST) {
            if (!recipeMap.mNEIAllowed) {
                continue;
            }
            machinesByHandler.computeIfAbsent(recipeMap.mNameNEI, id -> new ArrayList<>())
                .addAll(recipeMap.mRecipeMachineList);
        }
        machinesByHandler.forEach(RecipeCatalysts::putRecipeCatalysts);
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
