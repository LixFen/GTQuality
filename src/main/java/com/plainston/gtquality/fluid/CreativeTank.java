package com.plainston.gtquality.fluid;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.plainston.gtquality.GTQuality;

import gregapi.block.multitileentity.IMultiTileEntity.IMTE_AddToolTips;
import gregapi.block.multitileentity.MultiTileEntityBlock;
import gregapi.block.multitileentity.MultiTileEntityRegistry;
import gregapi.data.CS;
import gregapi.render.BlockTextureDefault;
import gregapi.render.BlockTextureMulti;
import gregapi.render.ITexture;
import gregapi.tileentity.base.TileEntityBase07Paintable;
import gregapi.util.UT;
import gregtech.tileentity.tanks.MultiTileEntityBarrelMetal;

public final class CreativeTank extends TileEntityBase07Paintable implements IFluidHandler, IMTE_AddToolTips {

    private FluidStack selectedFluid;
    public boolean autoOutput;
    public int outputRate = 1000;
    private long outputTick = Long.MIN_VALUE;
    private int emitted;

    public static void register() {
        MultiTileEntityRegistry registry = new MultiTileEntityRegistry("gtquality.multitileentity");
        MultiTileEntityBlock block = MultiTileEntityBlock.getOrCreate(
            GTQuality.MODID,
            "iron",
            Material.iron,
            Block.soundTypeMetal,
            CS.TOOL_wrench,
            0,
            0,
            15,
            false,
            false);
        registry.add(
            "Creative Fluid Tank",
            "GTQuality",
            1,
            1,
            CreativeTank.class,
            0,
            64,
            block,
            UT.NBT.make(CS.NBT_COLOR, 0xAA66FF, CS.NBT_HARDNESS, 5.0F, CS.NBT_RESISTANCE, 10.0F));
    }

    @Override
    public void readFromNBT2(NBTTagCompound nbt) {
        super.readFromNBT2(nbt);
        selectedFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("gtquality.fluid"));
        autoOutput = nbt.getBoolean("gtquality.autoOutput");
        outputRate = nbt.hasKey("gtquality.outputRate") ? Math.max(0, nbt.getInteger("gtquality.outputRate")) : 1000;
    }

    private void writeSettings(NBTTagCompound nbt) {
        if (selectedFluid != null) nbt.setTag("gtquality.fluid", selectedFluid.writeToNBT(new NBTTagCompound()));
        else nbt.removeTag("gtquality.fluid");
        nbt.setBoolean("gtquality.autoOutput", autoOutput);
        nbt.setInteger("gtquality.outputRate", outputRate);
    }

    @Override
    public void writeToNBT2(NBTTagCompound nbt) {
        super.writeToNBT2(nbt);
        writeSettings(nbt);
    }

    @Override
    public NBTTagCompound writeItemNBT2(NBTTagCompound nbt) {
        writeSettings(nbt);
        return super.writeItemNBT2(nbt);
    }

    public void selectFluid(FluidStack fluid) {
        selectedFluid = fluid == null ? null : fluid.copy();
        if (selectedFluid != null) selectedFluid.amount = 1;
        markDirty();
    }

    public FluidStack getSelectedFluid() {
        return selectedFluid == null ? null : selectedFluid.copy();
    }

    private int available() {
        long tick = worldObj.getTotalWorldTime();
        if (outputTick != tick) {
            outputTick = tick;
            emitted = 0;
        }
        return Math.max(0, outputRate - emitted);
    }

    @Override
    public void onTick2(long timer, boolean server) {
        super.onTick2(timer, server);
        if (!server || !autoOutput || selectedFluid == null) return;
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            int amount = available();
            if (amount == 0) break;
            TileEntity target = worldObj
                .getTileEntity(xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ);
            if (!(target instanceof IFluidHandler)) continue;
            FluidStack fluid = selectedFluid.copy();
            fluid.amount = amount;
            // Reserve the budget before calling another tile, which may call back into this tank.
            emitted += amount;
            int accepted = ((IFluidHandler) target).fill(side.getOpposite(), fluid, true);
            emitted -= amount - Math.max(0, Math.min(amount, accepted));
        }
    }

    @Override
    public FluidStack drain(ForgeDirection side, int amount, boolean doDrain) {
        if (selectedFluid == null || amount <= 0 || worldObj == null || worldObj.isRemote) return null;
        amount = Math.min(amount, available());
        if (amount == 0) return null;
        FluidStack fluid = selectedFluid.copy();
        fluid.amount = amount;
        if (doDrain) emitted += amount;
        return fluid;
    }

    @Override
    public FluidStack drain(ForgeDirection side, FluidStack fluid, boolean doDrain) {
        return fluid != null && selectedFluid != null && fluid.isFluidEqual(selectedFluid)
            ? drain(side, fluid.amount, doDrain)
            : null;
    }

    @Override
    public int fill(ForgeDirection side, FluidStack fluid, boolean doFill) {
        return 0;
    }

    @Override
    public boolean canFill(ForgeDirection side, Fluid fluid) {
        return false;
    }

    @Override
    public boolean canDrain(ForgeDirection side, Fluid fluid) {
        return selectedFluid != null && outputRate > 0 && (fluid == null || fluid == selectedFluid.getFluid());
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection side) {
        FluidStack fluid = selectedFluid == null ? null : selectedFluid.copy();
        if (fluid != null) fluid.amount = Integer.MAX_VALUE;
        return new FluidTankInfo[] { new FluidTankInfo(fluid, Integer.MAX_VALUE) };
    }

    @Override
    public boolean onBlockActivated3(EntityPlayer player, byte side, float x, float y, float z) {
        if (isServerSide()) openGUI(player);
        return true;
    }

    @Override
    public Object getGUIServer2(int id, EntityPlayer player) {
        return new CreativeTankContainer(player.inventory, this);
    }

    @Override
    public Object getGUIClient2(int id, EntityPlayer player) {
        return new CreativeTankScreen(player.inventory, this);
    }

    @Override
    public boolean canDrop(int slot) {
        return false;
    }

    @Override
    public ITexture getTexture2(Block block, int pass, byte side, boolean[] visible) {
        return visible[side] ? BlockTextureMulti.get(
            BlockTextureDefault.get(MultiTileEntityBarrelMetal.sColoreds[CS.FACES_TBS[side]], mRGBa),
            BlockTextureDefault.get(MultiTileEntityBarrelMetal.sOverlays[CS.FACES_TBS[side]])) : null;
    }

    @Override
    public void addToolTips(List<String> lines, ItemStack stack, boolean advanced) {
        lines.add(StatCollector.translateToLocal("gtquality.creative_tank.tip"));
    }

    @Override
    public String getTileEntityName() {
        return "gtquality.creative_tank";
    }
}
