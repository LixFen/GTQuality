package com.plainston.gtquality.circuit;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregapi.item.ItemIntegratedCircuit;

public final class CircuitInteraction {

    public static boolean isCircuit(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemIntegratedCircuit;
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK || !event.entityPlayer.isSneaking()
            || !isCircuit(event.entityPlayer.getHeldItem())) return;
        event.setCanceled(true);
        if (event.world.isRemote) CircuitScreen.open(event.entityPlayer);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (isCircuit(event.itemStack)) {
            event.toolTip.add(StatCollector.translateToLocal("gtquality.circuit.tooltip"));
        }
    }
}
