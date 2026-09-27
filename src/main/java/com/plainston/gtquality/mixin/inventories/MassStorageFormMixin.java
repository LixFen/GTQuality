package com.plainston.gtquality.mixin.inventories;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregapi.code.ItemStackContainer;
import gregapi.code.ItemStackSet;
import gregapi.data.CS;
import gregapi.data.MD;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.data.TD;
import gregapi.oredict.OreDictItemData;
import gregapi.oredict.OreDictPrefix;
import gregapi.tileentity.inventories.MultiTileEntityMassStorage;
import gregapi.util.OM;
import gregapi.util.ST;

@Mixin(value = MultiTileEntityMassStorage.class, remap = false)
public abstract class MassStorageFormMixin {

    @Shadow
    private ItemStackSet<ItemStackContainer> mLogisticsCache;

    @Inject(method = "onToolClick2", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtquality$changeStoredForm(String tool, long remainingDurability, long quality, Entity player,
        List<String> chat, IInventory playerInventory, boolean sneaking, ItemStack heldStack, byte side, float hitX,
        float hitY, float hitZ, CallbackInfoReturnable<Long> cir) {
        if (!CS.TOOL_chisel.equals(tool)) return;
        MultiTileEntityMassStorage storage = (MultiTileEntityMassStorage) (Object) this;
        if (storage.isClientSide() || (storage.mMode & CS.B[3]) != 0 || !storage.slotHas(1)) return;

        ItemStack current = storage.slot(1);
        OreDictItemData data = OM.anydata_(current);
        if (data == null || !data.validData()
            || (data.mBlackListed && (data.mMaterial.mMaterial == MT.Glass || !MD.MC.owns(current)))) return;
        OreDictPrefix[] forms = gtquality$forms(data.mPrefix);
        if (forms == null) return;

        int currentIndex = -1;
        for (int i = 0; i < forms.length; i++) {
            if (forms[i] == data.mPrefix) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex < 0) return;

        long totalUnits = storage.getUnitAmount(data.mPrefix) * current.stackSize + storage.mPartialUnits;
        for (int i = 1; i < forms.length; i++) {
            OreDictPrefix target = forms[(currentIndex + i) % forms.length];
            long targetUnits = storage.getUnitAmount(target);
            long count = totalUnits / targetUnits;
            if (count > storage.getMaxContent()) continue;
            ItemStack replacement = target.mat(data.mMaterial.mMaterial, count);
            if (ST.invalid(replacement)) continue;

            storage.slot(1, replacement);
            storage.mPartialUnits = totalUnits % targetUnits;
            mLogisticsCache = null;
            storage.updateClientData();
            storage.updateInventory();
            if (chat != null) chat.add("Storage form: " + replacement.getDisplayName());
            cir.setReturnValue(100L);
            return;
        }
    }

    @Unique
    private static OreDictPrefix[] gtquality$forms(OreDictPrefix prefix) {
        if (prefix.contains(TD.Prefix.DUST_BASED))
            return new OreDictPrefix[] { OP.dustSmall, OP.dust, OP.blockDust, OP.dustDiv72, OP.dustTiny };
        if (prefix.contains(TD.Prefix.INGOT_BASED))
            return new OreDictPrefix[] { OP.ingot, OP.blockIngot, OP.billet, OP.chunkGt, OP.nugget };
        if (prefix.contains(TD.Prefix.WIRE_BASED)) return new OreDictPrefix[] { OP.wireGt01, OP.wireGt02, OP.wireGt03,
            OP.wireGt04, OP.wireGt05, OP.wireGt06, OP.wireGt07, OP.wireGt08, OP.wireGt09, OP.wireGt10, OP.wireGt11,
            OP.wireGt12, OP.wireGt13, OP.wireGt14, OP.wireGt15, OP.wireGt16 };
        if (prefix == OP.gem || prefix == OP.blockGem) return new OreDictPrefix[] { OP.gem, OP.blockGem };
        if (prefix == OP.plate || prefix == OP.blockPlate) return new OreDictPrefix[] { OP.plate, OP.blockPlate };
        if (prefix == OP.plateGem || prefix == OP.blockPlateGem)
            return new OreDictPrefix[] { OP.plateGem, OP.blockPlateGem };
        if (prefix == OP.crushed || prefix == OP.crushedTiny) return new OreDictPrefix[] { OP.crushed, OP.crushedTiny };
        if (prefix == OP.crushedPurified || prefix == OP.crushedPurifiedTiny)
            return new OreDictPrefix[] { OP.crushedPurified, OP.crushedPurifiedTiny };
        if (prefix == OP.crushedCentrifuged || prefix == OP.crushedCentrifugedTiny)
            return new OreDictPrefix[] { OP.crushedCentrifuged, OP.crushedCentrifugedTiny };
        if (prefix == OP.oreRaw || prefix == OP.blockRaw) return new OreDictPrefix[] { OP.oreRaw, OP.blockRaw };
        return null;
    }
}
