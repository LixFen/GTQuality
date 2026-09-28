package com.plainston.gtquality.mixin.multiblocks;

import static gregapi.data.CS.SIDE_Y_POS;

import net.minecraft.block.Block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.plainston.gtquality.render.CrucibleTopTexture;

import gregapi.render.ITexture;
import gregtech.tileentity.multiblocks.MultiTileEntityCrucible;

@Mixin(value = MultiTileEntityCrucible.class, remap = false)
public abstract class CrucibleTopTextureMixin {

    @Inject(method = "getTexture2", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtquality$cropWallTop(Block block, int renderPass, byte side, boolean[] shouldRender,
        CallbackInfoReturnable<ITexture> cir) {
        if (side != SIDE_Y_POS || renderPass < 0 || renderPass > 3) return;
        MultiTileEntityCrucible crucible = (MultiTileEntityCrucible) (Object) this;
        if (!crucible.mStructureOkay) return;
        cir.setReturnValue(CrucibleTopTexture.forWall(crucible, renderPass, side));
    }
}
