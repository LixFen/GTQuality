package com.plainston.gtquality;

import gregapi.block.multitileentity.MultiTileEntityClassContainer;
import gregapi.block.multitileentity.MultiTileEntityRegistry;
import gregtech.tileentity.tools.MultiTileEntitySapBag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public final class SapBagHopperExport {

    private SapBagHopperExport() {}

    static void install() {
        MultiTileEntityRegistry registry = MultiTileEntityRegistry.getRegistry("gt.multitileentity");
        if (registry == null) throw new IllegalStateException("GT6 multi-tile registry is unavailable");
        int replaced = 0;
        for (int i = 0; i < registry.mRegistrations.size(); i++) {
            MultiTileEntityClassContainer old = registry.mRegistrations.get(i);
            if (old.mClass != MultiTileEntitySapBag.class) continue;

            MultiTileEntityClassContainer fixed = new MultiTileEntityClassContainer(old.mID, old.mCreativeTabID,
                SapBag.class, old.mBlockMetaData, old.mStackSize, old.mBlock, old.mParameters);
            registry.mRegistry.put(old.mID, fixed);
            registry.mRegistrations.set(i, fixed);
            replaced++;
        }
        if (replaced == 0) throw new IllegalStateException("GT6 Sap Bag was not registered before GTQuality initialized");
    }

    public static class SapBag extends MultiTileEntitySapBag {
        @Override
        public int[] getAccessibleSlotsFromSide2(byte side) {
            return new int[] {0};
        }

        @Override
        public boolean canExtractItem2(int slot, ItemStack stack, byte side) {
            return slot == 0;
        }
    }
}
