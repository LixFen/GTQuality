package com.plainston.gtquality.collision;

import static gregapi.data.CS.SIDE_X_NEG;
import static gregapi.data.CS.SIDE_X_POS;
import static gregapi.data.CS.SIDE_Y_NEG;
import static gregapi.data.CS.SIDE_Y_POS;
import static gregapi.data.CS.SIDE_Z_NEG;
import static gregapi.data.CS.SIDE_Z_POS;

import net.minecraft.tileentity.TileEntity;

import gregapi.block.multitileentity.MultiTileEntityClassContainer;
import gregapi.block.multitileentity.MultiTileEntityRegistry;
import gregapi.cover.ICover;
import gregapi.cover.covers.AbstractCoverAttachmentTorch;
import gregapi.cover.covers.CoverPressureValve;
import gregapi.tileentity.connectors.MultiTileEntityPipeFluid;
import gregapi.tileentity.connectors.MultiTileEntityWireRedstone;
import gregapi.tileentity.connectors.MultiTileEntityWireRedstoneInsulated;
import gregapi.tileentity.connectors.TileEntityBase10ConnectorRendered;

public final class SmallCoverHitbox {

    private SmallCoverHitbox() {}

    public static void install() {
        MultiTileEntityRegistry registry = MultiTileEntityRegistry.getRegistry("gt.multitileentity");
        if (registry == null) throw new IllegalStateException("GT6 multi-tile registry is unavailable");
        int replaced = 0;
        for (int i = 0; i < registry.mRegistrations.size(); i++) {
            MultiTileEntityClassContainer old = registry.mRegistrations.get(i);
            Class<? extends TileEntity> replacement;
            if (old.mClass == MultiTileEntityWireRedstoneInsulated.class) {
                replacement = InsulatedRedstoneWire.class;
            } else if (old.mClass == MultiTileEntityWireRedstone.class) {
                replacement = RedstoneWire.class;
            } else if (old.mClass == MultiTileEntityPipeFluid.class) {
                replacement = FluidPipe.class;
            } else {
                continue;
            }
            MultiTileEntityClassContainer fixed = new MultiTileEntityClassContainer(
                old.mID,
                old.mCreativeTabID,
                replacement,
                old.mBlockMetaData,
                old.mStackSize,
                old.mBlock,
                old.mParameters);
            registry.mRegistry.put(old.mID, fixed);
            registry.mRegistrations.set(i, fixed);
            replaced++;
        }
        if (replaced == 0)
            throw new IllegalStateException("GT6 connectors were not registered before GTQuality initialized");
    }

    private static float[] bounds(TileEntityBase10ConnectorRendered connector) {
        if (!connector.hasCovers() || connector.mFoam || connector.mDiameter >= 1.0F) return null;
        for (ICover cover : connector.mCovers.mBehaviours) {
            if (cover != null && !(cover instanceof AbstractCoverAttachmentTorch)
                && !(cover instanceof CoverPressureValve)) return null;
        }

        // GT6's shrunkBox() returns a full block whenever covers exist, so start from its cover-free connector bounds.
        float half = (1.0F - connector.mDiameter) / 2.0F;
        float[] bounds = { connector.connected(SIDE_X_NEG) ? 0 : half, connector.connected(SIDE_Y_NEG) ? 0 : half,
            connector.connected(SIDE_Z_NEG) ? 0 : half, connector.connected(SIDE_X_POS) ? 1 : 1 - half,
            connector.connected(SIDE_Y_POS) ? 1 : 1 - half, connector.connected(SIDE_Z_POS) ? 1 : 1 - half };
        for (byte side = 0; side < 6; side++) {
            ICover cover = connector.mCovers.mBehaviours[side];
            if (cover == null) continue;
            include(bounds, cover.getHolderBounds(side, connector.mCovers));
            include(bounds, cover.getCoverBounds(side, connector.mCovers));
        }
        return bounds;
    }

    private static void include(float[] bounds, float[] part) {
        for (int axis = 0; axis < 3; axis++) {
            bounds[axis] = Math.min(bounds[axis], part[axis]);
            bounds[axis + 3] = Math.max(bounds[axis + 3], part[axis + 3]);
        }
    }

    public static class InsulatedRedstoneWire extends MultiTileEntityWireRedstoneInsulated {

        @Override
        public float[] shrunkBox() {
            float[] bounds = bounds(this);
            return bounds == null ? super.shrunkBox() : bounds;
        }
    }

    public static class RedstoneWire extends MultiTileEntityWireRedstone {

        @Override
        public float[] shrunkBox() {
            float[] bounds = bounds(this);
            return bounds == null ? super.shrunkBox() : bounds;
        }
    }

    public static class FluidPipe extends MultiTileEntityPipeFluid {

        @Override
        public float[] shrunkBox() {
            float[] bounds = bounds(this);
            return bounds == null ? super.shrunkBox() : bounds;
        }
    }
}
