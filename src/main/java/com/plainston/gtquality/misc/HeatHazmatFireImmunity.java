package com.plainston.gtquality.misc;

import net.minecraftforge.event.entity.living.LivingAttackEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregapi.util.UT;

public class HeatHazmatFireImmunity {

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.source.isFireDamage() && UT.Entities.isWearingFullHeatHazmat(event.entityLiving)) {
            event.setCanceled(true);
        }
    }
}
