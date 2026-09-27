package com.plainston.gtquality;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregtech.tileentity.tools.MultiTileEntityScaffold;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.living.LivingEvent;

public class ScaffoldClimb {

    @SubscribeEvent
    public void onPlayerUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.entity;
        if (player.isSneaking() || !player.isOnLadder()) return;

        TileEntity tile = player.worldObj.getTileEntity(
            MathHelper.floor_double(player.posX), MathHelper.floor_double(player.boundingBox.minY),
            MathHelper.floor_double(player.posZ));
        if (!(tile instanceof MultiTileEntityScaffold)) return;

        if (player.rotationPitch < 0.0F && player.moveForward > 0.0F && GTQuality.scaffoldClimbUpSpeed > 0.0F) {
            player.moveEntity(0.0D, -player.rotationPitch / 90.0D * GTQuality.scaffoldClimbUpSpeed, 0.0D);
        } else if (player.rotationPitch > 0.0F && player.moveForward == 0.0F
            && GTQuality.scaffoldClimbDownSpeed > 0.0F) {
            player.moveEntity(0.0D, -player.rotationPitch / 90.0D * GTQuality.scaffoldClimbDownSpeed, 0.0D);
        }
    }
}
