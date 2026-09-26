package com.plainston.gtquality;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregapi.oredict.OreDictPrefix;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

@SideOnly(Side.CLIENT)
final class MoldScreen extends GuiScreen {

    private final int x, y, z, currentShape;
    private final List<Map.Entry<Integer, OreDictPrefix>> choices = new ArrayList<>();
    private int page;

    private MoldScreen(int x, int y, int z, int currentShape, final int lastShape) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.currentShape = currentShape;
        choices.addAll(MoldInteraction.choices().entrySet());
        Collections.sort(choices, new Comparator<Map.Entry<Integer, OreDictPrefix>>() {
            @Override
            public int compare(Map.Entry<Integer, OreDictPrefix> left, Map.Entry<Integer, OreDictPrefix> right) {
                if (left.getKey().equals(right.getKey())) return 0;
                if (left.getKey() == lastShape) return -1;
                if (right.getKey() == lastShape) return 1;
                boolean leftRaw = left.getValue().mNameLocal.toLowerCase(Locale.ROOT).startsWith("raw ");
                boolean rightRaw = right.getValue().mNameLocal.toLowerCase(Locale.ROOT).startsWith("raw ");
                if (leftRaw != rightRaw) return leftRaw ? 1 : -1;
                int nameOrder = left.getValue().mNameLocal.compareToIgnoreCase(right.getValue().mNameLocal);
                return nameOrder != 0 ? nameOrder : left.getKey().compareTo(right.getKey());
            }
        });
    }

    static void open(int x, int y, int z, int shape, int lastShape) {
        Minecraft.getMinecraft().displayGuiScreen(new MoldScreen(x, y, z, shape, lastShape));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        buttonList.clear();
        int left = width / 2 - 100;
        for (int i = 0; i < 8 && page * 8 + i < choices.size(); i++) {
            Map.Entry<Integer, OreDictPrefix> choice = choices.get(page * 8 + i);
            buttonList.add(new GuiButton(i, left, height / 2 - 82 + i * 21, 200, 20,
                choice.getValue().mNameLocal));
        }
        buttonList.add(new GuiButton(8, left, height / 2 + 91, 65, 20, "<"));
        buttonList.add(new GuiButton(9, left + 68, height / 2 + 91, 64, 20, "清空"));
        buttonList.add(new GuiButton(10, left + 135, height / 2 + 91, 65, 20, ">"));
        ((GuiButton) buttonList.get(buttonList.size() - 3)).enabled = page > 0;
        ((GuiButton) buttonList.get(buttonList.size() - 1)).enabled = (page + 1) * 8 < choices.size();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 8 || button.id == 10) {
            page += button.id == 8 ? -1 : 1;
            initGui();
            return;
        }
        int shape = button.id == 9 ? 0 : choices.get(page * 8 + button.id).getKey();
        GTQuality.NETWORK.sendToServer(new MoldPackets.Select(x, y, z, shape));
        mc.displayGuiScreen(null);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        String current = "自定义形状";
        if (currentShape == 0) current = "未雕刻";
        else {
            OreDictPrefix prefix = gregtech.tileentity.tools.MultiTileEntityMold.MOLD_RECIPES.get(currentShape);
            if (prefix != null) current = prefix.mNameLocal;
        }
        drawCenteredString(fontRendererObj, "选择模具雕刻类型", width / 2, height / 2 - 114, 0xffffff);
        drawCenteredString(fontRendererObj, "当前: " + current, width / 2, height / 2 - 101, 0xaaaaaa);
        drawCenteredString(fontRendererObj, (page + 1) + "/" + Math.max(1, (choices.size() + 7) / 8),
            width / 2, height / 2 + 82, 0xaaaaaa);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
