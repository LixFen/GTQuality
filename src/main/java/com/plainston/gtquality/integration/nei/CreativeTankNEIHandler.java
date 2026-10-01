package com.plainston.gtquality.integration.nei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import com.plainston.gtquality.fluid.CreativeTankScreen;

import codechicken.nei.api.INEIGuiHandler;

public final class CreativeTankNEIHandler implements INEIGuiHandler {

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int x, int y, ItemStack stack, int button) {
        if (!(gui instanceof CreativeTankScreen)) return false;
        CreativeTankScreen screen = (CreativeTankScreen) gui;
        if (!screen.overFluid(x, y)) return false;
        if (screen.select(stack)) {
            // NEI clears its dragged ghost only when the handler sets its size to zero.
            stack.stackSize = 0;
        }
        return true;
    }
}
