package com.plainston.gtquality;

import codechicken.nei.recipe.DefaultOverlayHandler;
import gregapi.tileentity.tools.MultiTileEntityAdvancedCraftingTable;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

final class AdvancedCraftingOverlayHandler extends DefaultOverlayHandler {

    AdvancedCraftingOverlayHandler() {
        super(55, 22);
    }

    @Override
    public boolean canMoveFrom(Slot slot, GuiContainer gui) {
        if (super.canMoveFrom(slot, gui)) return true;
        int index = slot.getSlotIndex();
        return slot.inventory instanceof MultiTileEntityAdvancedCraftingTable && index >= 0 && index <= 20;
    }

}
