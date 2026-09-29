package com.plainston.gtquality;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.wdmla.api.IWDMlaClientRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaCommonRegistration;
import com.gtnewhorizons.wdmla.api.IWDMlaPlugin;
import com.gtnewhorizons.wdmla.api.WDMlaPlugin;
import com.gtnewhorizons.wdmla.api.accessor.Accessor;
import com.gtnewhorizons.wdmla.api.provider.IClientExtensionProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerExtensionProvider;
import com.gtnewhorizons.wdmla.api.view.ClientViewGroup;
import com.gtnewhorizons.wdmla.api.view.ItemView;
import com.gtnewhorizons.wdmla.api.view.ViewGroup;

import gregapi.block.multitileentity.example.MultiTileEntityChest;
import gregapi.tileentity.inventories.MultiTileEntityMassStorage;
import gregapi.tileentity.connectors.MultiTileEntityPipeFluid;
import gregapi.tileentity.machines.MultiTileEntityBasicMachine;
import gregapi.tileentity.multiblocks.MultiTileEntityMultiBlockPart;
import gregapi.tileentity.tank.TileEntityBase08Barrel;
import gregapi.tileentity.tank.TileEntityBase08FluidContainer;
import gregapi.util.UT;
import gregtech.tileentity.energy.converters.MultiTileEntityBoilerTank;
import gregtech.tileentity.energy.converters.MultiTileEntityEngineSteam;
import gregtech.tileentity.energy.converters.MultiTileEntityTurbineSteam;
import gregtech.tileentity.energy.generators.MultiTileEntityGeneratorFluidBed;
import gregtech.tileentity.energy.generators.MultiTileEntityGeneratorLiquid;
import gregtech.tileentity.energy.generators.MultiTileEntityMotorLiquid;
import gregtech.tileentity.energy.reactors.MultiTileEntityReactorCore;
import gregtech.tileentity.inventories.MultiTileEntityDrawerQuad;
import gregtech.tileentity.multiblocks.MultiTileEntityCrucible;
import gregtech.tileentity.multiblocks.MultiTileEntityTank;
import gregtech.tileentity.tools.MultiTileEntityMixingBowl;

@WDMlaPlugin(uid = "gtquality", dependencies = { "gtquality", "gregtech" })
public class GTQualityWDMlaPlugin implements IWDMlaPlugin {

    private static final StorageProvider STORAGE = new StorageProvider();
    private static final GT6WDMlaProvider GT6_INFO = GT6WDMlaProvider.INSTANCE;
    private static final GT6FluidStorageProvider GT6_FLUIDS = GT6FluidStorageProvider.INSTANCE;

    @Override
    public void register(IWDMlaCommonRegistration registration) {
        registration.registerItemStorage(STORAGE, MultiTileEntityMassStorage.class);
        registration.registerItemStorage(STORAGE, MultiTileEntityChest.class);
        registration.registerItemStorage(STORAGE, MultiTileEntityDrawerQuad.class);
        registration.registerBlockDataProvider(GT6_INFO, Block.class);
        registration.registerFluidStorage(GT6_FLUIDS, TileEntityBase08FluidContainer.class);
        registration.registerFluidStorage(GT6_FLUIDS, MultiTileEntityBasicMachine.class);
        registration.registerFluidStorage(GT6_FLUIDS, TileEntityBase08Barrel.class);
        registration.registerFluidStorage(GT6_FLUIDS, MultiTileEntityBoilerTank.class);
        registration.registerFluidStorage(GT6_FLUIDS, MultiTileEntityGeneratorFluidBed.class);
        registration.registerFluidStorage(GT6_FLUIDS, MultiTileEntityGeneratorLiquid.class);
        registration.registerFluidStorage(GT6_FLUIDS, MultiTileEntityTank.class);
        registration.registerFluidStorage(GT6_FLUIDS, MultiTileEntityMultiBlockPart.class);
        registration.registerFluidStorage(GT6_FLUIDS, MultiTileEntityEngineSteam.class);
        registration.registerFluidStorage(GT6_FLUIDS, MultiTileEntityTurbineSteam.class);
        registration.registerFluidStorage(GT6_FLUIDS, MultiTileEntityMixingBowl.class);
        registration.registerFluidStorage(GT6_FLUIDS, MultiTileEntityPipeFluid.class);
        registration.registerFluidStorage(GT6_FLUIDS, MultiTileEntityMotorLiquid.class);
        registration.registerFluidStorage(GT6_FLUIDS, MultiTileEntityReactorCore.class);
        registration.registerFluidStorage(GT6_FLUIDS, MultiTileEntityCrucible.class);
    }

