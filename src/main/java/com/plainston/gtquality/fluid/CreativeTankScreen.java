package com.plainston.gtquality.fluid;

import java.util.Arrays;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import com.plainston.gtquality.GTQuality;

import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.StackInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregapi.data.FL;
import gregapi.gui.ContainerClient;

@SideOnly(Side.CLIENT)
public final class CreativeTankScreen extends ContainerClient {

    private final CreativeTank tank;
    private GuiTextField rate;
    private GuiButton automatic;

    public CreativeTankScreen(InventoryPlayer inventory, CreativeTank tank) {
        super(new CreativeTankContainer(inventory, tank), "gregtech:textures/gui/chests/1.png");
        this.tank = tank;
    }

    private static String text(String key) {
        return StatCollector.translateToLocal("gtquality.creative_tank." + key);
    }

    @Override
    public void initGui() {
        super.initGui();
        rate = new GuiTextField(fontRendererObj, guiLeft + 8, guiTop + 65, 88, 16);
        rate.setMaxStringLength(10);
        rate.setText(Integer.toString(tank.outputRate));
        automatic = new GuiButton(2, guiLeft + 8, guiTop + 42, 160, 20, "");
        buttonList.add(automatic);
        buttonList.add(new GuiButton(1, guiLeft + 102, guiTop + 63, 66, 20, text("apply")));
        buttonList.add(new GuiButton(3, guiLeft + 147, guiTop + 18, 21, 20, "X"));
    }

    public boolean overFluid(int x, int y) {
        return x >= guiLeft + 7 && x < guiLeft + 25 && y >= guiTop + 19 && y < guiTop + 37;
    }

    public boolean select(ItemStack stack) {
        if (stack == null) return false;
        FluidStack fluid;
        if (stack.getItem() instanceof gregapi.item.ItemFluidDisplay
            || stack.getItem() instanceof codechicken.nei.item.ItemFluidDisplay) {
            // Both display items store the fluid ID in metadata, even with no stored fluid amount.
            Fluid displayed = FluidRegistry.getFluid(stack.getItemDamage());
            fluid = displayed == null ? null : new FluidStack(displayed, 1);
        } else {
            fluid = StackInfo.getFluid(stack);
        }
        if (fluid == null) fluid = FL.getFluid(stack.copy(), true);
        if (fluid == null) return false;
        fluid = fluid.copy();
        fluid.amount = 1;
        send(0, 0, fluid);
        return true;
    }

    private void send(int action, int value, FluidStack fluid) {
        GTQuality.NETWORK
            .sendToServer(new CreativeTankPackets.Configure(inventorySlots.windowId, action, value, fluid));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            try {
                int value = Integer.parseInt(rate.getText());
                if (value >= 0) {
                    send(1, value, null);
                    rate.setFocused(false);
                }
            } catch (NumberFormatException ignored) {
                rate.setText(Integer.toString(tank.outputRate));
            }
        } else send(button.id, 0, null);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        rate.mouseClicked(x, y, button);
        if (overFluid(x, y) && (button == 0 || button == 1)) {
            // This branch skips GuiContainer.mouseClicked, where NEI normally injects its input hook.
            if (GuiContainerManager.getManager(this)
                .mouseClicked(x, y, button)) return;
            if (button == 1 && mc.thePlayer.inventory.getItemStack() == null) send(3, 0, null);
            else select(mc.thePlayer.inventory.getItemStack());
            return;
        }
        super.mouseClicked(x, y, button);
    }

    @Override
    protected void keyTyped(char character, int key) {
        if (rate.isFocused()) {
            if (key == 28 || key == 156) actionPerformed((GuiButton) buttonList.get(1));
            else if (key == 1) rate.setFocused(false);
            else if (Character.isDigit(character) || Character.isISOControl(character))
                rate.textboxKeyTyped(character, key);
            return;
        }
        super.keyTyped(character, key);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        rate.updateCursorCounter();
        if (!rate.isFocused()) rate.setText(Integer.toString(tank.outputRate));
        automatic.displayString = text("automatic") + ": " + text(tank.autoOutput ? "on" : "off");
    }

    @Override
    protected void drawGuiContainerBackgroundLayer2(float partialTicks, int x, int y) {
        super.drawGuiContainerBackgroundLayer2(partialTicks, x, y);
        drawRect(guiLeft + 79, guiTop + 34, guiLeft + 98, guiTop + 53, 0xFFC6C6C6);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        drawTexturedModalRect(guiLeft + 7, guiTop + 19, 79, 34, 18, 18);
        FluidStack fluid = ((CreativeTankContainer) inventorySlots).fluid;
        if (fluid != null) {
            IIcon icon = fluid.getFluid()
                .getIcon(fluid);
            if (icon == null) icon = mc.getTextureMapBlocks()
                .getAtlasSprite("missingno");
            int color = fluid.getFluid()
                .getColor(fluid);
            mc.getTextureManager()
                .bindTexture(TextureMap.locationBlocksTexture);
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F, 1.0F);
            drawTexturedModelRectFromIcon(guiLeft + 8, guiTop + 20, icon, 16, 16);
            GL11.glPopAttrib();
        }
        rate.drawTextBox();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        fontRendererObj.drawString(text("name"), 8, 6, 0x404040);
        FluidStack fluid = ((CreativeTankContainer) inventorySlots).fluid;
        String name = fluid == null ? text("empty") : fluid.getLocalizedName();
        fontRendererObj.drawString(fontRendererObj.trimStringToWidth(name, 114), 29, 25, 0x404040);
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        super.drawScreen(x, y, partialTicks);
        if (overFluid(x, y)) {
            FluidStack fluid = ((CreativeTankContainer) inventorySlots).fluid;
            drawHoveringText(
                Arrays.asList(fluid == null ? text("empty") : fluid.getLocalizedName(), text("fluid_tip")),
                x,
                y,
                fontRendererObj);
        }
    }
}
