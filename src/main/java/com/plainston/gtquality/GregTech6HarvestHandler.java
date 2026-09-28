package com.plainston.gtquality;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;

import com.gtnewhorizons.wdmla.api.harvestability.EffectiveTool;
import com.gtnewhorizons.wdmla.api.harvestability.HarvestLevel;
import com.gtnewhorizons.wdmla.api.harvestability.HarvestabilityInfo;
import com.gtnewhorizons.wdmla.api.harvestability.HarvestabilityTestPhase;
import com.gtnewhorizons.wdmla.api.provider.HarvestHandler;
import com.gtnewhorizons.wdmla.plugin.harvestability.VanillaHarvestToolHandler;

import gregapi.data.CS;
import gregapi.data.LH;
import gregapi.data.MD;
import gregapi.data.MT;
import gregapi.item.multiitem.MultiItemTool;

public enum GregTech6HarvestHandler implements HarvestHandler {

    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation("gtquality", "gregtech6_harvest");
    private static final Map<String, EffectiveTool> TOOLS = new HashMap<>();

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public int getDefaultPriority() {
        return 3000;
    }

    @Override
    public boolean testHarvest(HarvestabilityInfo info, HarvestabilityTestPhase phase, EntityPlayer player, Block block,
        int meta, MovingObjectPosition position) {
        if (!MD.GT.owns(block) && !MD.GAPI.owns(block)) return true;

        if (phase == HarvestabilityTestPhase.EFFECTIVE_TOOL_NAME) {
            if (!info.getEffectiveTool().isSameTool(EffectiveTool.CANNOT_HARVEST)) {
                String tool = block.getHarvestTool(meta);
                info.setEffectiveTool(
                    tool == null || tool.isEmpty() ? EffectiveTool.NO_TOOL : TOOLS.computeIfAbsent(tool, this::makeTool));
            }
        } else if (phase == HarvestabilityTestPhase.CURRENTLY_HARVESTABLE
            || phase == HarvestabilityTestPhase.IS_HELD_TOOL_EFFECTIVE) {
                ItemStack held = player.getHeldItem();
                if (held != null && held.getItem() instanceof MultiItemTool) {
                    boolean effective = ((MultiItemTool) held.getItem()).getDigSpeed(held.copy(), block, meta) > 0;
                    if (phase == HarvestabilityTestPhase.CURRENTLY_HARVESTABLE) {
                        info.setCurrentlyHarvestable(block.getMaterial().isToolNotRequired() || effective);
                    } else {
                        info.setHeldToolEffective(effective && info.isCurrentlyHarvestable());
                    }
                }
            }
        return true;
    }

    private EffectiveTool makeTool(String name) {
        if (CS.TOOL_pickaxe.equals(name)) return VanillaHarvestToolHandler.TOOL_PICKAXE;
        if (CS.TOOL_shovel.equals(name)) return VanillaHarvestToolHandler.TOOL_SHOVEL;
        if (CS.TOOL_axe.equals(name)) return VanillaHarvestToolHandler.TOOL_AXE;
        if (CS.TOOL_sword.equals(name)) return VanillaHarvestToolHandler.TOOL_SWORD;
        return new GT6Tool(name, iconFor(name));
    }

    private ItemStack iconFor(String name) {
        ItemStack icon = null;
        if (CS.TOOL_wrench.equals(name)) {
            icon = CS.ToolsGT.sMetaTool.getToolWithStats(CS.ToolsGT.WRENCH, MT.Steel, MT.Wood);
        } else if (CS.TOOL_cutter.equals(name)) {
            icon = CS.ToolsGT.sMetaTool.getToolWithStats(CS.ToolsGT.WIRECUTTER, MT.Steel, MT.Wood);
        } else if (CS.TOOL_scoop.equals(name)) {
            icon = CS.ToolsGT.sMetaTool.getToolWithStats(CS.ToolsGT.SCOOP, MT.Steel, MT.Wood);
        }
        if (CS.TOOL_shears.equals(name)) return new ItemStack(Items.shears);
        if (icon != null) {
            NBTTagCompound tag = icon.getTagCompound();
            if (tag == null) tag = new NBTTagCompound();
            tag.setBoolean("gtquality.hideHarvestIconDurability", true);
            icon.setTagCompound(tag);
        }
        return icon;
    }

    private static class GT6Tool extends EffectiveTool {

        private final ItemStack icon;

        GT6Tool(String name, ItemStack icon) {
            super(name, icon == null ? null : Collections.singletonList(icon));
            this.icon = icon;
        }

        @Override
        public ItemStack getIcon(HarvestLevel level) {
            return icon != null && level.isToolRequired() ? icon : null;
        }

        @Override
        public String getLocalizedName() {
            return LH.get(CS.TOOL_LOCALISER_PREFIX + value, super.getLocalizedName());
        }
    }
}
