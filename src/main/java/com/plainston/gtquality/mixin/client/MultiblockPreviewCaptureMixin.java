package com.plainston.gtquality.mixin.client;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ChunkCoordinates;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.plainston.gtquality.integration.blockrenderer.PreviewCapture;

import gregapi.tileentity.multiblocks.ITileEntityMultiBlockController;

@Mixin(value = ITileEntityMultiBlockController.Util.class, remap = false)
public abstract class MultiblockPreviewCaptureMixin {

    @Inject(
        method = "checkAndSetTarget(Lgregapi/tileentity/multiblocks/ITileEntityMultiBlockController;IIIIIIILnet/minecraft/util/ChunkCoordinates;Lnet/minecraft/entity/Entity;Lnet/minecraft/inventory/IInventory;)Z",
        at = @At("HEAD"),
        cancellable = true,
        remap = false)
    private static void gtquality$capturePreview(ITileEntityMultiBlockController controller, int x, int y, int z,
        int id, int registry, int design, int mode, ChunkCoordinates clickedAt, Entity player, IInventory inventory,
        CallbackInfoReturnable<Boolean> cir) {
        Boolean result = PreviewCapture.visit(controller, x, y, z, id, registry, design, mode);
        if (result != null) cir.setReturnValue(result);
    }
}
