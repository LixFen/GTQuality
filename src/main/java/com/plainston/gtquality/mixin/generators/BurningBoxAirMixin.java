package com.plainston.gtquality.mixin.generators;

import com.plainston.gtquality.CombustionAir;
import gregtech.tileentity.energy.generators.MultiTileEntityGeneratorFluidBed;
import gregtech.tileentity.energy.generators.MultiTileEntityGeneratorLiquid;
import gregtech.tileentity.energy.generators.MultiTileEntityGeneratorSolid;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
    value = {
        MultiTileEntityGeneratorSolid.class,
        MultiTileEntityGeneratorLiquid.class,
        MultiTileEntityGeneratorFluidBed.class
    },
    remap = false)
public abstract class BurningBoxAirMixin {

    @Redirect(
        method = {"onTick2", "getStateRunningPossible"},
        at = @At(
            value = "INVOKE",
            target = "Lgregapi/util/WD;hasCollide(Lnet/minecraft/world/World;III)Z",
            remap = false),
        remap = false)
    private boolean allowClosedTrapDoorCollision(World world, int x, int y, int z) {
        return CombustionAir.hasCollision(world, x, y, z);
    }

    @Redirect(
        method = {"onTick2", "getStateRunningPossible"},
        at = @At(
            value = "INVOKE",
            target = "Lgregapi/util/WD;oxygen(Lnet/minecraft/world/World;III)Z",
            remap = false),
        remap = false)
    private boolean allowClosedTrapDoorOxygen(World world, int x, int y, int z) {
        return CombustionAir.hasOxygen(world, x, y, z);
    }
}
