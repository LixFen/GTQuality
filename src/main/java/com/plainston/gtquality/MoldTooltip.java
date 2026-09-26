package com.plainston.gtquality;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregapi.block.multitileentity.MultiTileEntityContainer;
import gregapi.block.multitileentity.MultiTileEntityItemInternal;
import gregapi.data.LH;
import gregtech.tileentity.tools.MultiTileEntityMold;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public final class MoldTooltip {

    private static final String CHISEL_SELECT = "gtquality.tooltip.mold.chisel_select";

    static void register() {
        LH.add(CHISEL_SELECT, "手持凿子，蹲下并右键模具以切换雕刻类型");
    }

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (event.itemStack == null || !(event.itemStack.getItem() instanceof MultiTileEntityItemInternal)) return;
        MultiTileEntityItemInternal item = (MultiTileEntityItemInternal) event.itemStack.getItem();
        MultiTileEntityContainer container = item.mBlock.mMultiTileEntityRegistry
            .getNewTileEntityContainer(event.itemStack);
        if (container != null && container.mTileEntity.getClass() == MultiTileEntityMold.class) {
            event.toolTip.add(LH.Chat.DGRAY + LH.get(CHISEL_SELECT));
        }
    }
}