    @Override
    public void registerClient(IWDMlaClientRegistration registration) {
        registration.registerItemStorageClient(STORAGE);
        registration.registerBlockComponent(GT6_INFO, Block.class);
        registration.registerFluidStorageClient(GT6_FLUIDS);
        registration.registerHarvest(GregTech6HarvestHandler.INSTANCE, Block.class);
    }

    private static class StorageProvider
        implements IServerExtensionProvider<ItemStack>, IClientExtensionProvider<ItemStack, ItemView> {

        private static final ResourceLocation UID = new ResourceLocation("gtquality", "item_storage");

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public List<ViewGroup<ItemStack>> getGroups(Accessor accessor) {
            Object target = accessor.getTarget();
            if (target instanceof MultiTileEntityMassStorage) {
                IInventory inventory = (IInventory) target;
                return Collections.singletonList(collect(inventory, 1, 2));
            }
            if (target instanceof MultiTileEntityDrawerQuad) {
                IInventory inventory = (IInventory) target;
                List<ViewGroup<ItemStack>> groups = new ArrayList<>(4);
                for (int drawer = 0; drawer < 4; drawer++) {
                    groups.add(collect(inventory, drawer * 36, (drawer + 1) * 36));
                }
                return groups;
            }
            if (target instanceof MultiTileEntityChest) {
                IInventory inventory = (IInventory) target;
                return Collections.singletonList(collect(inventory, 0, inventory.getSizeInventory()));
            }
            return null;
        }

        private ViewGroup<ItemStack> collect(IInventory inventory, int from, int to) {
            List<ItemStack> items = new ArrayList<>();
            for (int slot = from; slot < to; slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack == null || stack.stackSize <= 0) continue;
                ItemStack normalized = stack.copy();
                normalized.stackSize = 1;
                ItemStack existing = null;
                for (ItemStack item : items) {
                    ItemStack comparable = item.copy();
                    comparable.stackSize = 1;
                    if (ItemStack.areItemStacksEqual(comparable, normalized)) {
                        existing = item;
                        break;
                    }
                }
                if (existing != null) existing.stackSize += stack.stackSize;
                else items.add(stack.copy());
            }
            return new ViewGroup<>(items);
        }

        @Override
        public List<ClientViewGroup<ItemView>> getClientGroups(Accessor accessor, List<ViewGroup<ItemStack>> groups) {
            if (accessor.getTarget() instanceof MultiTileEntityDrawerQuad) {
                MultiTileEntityDrawerQuad drawer = (MultiTileEntityDrawerQuad) accessor.getTarget();
                MovingObjectPosition hit = accessor.getHitResult();
                if (hit == null || hit.hitVec == null || hit.sideHit != drawer.mFacing) {
                    return Collections.emptyList();
                }
                float[] coords = UT.Code.getFacingCoordsClicked(
                    drawer.mFacing, (float) (hit.hitVec.xCoord - hit.blockX),
                    (float) (hit.hitVec.yCoord - hit.blockY), (float) (hit.hitVec.zCoord - hit.blockZ));
                int index = (coords[0] > 0.5F ? 1 : 0) | (coords[1] > 0.5F ? 2 : 0);
                return ClientViewGroup.map(Collections.singletonList(groups.get(index)), ItemView::new, null);
            }
            return ClientViewGroup.map(groups, ItemView::new, null);
        }
    }
}
