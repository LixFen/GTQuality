package com.plainston.gtquality.mixin.pipes;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.plainston.gtquality.GTQuality;
import com.plainston.gtquality.render.PipeFlowTexture;

import gregapi.data.CS;
import gregapi.render.ITexture;
import gregapi.tileentity.connectors.MultiTileEntityPipeFluid;
import gregapi.tileentity.connectors.TileEntityBase10ConnectorRendered;
import gregapi.util.UT;
import gregapi.util.WD;

@Mixin(value = MultiTileEntityPipeFluid.class, remap = false)
public abstract class OneWayFluidPipeMixin extends TileEntityBase10ConnectorRendered {

    @Unique
    private byte gtquality$disabledInputs;

    @Unique
    private static final String GTQUALITY_INPUT_MASK = "gtquality.disabledFluidInputs";

    @Inject(method = "readFromNBT2", at = @At("TAIL"), remap = false)
    private void gtquality$readInputs(NBTTagCompound tag, CallbackInfo ci) {
        gtquality$disabledInputs = (byte) (tag.getByte(GTQUALITY_INPUT_MASK) & 63);
    }

    @Inject(method = "writeToNBT2", at = @At("TAIL"), remap = false)
    private void gtquality$writeInputs(NBTTagCompound tag, CallbackInfo ci) {
        tag.setByte(GTQUALITY_INPUT_MASK, gtquality$disabledInputs);
    }

    @Inject(method = "onToolClick2", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtquality$toggleInput(String tool, long remainingDurability, long quality, Entity player,
        List<String> chat, IInventory playerInventory, boolean sneaking, ItemStack heldStack, byte side, float hitX,
        float hitY, float hitZ, CallbackInfoReturnable<Long> cir) {
        if (!GTQuality.oneWayFluidPipes || !sneaking || !CS.TOOL_wrench.equals(tool)) return;
        if (isClientSide()) {
            cir.setReturnValue(0L);
            return;
        }
        byte target = UT.Code.getSideWrenching(side, hitX, hitY, hitZ);
        gtquality$disabledInputs ^= CS.SBIT[target];
        WD.mark(this);
        updateClientData();
        if (chat != null) chat.add(
            StatCollector.translateToLocal(
                (gtquality$disabledInputs & CS.SBIT[target]) != 0 ? "gtquality.pipe.output_only"
                    : "gtquality.pipe.bidirectional"));
        cir.setReturnValue(10000L);
    }

    @Inject(method = "canAcceptFluidsFrom", at = @At("RETURN"), cancellable = true, remap = false)
    private void gtquality$blockInput(byte side, CallbackInfoReturnable<Boolean> cir) {
        if (GTQuality.oneWayFluidPipes && side >= 0 && side < 6 && (gtquality$disabledInputs & CS.SBIT[side]) != 0)
            cir.setReturnValue(false);
    }

    // Fluid pipes do not otherwise use GT6's visual byte. Both full chunk sync and incremental sync carry it.
    @Override
    public byte getVisualData() {
        return GTQuality.oneWayFluidPipes ? gtquality$disabledInputs : 0;
    }

    @Override
    public void setVisualData(byte data) {
        gtquality$disabledInputs = (byte) (data & 63);
    }

    @Inject(method = { "getTextureSide", "getTextureConnected" }, at = @At("RETURN"), cancellable = true, remap = false)
    private void gtquality$showArrows(byte side, byte connections, float diameter, int renderPass,
        CallbackInfoReturnable<ITexture> cir) {
        // Use the server-synchronised state even if the client has a different local configuration.
        int mask = gtquality$disabledInputs & connections;
        if (mask != 0) cir.setReturnValue(PipeFlowTexture.withArrows(cir.getReturnValue(), side, mask));
    }

    @Inject(method = "addToolTips", at = @At("TAIL"), remap = false)
    private void gtquality$explainFlow(List<String> tips, ItemStack stack, boolean advanced, CallbackInfo ci) {
        if (GTQuality.oneWayFluidPipes)
            tips.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal("gtquality.pipe.tip"));
    }
}
