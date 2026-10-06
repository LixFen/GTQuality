package com.plainston.gtquality.misc;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.plainston.gtquality.GTQuality;

import gregapi.data.CS.ArmorsGT;

public final class UniversalHazmat {

    private static final int[] ARMOR_POINTS = { 3, 8, 6, 3 };

    private UniversalHazmat() {}

    public static void install() {
        for (Item armor : ArmorsGT.HAZMAT_UNIVERSAL) {
            // Damage is stored as an absolute value in old stacks; leave it untouched.
            armor.setMaxDamage(armor.getMaxDamage() * 4);
        }
    }

    public static int armorPoints(EntityLivingBase wearer, ItemStack stack) {
        if (!GTQuality.universalHazmatEnhancement || wearer == null || stack == null) return 0;
        for (int type = 0; type < 4; type++) {
            ItemStack equipped = wearer.getEquipmentInSlot(4 - type);
            if (equipped == null || equipped.getItem() != ArmorsGT.HAZMAT_UNIVERSAL[type]) return 0;
        }
        for (int type = 0; type < 4; type++) {
            if (stack.getItem() == ArmorsGT.HAZMAT_UNIVERSAL[type]) return ARMOR_POINTS[type];
        }
        return 0;
    }
}
