package com.plainston.gtquality;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

import gregapi.gui.ContainerClientBasicMachine;
import gregapi.gui.Slot_Render;
import gregapi.tileentity.machines.MultiTileEntityBasicMachine;

final class GuiFluidInteractionClient {

    private GuiFluidInteractionClient() {}

    static Object create(EntityPlayer player, MultiTileEntityBasicMachine machine, int guiId) {
        return new FluidMachineScreen(player, machine, guiId);
    }

    private static final class FluidMachineScreen extends ContainerClientBasicMachine {

        private boolean consumedShiftClick;

        FluidMachineScreen(EntityPlayer player, MultiTileEntityBasicMachine machine, int guiId) {
            super(player.inventory, machine, machine.mRecipes, guiId, machine.mGUITexture);
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int button) {
            if (GTQuality.guiFluidInteraction && isShiftKeyDown()
                && (button == 0 || button == 1)
                && mc.thePlayer.inventory.getItemStack() != null) {
                int x = mouseX - getLeft();
                int y = mouseY - getTop();
                for (Object entry : inventorySlots.inventorySlots) {
                    Slot slot = (Slot) entry;
                    if (slot instanceof Slot_Render && x >= slot.xDisplayPosition - 1
                        && x < slot.xDisplayPosition + 17
                        && y >= slot.yDisplayPosition - 1
                        && y < slot.yDisplayPosition + 17) {
                        mc.playerController
                            .windowClick(inventorySlots.windowId, slot.slotNumber, button, 1, mc.thePlayer);
                        consumedShiftClick = true;
                        return;
                    }
                }
            }
            super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
            if (consumedShiftClick && (state == 0 || state == 1)) {
                consumedShiftClick = false;
                return;
            }
            super.mouseMovedOrUp(mouseX, mouseY, state);
        }
    }
}
