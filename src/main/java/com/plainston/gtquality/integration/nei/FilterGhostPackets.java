package com.plainston.gtquality.integration.nei;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import gregapi.gui.ContainerCommon;
import gregapi.gui.Slot_Holo;
import gregtech.tileentity.extenders.MultiTileEntityFilter.MultiTileEntityGUICommonFilter;
import gregtech.tileentity.extenders.MultiTileEntityFilterPrefix.MultiTileEntityGUICommonFilterPrefix;
import io.netty.buffer.ByteBuf;

public final class FilterGhostPackets {

    private static final Queue<Runnable> TASKS = new ConcurrentLinkedQueue<>();

    static boolean isFilterSlot(Container container, int slot) {
        return (container instanceof MultiTileEntityGUICommonFilter
            || container instanceof MultiTileEntityGUICommonFilterPrefix) && slot >= 0
            && slot < container.inventorySlots.size()
            && container.inventorySlots.get(slot) instanceof Slot_Holo;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Runnable task;
        while ((task = TASKS.poll()) != null) task.run();
    }

    public static final class Select implements IMessage {

        private int window, slot;
        private ItemStack stack;

        public Select() {}

        public Select(int window, int slot, ItemStack stack) {
            this.window = window;
            this.slot = slot;
            this.stack = stack.copy();
            this.stack.stackSize = 1;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            window = buf.readInt();
            slot = buf.readInt();
            stack = ByteBufUtils.readItemStack(buf);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(window);
            buf.writeInt(slot);
            ByteBufUtils.writeItemStack(buf, stack);
        }
    }

    public static final class Handler implements IMessageHandler<Select, IMessage> {

        @Override
        public IMessage onMessage(final Select message, MessageContext context) {
            final EntityPlayerMP player = context.getServerHandler().playerEntity;
            TASKS.add(() -> {
                Container container = player.openContainer;
                if (container.windowId != message.window || !isFilterSlot(container, message.slot)
                    || !container.canInteractWith(player)
                    || message.stack == null
                    || message.stack.stackSize <= 0) return;
                ItemStack held = player.inventory.getItemStack();
                ItemStack ghost = message.stack.copy();
                ghost.stackSize = 1;
                // Reuse GT6's item, fluid and prefix selection rules without changing the real cursor stack.
                player.inventory.setItemStack(ghost);
                try {
                    container.slotClick(message.slot, 0, 0, player);
                } finally {
                    player.inventory.setItemStack(held);
                }
                ((ContainerCommon) container).mTileEntity.markDirtyGUI();
                container.detectAndSendChanges();
            });
            return null;
        }
    }
}
