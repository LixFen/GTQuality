package com.plainston.gtquality.mixin.inventories;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.plainston.gtquality.integration.ae2.PowderCellChanges;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEStack;

@Pseudo
@Mixin(targets = { "appeng.me.cache.NetworkMonitor", "appeng.api.storage.MEMonitorHandler" }, remap = false)
public abstract class PowderCellMonitorMixin {

    @Invoker("postChangesToListeners")
    public abstract void gtquality$postPowderChanges(Iterable changes, BaseActionSource source);

    @Inject(method = "injectItems", at = @At("HEAD"), remap = false)
    private void gtquality$beginPowderInput(IAEStack input, Actionable mode, BaseActionSource source,
        CallbackInfoReturnable<IAEStack> cir) {
        PowderCellChanges.begin(this);
    }

    @Inject(method = "injectItems", at = @At("RETURN"), remap = false)
    private void gtquality$endPowderInput(IAEStack input, Actionable mode, BaseActionSource source,
        CallbackInfoReturnable<IAEStack> cir) {
        PowderCellChanges.end(this);
    }

    @Inject(method = "monitorDifference", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtquality$reportConvertedPowders(IAEStack original, IAEStack leftovers, boolean extraction,
        BaseActionSource source, CallbackInfoReturnable<IAEStack> cir) {
        if (extraction) return;
        List<IAEStack> changes = PowderCellChanges.corrected(this, original, leftovers);
        if (changes == null) return;
        gtquality$postPowderChanges(changes, source);
        cir.setReturnValue(leftovers);
    }
}
