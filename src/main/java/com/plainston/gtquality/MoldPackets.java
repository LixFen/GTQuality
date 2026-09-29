package com.plainston.gtquality;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public final class MoldPackets {

    private static final Queue<Runnable> SERVER_TASKS = new ConcurrentLinkedQueue<>();

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Runnable task;
        while ((task = SERVER_TASKS.poll()) != null) task.run();
    }

    public static final class Open implements IMessage {

        int x, y, z, shape, lastShape;

        public Open() {}

        Open(int x, int y, int z, int shape, int lastShape) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.shape = shape;
            this.lastShape = lastShape;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            x = buf.readInt();
            y = buf.readInt();
            z = buf.readInt();
            shape = buf.readInt();
            lastShape = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(x);
            buf.writeInt(y);
            buf.writeInt(z);
            buf.writeInt(shape);
            buf.writeInt(lastShape);
        }
    }

    public static final class OpenHandler implements IMessageHandler<Open, IMessage> {

        @Override
        public IMessage onMessage(final Open message, MessageContext context) {
            Minecraft.getMinecraft()
                .func_152344_a(new Runnable() {

                    @Override
                    public void run() {
                        MoldScreen.open(message.x, message.y, message.z, message.shape, message.lastShape);
                    }
                });
            return null;
        }
    }

    public static final class Select implements IMessage {

        int x, y, z, shape;

        public Select() {}

        Select(int x, int y, int z, int shape) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.shape = shape;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            x = buf.readInt();
            y = buf.readInt();
            z = buf.readInt();
            shape = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(x);
            buf.writeInt(y);
            buf.writeInt(z);
            buf.writeInt(shape);
        }
    }

    public static final class SelectHandler implements IMessageHandler<Select, IMessage> {

        @Override
        public IMessage onMessage(final Select message, final MessageContext context) {
            final EntityPlayerMP player = context.getServerHandler().playerEntity;
            SERVER_TASKS.add(new Runnable() {

                @Override
                public void run() {
                    MoldInteraction.select(player, message.x, message.y, message.z, message.shape);
                }
            });
            return null;
        }
    }
}
