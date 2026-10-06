package com.plainston.gtquality.mixin.tools;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ISpecialArmor.ArmorProperties;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.plainston.gtquality.misc.UniversalHazmat;

import gregapi.item.ItemArmorBase;

@Mixin(value = ItemArmorBase.class, remap = false)
public abstract class UniversalHazmatMixin {

    @Inject(method = "getArmorDisplay", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtquality$armorDisplay(EntityPlayer player, ItemStack stack, int slot,
        CallbackInfoReturnable<Integer> cir) {
        int points = UniversalHazmat.armorPoints(player, stack);
        if (points > 0) cir.setReturnValue(points);
    }

    @Inject(method = "getProperties", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtquality$armorProtection(EntityLivingBase wearer, ItemStack stack, DamageSource source, double damage,
        int slot, CallbackInfoReturnable<ArmorProperties> cir) {
        int points = UniversalHazmat.armorPoints(wearer, stack);
        if (points > 0 && !source.isUnblockable()) {
            cir.setReturnValue(new ArmorProperties(0, points / 25.0, stack.getMaxDamage() + 1 - stack.getItemDamage()));
        }
    }
}
