package com.plainston.gtquality;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import gregapi.data.CS;
import gregapi.item.multiitem.MultiItem;
import gregapi.item.multiitem.MultiItemTool;
import gregapi.item.multiitem.behaviors.IBehavior;
import gregapi.item.multiitem.behaviors.IBehavior.AbstractBehaviorDefault;
import gregapi.oredict.OreDictPrefix;
import gregtech.items.tools.early.GT_Tool_Chisel;
import gregtech.tileentity.tools.MultiTileEntityMold;

public final class MoldInteraction {

    private static final String LAST_SHAPE_KEY = "gtquality.lastMoldShape";

    private static final Field SHAPE;
    private static final Field CONTENT;
    private static final Field INVENTORY;
    private static final Method UPDATE_CLIENT_DATA;

    static {
        try {
            SHAPE = MultiTileEntityMold.class.getDeclaredField("mShape");
            CONTENT = MultiTileEntityMold.class.getDeclaredField("mContent");
            INVENTORY = Class.forName("gregapi.tileentity.base.TileEntityBase05Inventories")
                .getDeclaredField("mInventory");
            UPDATE_CLIENT_DATA = Class.forName("gregapi.tileentity.base.TileEntityBase03TicksAndSync")
                .getMethod("updateClientData");
            SHAPE.setAccessible(true);
            CONTENT.setAccessible(true);
            INVENTORY.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot access GT6 mold state", e);
        }
    }

    static void install() {
        MultiItemTool tools = CS.ToolsGT.sMetaTool;
        IBehavior<MultiItem> behavior = new MoldChiselBehavior();
        tools.mItemBehaviors.get((short) CS.ToolsGT.CHISEL)
            .add(0, behavior);
        tools.mItemBehaviors.get((short) CS.ToolsGT.POCKET_CHISEL)
            .add(0, behavior);
    }

    private static final class MoldChiselBehavior extends AbstractBehaviorDefault {

        @Override
        public boolean onItemUseFirst(MultiItem item, ItemStack stack, EntityPlayer player, World world, int x, int y,
            int z, byte side, float hitX, float hitY, float hitZ) {
            if (!player.isSneaking()) return false;
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile == null || tile.getClass() != MultiTileEntityMold.class) return false;
            MultiTileEntityMold mold = (MultiTileEntityMold) tile;
            if (!canEdit(mold)) return false;
            if (!world.isRemote) {
                GTQuality.NETWORK.sendTo(
                    new MoldPackets.Open(x, y, z, shape(mold), lastShape(player)),
                    (net.minecraft.entity.player.EntityPlayerMP) player);
            }
            return !world.isRemote;
        }
    }

    static boolean isChisel(ItemStack stack) {
        return stack != null && stack.getItem() instanceof MultiItemTool
            && ((MultiItemTool) stack.getItem()).getToolStats(stack) instanceof GT_Tool_Chisel;
    }

    static int shape(MultiTileEntityMold mold) {
        try {
            return SHAPE.getInt(mold);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    static boolean canEdit(MultiTileEntityMold mold) {
        try {
            return CONTENT.get(mold) == null && ((ItemStack[]) INVENTORY.get(mold))[0] == null;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static int lastShape(EntityPlayer player) {
        return player.getEntityData()
            .getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG)
            .getInteger(LAST_SHAPE_KEY);
    }

    static void select(EntityPlayer player, int x, int y, int z, int selected) {
        if (!isChisel(player.getHeldItem()) || player.getDistanceSq(x + .5, y + .5, z + .5) > 64) return;
        TileEntity tile = player.worldObj.getTileEntity(x, y, z);
        if (!(tile instanceof MultiTileEntityMold) || tile.getClass() != MultiTileEntityMold.class) return;
        MultiTileEntityMold mold = (MultiTileEntityMold) tile;
        if (!canEdit(mold) || (selected != 0 && !choices().containsKey(selected))) return;
        try {
            SHAPE.setInt(mold, selected);
            tile.markDirty();
            UPDATE_CLIENT_DATA.invoke(mold);
            NBTTagCompound data = player.getEntityData();
            NBTTagCompound persisted = data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            persisted.setInteger(LAST_SHAPE_KEY, selected);
            data.setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    static Map<Integer, OreDictPrefix> choices() {
        Map<Integer, OreDictPrefix> choices = new TreeMap<>();
        for (Map.Entry<Integer, OreDictPrefix> entry : MultiTileEntityMold.MOLD_RECIPES.entrySet()) {
            boolean found = false;
            for (OreDictPrefix prefix : choices.values()) {
                if (prefix == entry.getValue()) {
                    found = true;
                    break;
                }
            }
            if (!found) choices.put(entry.getKey(), entry.getValue());
        }
        return choices;
    }
}
