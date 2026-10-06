package com.plainston.gtquality.integration.nei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;

import com.plainston.gtquality.GTQuality;

import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.event.NEIRegisterHandlerInfosEvent;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.GuiRecipeButton;
import codechicken.nei.recipe.RecipeHandlerRef;
import codechicken.nei.recipe.TemplateRecipeHandler;

public final class WorldgenNEIHandler extends TemplateRecipeHandler {

    public static final String ID = "gtquality.worldgen";
    private static final int WIDTH = 166;
    private static volatile List<WorldgenCatalog.Page> catalog = Collections.emptyList();

    static void register() {
        // NEI loads plugins after mod loading, before starting its parallel recipe/item searches.
        catalog = WorldgenCatalog.collect();
        WorldgenNEIHandler handler = new WorldgenNEIHandler();
        API.registerRecipeHandler(handler);
        API.registerUsageHandler(handler);
        // Keep this listener registered: NEI can reload handler metadata independently of plugin loading.
        MinecraftForge.EVENT_BUS.register(handler);
        // The initial metadata event fires at FML load-complete, before NEI loads its plugins.
        handler.registerHandlerInfo(new NEIRegisterHandlerInfosEvent());
    }

    @cpw.mods.fml.common.eventhandler.SubscribeEvent
    public void registerHandlerInfo(NEIRegisterHandlerInfosEvent event) {
        event.registerHandlerInfo(
            ID,
            "GTQuality",
            GTQuality.MODID,
            builder -> builder.setDisplayStack(new ItemStack(Blocks.gold_ore))
                .setWidth(WIDTH)
                .setHeight(150)
                .setMultipleWidgetsAllowed(false)
                .setAllowOverflowY(false)
                .setShowOverlayButton(false)
                .setShowFavoritesButton(false));
    }

    @Override
    public String getHandlerId() {
        return ID;
    }

    @Override
    public String getOverlayIdentifier() {
        return ID;
    }

    @Override
    public String getRecipeName() {
        return translate("title");
    }

    @Override
    public String getGuiTexture() {
        return "nei:textures/gui/recipebg.png";
    }

    @cpw.mods.fml.common.eventhandler.SubscribeEvent
    public void addPageButtons(GuiRecipeButton.UpdateRecipeButtonsEvent.Post event) {
        RecipeHandlerRef ref = (RecipeHandlerRef) NEIButtonEventCompat.handlerRef(event);
        if (!(ref.handler instanceof WorldgenNEIHandler handler)) return;
        CachedPage page = (CachedPage) handler.arecipes.get(ref.recipeIndex);
        page.layout();
        event.buttonList.add(new PageButton(ref, page, false));
        if (page.biomesToggleY >= 0) event.buttonList.add(new PageButton(ref, page, true));
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (ID.equals(outputId) || "all".equals(outputId)) {
            for (WorldgenCatalog.Page page : catalog) arecipes.add(new CachedPage(page));
        } else super.loadCraftingRecipes(outputId, results);
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        loadMatching(result, false);
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        loadMatching(ingredient, true);
    }

    @Override
    public void loadUsageRecipes(String inputId, Object... ingredients) {
        if ("all".equals(inputId) || ID.equals(inputId)) loadCraftingRecipes(ID);
        else super.loadUsageRecipes(inputId, ingredients);
    }

    private void loadMatching(ItemStack stack, boolean usage) {
        if (stack == null) return;
        for (WorldgenCatalog.Page page : catalog) {
            if (page.matches(stack, usage)) arecipes.add(new CachedPage(page));
        }
    }

    @Override
    public void drawBackground(int recipe) {}

    @Override
    public void drawExtras(int recipe) {
        CachedPage page = (CachedPage) arecipes.get(recipe);
        page.layout();
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer font = mc.fontRenderer;
        for (Line line : page.lines) font.drawString(line.text, line.x, line.y, line.color);
    }

    @Override
    public int getRecipeHeight(int recipe) {
        CachedPage page = (CachedPage) arecipes.get(recipe);
        page.layout();
        return page.height;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        // NEI automatically cycles input groups; information previews are output groups so page buttons are available.
        if (cycleticks % 20 != 0 || NEIClientUtils.shiftKey()) return;
        for (CachedRecipe recipe : arecipes) {
            for (PositionedStack preview : ((CachedPage) recipe).stacks) cyclePreview(preview, 1);
        }
    }

    @Override
    public boolean mouseScrolled(GuiRecipe<?> gui, int scroll, int recipe) {
        CachedPage page = (CachedPage) arecipes.get(recipe);
        page.layout();
        for (PositionedStack preview : page.stacks) {
            if (preview.items.length > 1 && gui.isMouseOver(preview, recipe)) {
                cyclePreview(preview, scroll > 0 ? -1 : 1);
                return true;
            }
        }
        return false;
    }

