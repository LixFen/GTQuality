package com.plainston.gtquality.integration.ae2;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;
import gregapi.data.MD;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.data.TD;
import gregapi.oredict.OreDictItemData;
import gregapi.oredict.OreDictMaterial;
import gregapi.util.OM;
import gregapi.util.ST;

final class PowderCellInventory implements IMEInventoryHandler<IAEItemStack> {

    static final String CONTENTS = "gtquality.powders";
    private final ItemStack cell;
    private final ISaveProvider host;
    private final PowderAmounts amounts = new PowderAmounts();

    PowderCellInventory(ItemStack cell, ISaveProvider host) {
        this.cell = cell;
        this.host = host;
        if (cell.hasTagCompound()) amounts.read(
            cell.getTagCompound()
                .getTagList(CONTENTS, 10));
    }

    private static OreDictItemData powder(ItemStack stack) {
        if (stack == null || stack.hasTagCompound()) return null;
        OreDictItemData data = OM.anydata_(stack);
        if (data == null || !data.validData() || !data.mPrefix.contains(TD.Prefix.DUST_BASED)) return null;
        if (data.mBlackListed && (data.mMaterial.mMaterial == MT.Glass || !MD.MC.owns(stack))) return null;
        return ST.valid(OP.dust.mat(data.mMaterial.mMaterial, 1)) ? data : null;
    }

    private static ItemStack dust(String material) {
        OreDictMaterial mat = OreDictMaterial.MATERIAL_MAP.get(material);
        return mat == null ? null : OP.dust.mat(mat, 1);
    }

    @Override
    public IAEItemStack injectItems(IAEItemStack input, Actionable mode, BaseActionSource source) {
        OreDictItemData data = input == null ? null : powder(input.getItemStack());
        if (data == null || input.getStackSize() <= 0) return input;
        String material = data.mMaterial.mMaterial.mNameInternal;
        long before = amounts.whole(material);
        long accepted = amounts
            .insert(material, data.mPrefix.mAmount, input.getStackSize(), mode == Actionable.SIMULATE);
        if (accepted == 0) return input;
        if (mode == Actionable.MODULATE) {
            IAEItemStack output = AEItemStack.create(dust(material));
            output.setStackSize(amounts.whole(material) - before);
            PowderCellChanges.converted(input, accepted, output);
            save();
        }
        return accepted == input.getStackSize() ? null
            : input.copy()
                .setStackSize(input.getStackSize() - accepted);
    }

    @Override
    public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource source) {
        OreDictItemData data = request == null ? null : powder(request.getItemStack());
        if (data == null || data.mPrefix != OP.dust) return null;
        String material = data.mMaterial.mMaterial.mNameInternal;
        ItemStack canonical = dust(material);
        if (!ST.equal(canonical, request.getItemStack())) return null;
        long extracted = amounts.extract(material, request.getStackSize(), mode == Actionable.SIMULATE);
        if (extracted == 0) return null;
        if (mode == Actionable.MODULATE) save();
        return request.copy()
            .setStackSize(extracted);
    }

    private void save() {
        if (!cell.hasTagCompound()) cell.setTagCompound(new NBTTagCompound());
        cell.getTagCompound()
            .setTag(CONTENTS, amounts.write());
        if (host != null) host.saveChanges(this);
    }

    @Override
    public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
        amounts.materials.forEach((material, units) -> {
            long count = amounts.whole(material);
            IAEItemStack stack = AEItemStack.create(dust(material));
            if (count > 0 && stack != null) out.add(stack.setStackSize(count));
        });
        return out;
    }

    int status() {
        if (amounts.materials.isEmpty()) return 1;
        if (amounts.materials.size() < PowderAmounts.MAX_TYPES) return 2;
        return amounts.materials.values()
            .stream()
            .allMatch(units -> PowderAmounts.MAX_UNITS - units < gregapi.data.CS.U72) ? 4 : 3;
    }

    @Override
    public boolean canAccept(IAEItemStack input) {
        OreDictItemData data = input == null ? null : powder(input.getItemStack());
        return data != null
            && amounts.insert(data.mMaterial.mMaterial.mNameInternal, data.mPrefix.mAmount, 1, true) > 0;
    }

    @Override
    public boolean isPrioritized(IAEItemStack input) {
        OreDictItemData data = input == null ? null : powder(input.getItemStack());
        return data != null && amounts.materials.containsKey(data.mMaterial.mMaterial.mNameInternal);
    }

    @Override
    public StorageChannel getChannel() {
        return StorageChannel.ITEMS;
    }

    @Override
    public AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(int pass) {
        return true;
    }
}
