package com.plainston.gtquality.mixin.tools;

import static gregapi.data.CS.SIDE_X_NEG;
import static gregapi.data.CS.SIDE_X_POS;
import static gregapi.data.CS.SIDE_Z_NEG;
import static gregapi.data.CS.SIDE_Z_POS;

import java.util.List;

import net.minecraft.util.AxisAlignedBB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import gregtech.tileentity.tools.MultiTileEntityScaffold;

@Mixin(value = MultiTileEntityScaffold.class, remap = false)
public abstract class ScaffoldCollisionMixin {

    @Redirect(
        method = "addCollisionBoxesToList2",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/tileentity/tools/MultiTileEntityScaffold;box(Lnet/minecraft/util/AxisAlignedBB;Ljava/util/List;DDDDDD)Z",
            remap = false),
        remap = false)
    private boolean gtquality$addCollisionBox(MultiTileEntityScaffold scaffold, AxisAlignedBB query,
        List<AxisAlignedBB> collisions, double minX, double minY, double minZ, double maxX, double maxY,
        double maxZ) {
        byte design = scaffold.getVisualData();
        if (design == 1 || design == 2) {
            boolean cornerX = (minX == 0.0D && maxX == 0.0625D) || (minX == 0.9375D && maxX == 1.0D);
            boolean cornerZ = (minZ == 0.0D && maxZ == 0.0625D) || (minZ == 0.9375D && maxZ == 1.0D);
            boolean pillar = minY == 0.0D && maxY == 1.0D && maxX - minX == 0.0625D
                && maxZ - minZ == 0.0625D && cornerX && cornerZ;
            if (pillar) {
                boolean farSidePost;
                switch (scaffold.getFacing()) {
                    case SIDE_Z_NEG:
                        farSidePost = minZ == 0.0D;
                        break;
                    case SIDE_Z_POS:
                        farSidePost = minZ == 0.9375D;
                        break;
                    case SIDE_X_POS:
                        farSidePost = minX == 0.9375D;
                        break;
                    case SIDE_X_NEG:
                        farSidePost = minX == 0.0D;
                        break;
                    default:
                        farSidePost = false;
                }
                if (farSidePost) return false;
            }
        }
        return scaffold.box(query, collisions, minX, minY, minZ, maxX, maxY, maxZ);
    }
}