    private static void cyclePreview(PositionedStack preview, int direction) {
        int size = preview.items.length;
        if (size > 1)
            preview.setPermutationToRender((preview.getPermutationIndex(preview.item) + direction + size) % size);
    }

    private static String translate(String key, Object... arguments) {
        return StatCollector.translateToLocalFormatted("gtquality.nei.worldgen." + key, arguments);
    }

    private final class CachedPage extends CachedRecipe {

        private final WorldgenCatalog.Page page;
        private final List<PositionedStack> stacks = new ArrayList<>();
        private final List<Line> lines = new ArrayList<>();
        private String language;
        private int height;
        private boolean biomesExpanded;
        private int biomesToggleY = -1;

        CachedPage(WorldgenCatalog.Page page) {
            this.page = page;
        }

        private void layout() {
            Minecraft mc = Minecraft.getMinecraft();
            String currentLanguage = mc.getLanguageManager()
                .getCurrentLanguage()
                .getLanguageCode();
            if (currentLanguage.equals(language)) return;
            language = currentLanguage;
            lines.clear();
            stacks.clear();
            biomesToggleY = -1;
            int y = text(translate("type." + page.type), 4, 25, 0x28536B);
            int index = 0;
            for (List<WorldgenCatalog.Row> group : page.previewGroups()) {
                WorldgenCatalog.Row row = group.get(0);
                List<ItemStack> variants = group.stream()
                    .map(entry -> entry.stack.copy())
                    .collect(Collectors.toList());
                PositionedStack preview = new PositionedStack(
                    variants,
                    5 + (index % 8) * 20,
                    y + (index / 8) * 20,
                    false);
                preview.setTooltip(
                    Collections.singletonList(StatCollector.translateToLocalFormatted(row.key, row.arguments)));
                stacks.add(preview);
                index++;
            }
            y += ((index + 7) / 8) * 20 + 4;
            for (WorldgenCatalog.Row row : page.rows) {
                if (row.stack != null) continue;
                String value = StatCollector.translateToLocalFormatted(row.key, row.arguments);
                List<String> wrapped = mc.fontRenderer.listFormattedStringToWidth(value, WIDTH - 8);
                if (row.key.equals("gtquality.nei.worldgen.biomes") && wrapped.size() > 3) {
                    biomesToggleY = y;
                    y += 19;
                    if (!biomesExpanded) wrapped = wrapped.subList(0, 3);
                }
                for (String line : wrapped) {
                    lines.add(new Line(line, 4, y, 0x404040));
                    y += mc.fontRenderer.FONT_HEIGHT + 2;
                }
                y += 3;
            }
            y = text(translate("configured_rules"), 4, y + 4, 0x666666);
            y = text(translate("source", page.id), 4, y + 3, 0x666666);
            String worlds = page.worlds.stream()
                .map(world -> translate("world." + world))
                .collect(Collectors.joining(", "));
            height = text(translate("worlds", worlds), 4, y + 4, 0x404040) + 4;
        }

        private int text(String value, int x, int y, int color) {
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            for (Object line : font.listFormattedStringToWidth(value, WIDTH - x - 4)) {
                lines.add(new Line(line.toString(), x, y, color));
                y += font.FONT_HEIGHT + 2;
            }
            return y;
        }

        @Override
        public PositionedStack getResult() {
            return null;
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            layout();
            return stacks;
        }
    }

    private static final class PageButton extends GuiRecipeButton {

        private final CachedPage page;
        private final boolean toggleBiomes;

        PageButton(RecipeHandlerRef ref, CachedPage page, boolean toggleBiomes) {
            super(ref, 4, toggleBiomes ? page.biomesToggleY : 4, toggleBiomes ? 1 : 0, "");
            this.page = page;
            this.toggleBiomes = toggleBiomes;
            width = WIDTH - 8;
            height = 16;
            update();
        }

        @Override
        public void update() {
            displayString = translate(
                toggleBiomes ? page.biomesExpanded ? "collapse_biomes" : "expand_biomes" : "all_pages");
        }

        @Override
        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            if (!super.mousePressed(mc, mouseX, mouseY)) return false;
            if (toggleBiomes) {
                page.biomesExpanded = !page.biomesExpanded;
                page.language = null;
                page.layout();
                if (mc.currentScreen instanceof GuiRecipe<?>gui) gui.forceRefreshPage();
            } else GuiCraftingRecipe.openRecipeGui(ID);
            return true;
        }

        @Override
        public List<String> handleTooltip(List<String> tooltip) {
            tooltip.add(displayString);
            return tooltip;
        }

        @Override
        public Map<String, String> handleHotkeys(int mouseX, int mouseY, Map<String, String> hotkeys) {
            return hotkeys;
        }

        @Override
        public void lastKeyTyped(char keyChar, int keyID) {}

        @Override
        public void drawItemOverlay() {}
    }

    private static final class Line {

        final String text;
        final int x;
        final int y;
        final int color;

        Line(String text, int x, int y, int color) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }
}
