package com.plainston.gtquality.mixin.tools;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.plainston.gtquality.GTQuality;

import gregtech.tileentity.tools.MultiTileEntitySapBag;

@Mixin(value = MultiTileEntitySapBag.class, remap = false)
public abstract class SapBagHopperExportMixin {

    @Inject(method = "getAccessibleSlotsFromSide2", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtquality$accessibleSlot(byte side, CallbackInfoReturnable<int[]> cir) {
        if (GTQuality.allowSapBagHopperExtraction) cir.setReturnValue(new int[] { 0 });
    }

    @Inject(method = "canExtractItem2", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtquality$allowExtraction(int slot, ItemStack stack, byte side, CallbackInfoReturnable<Boolean> cir) {
        if (GTQuality.allowSapBagHopperExtraction) cir.setReturnValue(slot == 0);
    }
}
