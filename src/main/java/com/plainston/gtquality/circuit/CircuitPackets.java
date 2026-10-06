package com.plainston.gtquality.circuit;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import com.plainston.gtquality.GTQuality;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public final class CircuitPackets {

    private static final Queue<Runnable> SERVER_TASKS = new ConcurrentLinkedQueue<>();

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Runnable task;
        while ((task = SERVER_TASKS.poll()) != null) task.run();
    }

    public static final class Select implements IMessage {

        private int slot, original, number;

        public Select() {}

        Select(int slot, int original, int number) {
            this.slot = slot;
            this.original = original;
            this.number = number;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            slot = buf.readInt();
            original = buf.readInt();
            number = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(slot);
            buf.writeInt(original);
            buf.writeInt(number);
        }
    }

    public static final class Handler implements IMessageHandler<Select, IMessage> {

        @Override
        public IMessage onMessage(final Select message, MessageContext context) {
            if (!GTQuality.circuitSelectorGui) return null;
            final EntityPlayerMP player = context.getServerHandler().playerEntity;
            SERVER_TASKS.add(() -> {
                if (player.isDead || message.slot < 0
                    || message.slot > 8
                    || player.inventory.currentItem != message.slot
                    || message.number < 0
                    || message.number > 24) return;
                ItemStack stack = player.getHeldItem();
                if (!CircuitInteraction.isCircuit(stack) || stack.getItemDamage() != message.original) return;
                // Keep the comparison mode, stack count and NBT; only change the selector number.
                stack.setItemDamage((message.original & ~255) | message.number);
                player.inventory.markDirty();
                player.inventoryContainer.detectAndSendChanges();
            });
            return null;
        }
    }
}
