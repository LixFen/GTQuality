package com.plainston.gtquality;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import gregapi.GT_API;
import gregapi.data.CS;
import gregapi.data.FL;
import gregapi.fluid.FluidTankGT;
import gregapi.gui.ContainerCommonBasicMachine;
import gregapi.gui.Slot_Render;
import gregapi.tileentity.ITileEntityInventoryGUI;
import gregapi.tileentity.machines.MultiTileEntityBasicMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public final class GuiFluidInteraction {

    private GuiFluidInteraction() {}

    static void install() {
        final IGuiHandler original = GT_API.api_proxy;
        NetworkRegistry.INSTANCE.registerGuiHandler(CS.GAPI, new IGuiHandler() {
            @Override
            public Object getServerGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z) {
                TileEntity tile = world.getTileEntity(x, y, z);
                if (guiId >= 0 && tile instanceof MultiTileEntityBasicMachine) {
                    MultiTileEntityBasicMachine machine = (MultiTileEntityBasicMachine) tile;
                    return new FluidMachineContainer(player.inventory, machine, guiId);
                }
                return original.getServerGuiElement(guiId, player, world, x, y, z);
            }

            @Override
            public Object getClientGuiElement(int guiId, EntityPlayer player, World world, int x, int y, int z) {
                TileEntity tile = world.getTileEntity(x, y, z);
                if (guiId >= 0 && tile instanceof MultiTileEntityBasicMachine) {
                    return GuiFluidInteractionClient.create(player, (MultiTileEntityBasicMachine) tile, guiId);
                }
                return original.getClientGuiElement(guiId, player, world, x, y, z);
            }
        });
    }

    private static final class FluidMachineContainer extends ContainerCommonBasicMachine {
        FluidMachineContainer(InventoryPlayer inventory, MultiTileEntityBasicMachine machine, int guiId) {
            super(inventory, machine, machine.mRecipes, guiId);
        }

        @Override
        public ItemStack slotClick(int slotIndex, int mouseButton, int mode, EntityPlayer player) {
            if (GTQuality.guiFluidInteraction && player instanceof EntityPlayerMP
                && (mode == 0 || mode == 1) && (mouseButton == 0 || mouseButton == 1)
                && slotIndex >= 0 && slotIndex < inventorySlots.size()
                && inventorySlots.get(slotIndex) instanceof Slot_Render) {
                click((EntityPlayerMP) player, this, slotIndex, mode == 1);
                return null;
            }
            return super.slotClick(slotIndex, mouseButton, mode, player);
        }
    }

    private static void click(EntityPlayerMP player, ContainerCommonBasicMachine container, int slotIndex, boolean shift) {
        if (!container.canInteractWith(player) || !(container.mTileEntity instanceof MultiTileEntityBasicMachine)) return;

        Slot slot = (Slot) container.inventorySlots.get(slotIndex);
        MultiTileEntityBasicMachine machine = (MultiTileEntityBasicMachine) container.mTileEntity;
        int firstFluidSlot = machine.mRecipes.mInputItemsCount + machine.mRecipes.mOutputItemsCount + 1;
        int fluidIndex = slot.getSlotIndex() - firstFluidSlot;
        if (!(slot instanceof Slot_Render) || ((Slot_Render) slot).mInventory != machine || fluidIndex < 0) return;

        boolean input = fluidIndex < machine.mTanksInput.length;
        FluidTankGT tank = input ? machine.mTanksInput[fluidIndex]
            : fluidIndex - machine.mTanksInput.length < machine.mTanksOutput.length
                ? machine.mTanksOutput[fluidIndex - machine.mTanksInput.length] : null;
        if (tank == null) return;

        ItemStack held = player.inventory.getItemStack();
        if (held == null || held.stackSize <= 0) return;
        int attempts = shift ? held.stackSize : 1;
        boolean changed = false;
        for (int i = 0; i < attempts; i++) {
            held = player.inventory.getItemStack();
            if (held == null || held.stackSize <= 0) break;
            ItemStack one = held.copy();
            one.stackSize = 1;
            Transfer transfer = input ? deposit(tank, one) : null;
            if (transfer == null) transfer = withdraw(tank, one);
            if (transfer == null) break;

            if (!shift && held.stackSize == 1) {
                player.inventory.setItemStack(transfer.result);
            } else {
                held.stackSize--;
                if (held.stackSize == 0) player.inventory.setItemStack(null);
                if (transfer.result != null && !player.inventory.addItemStackToInventory(transfer.result)) {
                    player.dropPlayerItemWithRandomChoice(transfer.result, false);
                }
            }
            changed = true;
        }
        if (changed) {
            ((ITileEntityInventoryGUI) machine).markDirtyGUI();
            player.sendContainerToPlayer(container);
        }
    }

    private static Transfer deposit(FluidTankGT tank, ItemStack one) {
        FluidStack fluid = FL.getFluid(one.copy(), true);
        if (fluid == null || fluid.amount <= 0) return null;
        int accepted = tank.fill(fluid, false);
        if (accepted <= 0) return null;

        ItemStack result;
        FluidStack toFill;
        if (one.getItem() instanceof IFluidContainerItem) {
            result = one.copy();
            toFill = ((IFluidContainerItem) result.getItem()).drain(result, accepted, true);
            if (toFill == null || toFill.amount <= 0 || toFill.amount > accepted
                || !toFill.isFluidEqual(fluid)) return null;
        } else {
            if (accepted < fluid.amount) return null;
            result = FL.getEmpty(one, true);
            toFill = fluid;
        }
        if (tank.fill(toFill, false) != toFill.amount || tank.fill(toFill, true) != toFill.amount) return null;
        return new Transfer(result);
    }

    private static Transfer withdraw(FluidTankGT tank, ItemStack one) {
        FluidStack available = tank.drain(Integer.MAX_VALUE, false);
        if (available == null || available.amount <= 0) return null;
        int before = available.amount;
        ItemStack result = FL.fill(available, one, true, true, true, true);
        int moved = before - available.amount;
        if (result == null || moved <= 0) return null;
        if (tank.drain(moved, true) == null) return null;
        return new Transfer(result);
    }

    private static final class Transfer {
        final ItemStack result;

        Transfer(ItemStack result) {
            this.result = result;
        }
    }

}
