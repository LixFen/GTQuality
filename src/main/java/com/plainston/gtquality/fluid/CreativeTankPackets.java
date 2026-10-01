package com.plainston.gtquality.fluid;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public final class CreativeTankPackets {

    private static final Queue<Runnable> TASKS = new ConcurrentLinkedQueue<>();

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Runnable task;
        while ((task = TASKS.poll()) != null) task.run();
    }

    public static final class Configure implements IMessage {

        private int window, action, rate;
        private FluidStack fluid;

        public Configure() {}

        public Configure(int window, int action, int rate, FluidStack fluid) {
            this.window = window;
            this.action = action;
            this.rate = rate;
            this.fluid = fluid;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            window = buf.readInt();
            action = buf.readByte();
            rate = buf.readInt();
            NBTTagCompound nbt = ByteBufUtils.readTag(buf);
            fluid = nbt == null ? null : FluidStack.loadFluidStackFromNBT(nbt);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(window);
            buf.writeByte(action);
            buf.writeInt(rate);
            ByteBufUtils.writeTag(buf, fluid == null ? null : fluid.writeToNBT(new NBTTagCompound()));
        }
    }

    public static final class Handler implements IMessageHandler<Configure, IMessage> {

        @Override
        public IMessage onMessage(final Configure message, MessageContext context) {
            final EntityPlayerMP player = context.getServerHandler().playerEntity;
            TASKS.add(() -> {
                if (!(player.openContainer instanceof CreativeTankContainer)
                    || player.openContainer.windowId != message.window
                    || !player.openContainer.canInteractWith(player)) return;
                CreativeTank tank = (CreativeTank) ((CreativeTankContainer) player.openContainer).mTileEntity;
                if (message.action == 0 && message.fluid != null) tank.selectFluid(message.fluid);
                if (message.action == 1 && message.rate >= 0) tank.outputRate = message.rate;
                if (message.action == 2) tank.autoOutput = !tank.autoOutput;
                if (message.action == 3) tank.selectFluid(null);
                tank.markDirty();
                player.openContainer.detectAndSendChanges();
            });
            return null;
        }
    }

    public static final class FluidState implements IMessage {

        private int window;
        private FluidStack fluid;

        public FluidState() {}

        FluidState(int window, FluidStack fluid) {
            this.window = window;
            this.fluid = fluid;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            window = buf.readInt();
            NBTTagCompound nbt = ByteBufUtils.readTag(buf);
            fluid = nbt == null ? null : FluidStack.loadFluidStackFromNBT(nbt);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(window);
            ByteBufUtils.writeTag(buf, fluid == null ? null : fluid.writeToNBT(new NBTTagCompound()));
        }
    }

    public static final class FluidStateHandler implements IMessageHandler<FluidState, IMessage> {

        @Override
        public IMessage onMessage(final FluidState message, MessageContext context) {
            Minecraft.getMinecraft()
                .func_152344_a(() -> {
                    if (Minecraft.getMinecraft().thePlayer == null) return;
                    if (Minecraft.getMinecraft().thePlayer.openContainer instanceof CreativeTankContainer
                        && Minecraft.getMinecraft().thePlayer.openContainer.windowId == message.window) {
                        ((CreativeTankContainer) Minecraft
                            .getMinecraft().thePlayer.openContainer).fluid = message.fluid;
                    }
                });
            return null;
        }
    }
}
