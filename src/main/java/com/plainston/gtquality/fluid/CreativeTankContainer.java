package com.plainston.gtquality.fluid;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraftforge.fluids.FluidStack;

import com.plainston.gtquality.GTQuality;

import gregapi.gui.ContainerCommon;

public final class CreativeTankContainer extends ContainerCommon {

    public FluidStack fluid;
    private FluidStack lastFluid;

    public CreativeTankContainer(InventoryPlayer inventory, CreativeTank tank) {
        super(inventory, tank);
        fluid = tank.getSelectedFluid();
    }

    @Override
    public int addSlots(InventoryPlayer inventory) {
        return 84;
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafter) {
        super.addCraftingToCrafters(crafter);
        sendFluid(crafter, ((CreativeTank) mTileEntity).getSelectedFluid());
    }

    private void sendFluid(ICrafting crafter, FluidStack selected) {
        if (crafter instanceof EntityPlayerMP) {
            GTQuality.NETWORK.sendTo(new CreativeTankPackets.FluidState(windowId, selected), (EntityPlayerMP) crafter);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        CreativeTank tank = (CreativeTank) mTileEntity;
        FluidStack selected = tank.getSelectedFluid();
        boolean changed = selected == null ? lastFluid != null : !selected.isFluidEqual(lastFluid);
        for (Object listener : crafters) {
            ICrafting crafter = (ICrafting) listener;
            crafter.sendProgressBarUpdate(this, 0, tank.outputRate & 0xFFFF);
            crafter.sendProgressBarUpdate(this, 1, tank.outputRate >>> 16);
            crafter.sendProgressBarUpdate(this, 2, tank.autoOutput ? 1 : 0);
            if (changed) sendFluid(crafter, selected);
        }
        lastFluid = selected;
    }

    @Override
    public void updateProgressBar(int id, int value) {
        CreativeTank tank = (CreativeTank) mTileEntity;
        if (id == 0) tank.outputRate = (tank.outputRate & 0xFFFF0000) | (value & 0xFFFF);
        if (id == 1) tank.outputRate = (tank.outputRate & 0xFFFF) | ((value & 0xFFFF) << 16);
        if (id == 2) tank.autoOutput = value != 0;
    }
}
