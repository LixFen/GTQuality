package com.plainston.gtquality.integration.nei;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import com.plainston.gtquality.GTQuality;

import codechicken.nei.ItemPanels;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerTooltipHandler;
import gregapi.gui.ContainerClient;

public final class FilterNEIHandler implements INEIGuiHandler, IContainerTooltipHandler {

    private static Slot getFilterSlot(GuiContainer gui, int x, int y) {
        if (!(gui instanceof ContainerClient)) return null;
        ContainerClient screen = (ContainerClient) gui;
        x -= screen.getLeft();
        y -= screen.getTop();
        for (Object entry : gui.inventorySlots.inventorySlots) {
            Slot slot = (Slot) entry;
            if (FilterGhostPackets.isFilterSlot(gui.inventorySlots, slot.slotNumber) && x >= slot.xDisplayPosition - 1
                && x < slot.xDisplayPosition + 17
                && y >= slot.yDisplayPosition - 1
                && y < slot.yDisplayPosition + 17) return slot;
        }
        return null;
    }

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int x, int y, ItemStack stack, int button) {
        // NEI also calls this with a copy of the real cursor stack during ordinary clicks.
        if (stack != ItemPanels.itemPanel.draggedStack && stack != ItemPanels.bookmarkPanel.draggedStack) return false;
        if (button != 0 && button != 1) return false;
        Slot slot = getFilterSlot(gui, x, y);
        if (slot == null) return false;
        GTQuality.NETWORK
            .sendToServer(new FilterGhostPackets.Select(gui.inventorySlots.windowId, slot.slotNumber, stack));
        // Returning true stops other handlers; zero size tells PanelWidget to clear the ghost.
        stack.stackSize = 0;
        return true;
    }

    @Override
    public List<String> handleTooltip(GuiContainer gui, int x, int y, List<String> tooltip) {
        if (getFilterSlot(gui, x, y) != null && GuiContainerManager.shouldShowTooltip(gui)
            && GuiContainerManager.getStackMouseOver(gui) == null) addHint(tooltip);
        return tooltip;
    }

    @Override
    public List<String> handleItemTooltip(GuiContainer gui, ItemStack stack, int x, int y, List<String> tooltip) {
        if (getFilterSlot(gui, x, y) != null) addHint(tooltip);
        return tooltip;
    }

    private static void addHint(List<String> tooltip) {
        tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("gtquality.nei.filter.ghost"));
    }
}
