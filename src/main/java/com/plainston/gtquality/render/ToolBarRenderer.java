package com.plainston.gtquality.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;

import com.plainston.gtquality.GTQuality;

import gregapi.data.CS;
import gregapi.data.TD;
import gregapi.item.IItemEnergy;
import gregapi.item.multiitem.MultiItemTool;

public final class ToolBarRenderer implements IItemRenderer {

    private static final ToolBarRenderer INSTANCE = new ToolBarRenderer();
    private static final RenderItem ICON_RENDERER = new RenderItem();

    private ToolBarRenderer() {}

    public static void install() {
        MinecraftForgeClient.registerItemRenderer(CS.ToolsGT.sMetaTool, INSTANCE);
    }

    @Override
    public boolean handleRenderType(ItemStack stack, ItemRenderType type) {
        return GTQuality.gtmToolBars && type == ItemRenderType.INVENTORY;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack stack, ItemRendererHelper helper) {
        return false;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        MultiItemTool tool = (MultiItemTool) stack.getItem();
        int passes = tool.getRenderPasses(stack.getItemDamage()) - 2;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        for (int pass = 0; pass < passes; pass++) {
            IIcon icon = tool.getIcon(stack, pass);
            int color = tool.getColorFromItemStack(stack, pass);
            GL11.glColor4f((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, 1);
            ICON_RENDERER.renderIcon(0, 0, icon, 16, 16);
        }
        GL11.glColor4f(1, 1, 1, 1);
        if (!stack.hasTagCompound() || !stack.getTagCompound()
            .getBoolean("gtquality.hideHarvestIconDurability")) {
            render(tool, stack, 0, 0);
        }
        GL11.glPopAttrib();
    }

    public static void render(MultiItemTool tool, ItemStack stack, int x, int y) {
        long maxDamage = MultiItemTool.getToolMaxDamage(stack);
        boolean durability = maxDamage > 0;
        if (durability) {
            long remaining = Math.max(0, maxDamage - MultiItemTool.getToolDamage(stack));
            int width = (int) Math.round(13.0 * Math.min(remaining, maxDamage) / maxDamage);
            drawBar(x, y + 13, width, 0x147c00, 0x73ff59);
        }

        IItemEnergy energy = tool.getEnergyStats(stack);
        if (energy != null) {
            long capacity = energy.getEnergyCapacity(TD.Energy.EU, stack);
            long charge = energy.getEnergyStored(TD.Energy.EU, stack);
            if (capacity > 0 && charge > 0) {
                int width = (int) Math.round(13.0 * Math.min(charge, capacity) / capacity);
                drawBar(x, y + (durability ? 11 : 13), width, 0x0065b2, 0xd9eeff);
            }
        }
    }

    private static void drawBar(int x, int y, int width, int left, int right) {
        if (width <= 3) {
            left = 0x7a0000;
            right = 0xff1b1b;
        }
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_LIGHTING_BIT);
        int shadeModel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        rectangle(x + 2, y, 13, 2, 0xff000000);
        gradient(x + 2, y, width, left, right);
        GL11.glShadeModel(shadeModel);
        GL11.glPopAttrib();
    }

    private static void rectangle(int x, int y, int width, int height, int color) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_I(color & 0xffffff, color >>> 24);
        tessellator.addVertex(x, y + height, 190);
        tessellator.addVertex(x + width, y + height, 190);
        tessellator.addVertex(x + width, y, 190);
        tessellator.addVertex(x, y, 190);
        tessellator.draw();
    }

    private static void gradient(int x, int y, int width, int left, int right) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(left);
        tessellator.addVertex(x, y + 1, 191);
        tessellator.setColorOpaque_I(right);
        tessellator.addVertex(x + width, y + 1, 191);
        tessellator.addVertex(x + width, y, 191);
        tessellator.setColorOpaque_I(left);
        tessellator.addVertex(x, y, 191);
        tessellator.draw();
    }
}
