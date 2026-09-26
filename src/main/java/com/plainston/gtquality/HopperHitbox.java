package com.plainston.gtquality;

import gregapi.block.multitileentity.MultiTileEntityClassContainer;
import gregapi.block.multitileentity.MultiTileEntityRegistry;
import gregapi.block.multitileentity.IMultiTileEntity.IMTE_CollisionRayTrace;
import gregapi.tileentity.base.TileEntityBase09FacingSingle;
import gregtech.tileentity.inventories.MultiTileEntityHopper;
import gregtech.tileentity.inventories.MultiTileEntityQueueHopper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public final class HopperHitbox {

    private HopperHitbox() {}

    static void install() {
        MultiTileEntityRegistry registry = MultiTileEntityRegistry.getRegistry("gt.multitileentity");
        if (registry == null) throw new IllegalStateException("GT6 multi-tile registry is unavailable");
        int replaced = 0;
        for (int i = 0; i < registry.mRegistrations.size(); i++) {
            MultiTileEntityClassContainer old = registry.mRegistrations.get(i);
            Class<? extends TileEntity> replacement;
            if (old.mClass == MultiTileEntityHopper.class) {
                replacement = Hopper.class;
            } else if (old.mClass == MultiTileEntityQueueHopper.class) {
                replacement = QueueHopper.class;
            } else {
                continue;
            }
            MultiTileEntityClassContainer fixed = new MultiTileEntityClassContainer(old.mID, old.mCreativeTabID,
                replacement, old.mBlockMetaData, old.mStackSize, old.mBlock, old.mParameters);
            registry.mRegistry.put(old.mID, fixed);
            registry.mRegistrations.set(i, fixed);
            replaced++;
        }
        if (replaced == 0) throw new IllegalStateException("GT6 hoppers were not registered before GTQuality initialized");
    }

    private static Hit trace(TileEntityBase09FacingSingle hopper, Vec3 start, Vec3 end) {
        Hit hit = new Hit();
        hit.add(hopper, start, end, 0, 10, 0, 16, 16, 16);
        hit.add(hopper, start, end, 4, 4, 4, 12, 10, 12);
        switch (hopper.mFacing) {
            case 0: hit.add(hopper, start, end, 6, 0, 6, 10, 4, 10); break;
            case 2: hit.add(hopper, start, end, 6, 4, 0, 10, 8, 4); break;
            case 3: hit.add(hopper, start, end, 6, 4, 12, 10, 8, 16); break;
            case 4: hit.add(hopper, start, end, 0, 4, 6, 4, 8, 10); break;
            case 5: hit.add(hopper, start, end, 12, 4, 6, 16, 8, 10); break;
            default: break;
        }
        if (hopper.hasCovers()) {
            for (byte side = 0; side < 6; side++) {
                if (hopper.mCovers.mBehaviours[side] == null) continue;
                hit.add(hopper, start, end, hopper.mCovers.mBehaviours[side].getHolderBounds(side, hopper.mCovers));
                hit.add(hopper, start, end, hopper.mCovers.mBehaviours[side].getCoverBounds(side, hopper.mCovers));
            }
        }
        return hit;
    }

    private static final class Hit {
        private MovingObjectPosition target;
        private AxisAlignedBB box;
        private double distance = Double.POSITIVE_INFINITY;

        void add(TileEntityBase09FacingSingle hopper, Vec3 start, Vec3 end, float[] bounds) {
            if (bounds != null) add(hopper, start, end, bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
        }

        void add(TileEntityBase09FacingSingle hopper, Vec3 start, Vec3 end, int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ) {
            add(hopper, start, end, minX / 16F, minY / 16F, minZ / 16F, maxX / 16F, maxY / 16F, maxZ / 16F);
        }

        void add(TileEntityBase09FacingSingle hopper, Vec3 start, Vec3 end, float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ) {
            AxisAlignedBB candidate = AxisAlignedBB.getBoundingBox(hopper.xCoord + minX, hopper.yCoord + minY,
                hopper.zCoord + minZ, hopper.xCoord + maxX, hopper.yCoord + maxY, hopper.zCoord + maxZ);
            MovingObjectPosition intersection = candidate.calculateIntercept(start, end);
            if (intersection == null) return;
            double newDistance = start.squareDistanceTo(intersection.hitVec);
            if (newDistance < distance) {
                distance = newDistance;
                box = candidate;
                target = new MovingObjectPosition(hopper.xCoord, hopper.yCoord, hopper.zCoord,
                    intersection.sideHit, intersection.hitVec);
            }
        }
    }

    public static class Hopper extends MultiTileEntityHopper implements IMTE_CollisionRayTrace {
        private AxisAlignedBB selectedBox;

        @Override
        public MovingObjectPosition collisionRayTrace(Vec3 start, Vec3 end) {
            Hit hit = trace(this, start, end);
            selectedBox = hit.box;
            return hit.target;
        }

        @Override
        public AxisAlignedBB getSelectedBoundingBoxFromPool() {
            return FORCE_FULL_SELECTION_BOXES || selectedBox == null ? super.getSelectedBoundingBoxFromPool() : selectedBox;
        }
    }

    public static class QueueHopper extends MultiTileEntityQueueHopper implements IMTE_CollisionRayTrace {
        private AxisAlignedBB selectedBox;

        @Override
        public MovingObjectPosition collisionRayTrace(Vec3 start, Vec3 end) {
            Hit hit = trace(this, start, end);
            selectedBox = hit.box;
            return hit.target;
        }

        @Override
        public AxisAlignedBB getSelectedBoundingBoxFromPool() {
            return FORCE_FULL_SELECTION_BOXES || selectedBox == null ? super.getSelectedBoundingBoxFromPool() : selectedBox;
        }
    }
}
