package com.plainston.gtquality.integration.ae2;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.sync.GuiBridge;
import appeng.items.contents.CellUpgrades;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class PowderCell extends Item implements ICellHandler, ICellWorkbenchItem {

    private PowderCell() {
        setUnlocalizedName("gtquality.powder_cell");
        setTextureName("gtquality:powder_cell");
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.tabMisc);
    }

    public static void register() {
        PowderCell item = new PowderCell();
        GameRegistry.registerItem(item, "powder_cell");
        Upgrades.INVERTER.registerItem(new ItemStack(item), 1);
        AEApi.instance()
            .registries()
            .cell()
            .addCellHandler(item);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add(StatCollector.translateToLocal("gtquality.powder_cell.description"));
        tooltip.add(StatCollector.translateToLocal("gtquality.powder_cell.capacity"));
        tooltip.add(StatCollector.translateToLocal("gtquality.powder_cell.workbench"));
        tooltip.add(StatCollector.translateToLocal("gtquality.powder_cell.inverter"));
        PowderCellConfig forms = new PowderCellConfig(stack);
        PowderAmounts amounts = new PowderAmounts();
        if (stack.hasTagCompound()) amounts.read(
            stack.getTagCompound()
                .getTagList(PowderCellInventory.CONTENTS, 10));
        tooltip.add(StatCollector.translateToLocalFormatted("gtquality.powder_cell.types", amounts.materials.size()));
        amounts.materials.forEach((material, units) -> {
            long outputUnits = forms.unitsPerItem(material);
            long remainder = units % outputUnits;
            ItemStack output = forms.output(material);
            if (remainder > 0) tooltip.add(
                StatCollector.translateToLocalFormatted(
                    "gtquality.powder_cell.remainder",
                    output == null ? material : output.getDisplayName(),
                    remainder / gregapi.data.CS.U72,
                    outputUnits / gregapi.data.CS.U72));
        });
    }

    @Override
    public boolean isEditable(ItemStack stack) {
        return isCell(stack);
    }

    @Override
    public IInventory getConfigInventory(ItemStack stack) {
        return new PowderCellConfig(stack);
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack stack) {
        return new CellUpgrades(stack, 1);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack stack) {
        return FuzzyMode.IGNORE_ALL;
    }

    @Override
    public void setFuzzyMode(ItemStack stack, FuzzyMode mode) {}

    @Override
    public boolean isCell(ItemStack stack) {
        return stack != null && stack.getItem() == this;
    }

    @Override
    public IMEInventoryHandler getCellInventory(ItemStack stack, ISaveProvider host, StorageChannel channel) {
        return isCell(stack) && channel == StorageChannel.ITEMS ? new PowderCellInventory(stack, host) : null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getTopTexture_Light() {
        return ExtraBlockTextures.BlockMEChestItems_Light.getIcon();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getTopTexture_Medium() {
        return ExtraBlockTextures.BlockMEChestItems_Medium.getIcon();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getTopTexture_Dark() {
        return ExtraBlockTextures.BlockMEChestItems_Dark.getIcon();
    }

    @Override
    public void openChestGui(EntityPlayer player, IChestOrDrive chest, ICellHandler handler,
        IMEInventoryHandler inventory, ItemStack stack, StorageChannel channel) {
        Platform.openGUI(player, (TileEntity) chest, chest.getUp(), GuiBridge.GUI_ME);
    }

    @Override
    public int getStatusForCell(ItemStack stack, IMEInventory inventory) {
        return inventory instanceof PowderCellInventory ? ((PowderCellInventory) inventory).status() : 0;
    }

    @Override
    public double cellIdleDrain(ItemStack stack, IMEInventory inventory) {
        return 2.0;
    }
}
