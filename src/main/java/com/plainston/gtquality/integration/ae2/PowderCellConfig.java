package com.plainston.gtquality.integration.ae2;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.items.contents.CellUpgrades;
import gregapi.data.MD;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.data.TD;
import gregapi.oredict.OreDictItemData;
import gregapi.oredict.OreDictMaterial;
import gregapi.util.OM;
import gregapi.util.ST;

/** Workbench ghost slots select output forms; an inverter enables a material whitelist. */
final class PowderCellConfig extends InventoryBasic {

    private static final String FORMS = "gtquality.powderForms";
    private final ItemStack cell;
    private boolean loaded;

    PowderCellConfig(ItemStack cell) {
        super("gtquality.powder_cell", false, 63);
        this.cell = cell;
        if (cell.hasTagCompound()) {
            NBTTagList list = cell.getTagCompound()
                .getTagList(FORMS, 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound entry = list.getCompoundTagAt(i);
                int slot = entry.getInteger("slot");
                if (slot >= 0 && slot < getSizeInventory()) {
                    setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(entry));
                }
            }
        }
        loaded = true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (!loaded) return;
        NBTTagList list = new NBTTagList();
        for (int slot = 0; slot < getSizeInventory(); slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (stack == null) continue;
            NBTTagCompound entry = new NBTTagCompound();
            ST.amount(1, stack)
                .writeToNBT(entry);
            entry.setInteger("slot", slot);
            list.appendTag(entry);
        }
        if (!cell.hasTagCompound()) cell.setTagCompound(new NBTTagCompound());
        cell.getTagCompound()
            .setTag(FORMS, list);
    }

    static OreDictItemData powder(ItemStack stack) {
        if (stack == null || stack.hasTagCompound()) return null;
        OreDictItemData data = OM.anydata_(stack);
        if (data == null || !data.validData() || !data.mPrefix.contains(TD.Prefix.DUST_BASED)) return null;
        if (data.mBlackListed && (data.mMaterial.mMaterial == MT.Glass || !MD.MC.owns(stack))) return null;
        return ST.valid(OP.dust.mat(data.mMaterial.mMaterial, 1)) ? data : null;
    }

    ItemStack output(String material) {
        ItemStack marked = markedOutput(material);
        if (marked != null) return marked;
        OreDictMaterial mat = OreDictMaterial.MATERIAL_MAP.get(material);
        return mat == null ? null : OP.dust.mat(mat, 1);
    }

    private ItemStack markedOutput(String material) {
        for (int slot = 0; slot < getSizeInventory(); slot++) {
            ItemStack stack = getStackInSlot(slot);
            OreDictItemData data = powder(stack);
            if (data != null && data.mMaterial.mMaterial.mNameInternal.equals(material)) return ST.amount(1, stack);
        }
        return null;
    }

    static boolean hasInverter(ItemStack cell) {
        return hasInverter(new CellUpgrades(cell, 1));
    }

    static boolean hasInverter(IInventory upgrades) {
        ItemStack upgrade = upgrades.getStackInSlot(0);
        return upgrade != null && upgrade.getItem() instanceof IUpgradeModule
            && ((IUpgradeModule) upgrade.getItem()).getType(upgrade) == Upgrades.INVERTER;
    }

    boolean acceptsMaterial(String material) {
        return !hasInverter(cell) || markedOutput(material) != null;
    }

    long unitsPerItem(String material) {
        OreDictItemData data = powder(output(material));
        return data == null ? gregapi.data.CS.U : data.mPrefix.mAmount;
    }
}
