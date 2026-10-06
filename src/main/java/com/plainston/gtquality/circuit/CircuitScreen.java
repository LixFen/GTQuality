package com.plainston.gtquality.circuit;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;

import com.plainston.gtquality.GTQuality;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
final class CircuitScreen extends GuiScreen {

    private final int slot;
    private final int original;

    private CircuitScreen(EntityPlayer player) {
        slot = player.inventory.currentItem;
        original = player.getHeldItem()
            .getItemDamage();
    }

    static void open(EntityPlayer player) {
        Minecraft.getMinecraft()
            .displayGuiScreen(new CircuitScreen(player));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        buttonList.clear();
        for (int number = 0; number <= 24; number++) {
            GuiButton button = new GuiButton(
                number,
                width / 2 - 100 + number % 5 * 41,
                height / 2 - 50 + number / 5 * 22,
                36,
                20,
                Integer.toString(number));
            button.enabled = number != (original & 255);
            buttonList.add(button);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        GTQuality.NETWORK.sendToServer(new CircuitPackets.Select(slot, original, button.id));
        mc.displayGuiScreen(null);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(
            fontRendererObj,
            StatCollector.translateToLocal("gtquality.circuit.title"),
            width / 2,
            height / 2 - 82,
            0xffffff);
        drawCenteredString(
            fontRendererObj,
            StatCollector.translateToLocalFormatted("gtquality.circuit.current", original & 255),
            width / 2,
            height / 2 - 67,
            0xaaaaaa);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
