package com.plainston.gtquality.integration.blockrenderer;

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;

import com.plainston.gtquality.GTQuality;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.event.NEIRegisterHandlerInfosEvent;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;

/** Loaded only by the NEI plugin after the optional BlockRenderer6343 mod has been detected. */
public final class GT6MultiblockHandler extends TemplateRecipeHandler {

    static final String ID = "gtquality.multiblock";
    static final int WIDTH = 166;
    static final int SCENE_Y = 20, SCENE_HEIGHT = 110, BUTTON_Y = 133, MATERIAL_Y = 176;
    private static final Field GUI_LEFT = ReflectionHelper.findField(GuiContainer.class, "guiLeft", "field_147003_i");
    private static final Field GUI_TOP = ReflectionHelper.findField(GuiContainer.class, "guiTop", "field_147009_r");
    private static List<GT6MultiblockCatalog.Page> catalog = Collections.emptyList();
    private MultiblockScene scene;
    private CachedPage activePage;

    public static void register() {
        catalog = GT6MultiblockCatalog.collect();
        if (catalog.isEmpty()) return;
        GT6MultiblockHandler handler = new GT6MultiblockHandler();
        API.registerRecipeHandler(handler);
        API.registerUsageHandler(handler);
        MinecraftForge.EVENT_BUS.register(handler);
        handler.registerHandlerInfo(new NEIRegisterHandlerInfosEvent());
    }

    @SubscribeEvent
    public void registerHandlerInfo(NEIRegisterHandlerInfosEvent event) {
        event.registerHandlerInfo(
            ID,
            "GTQuality",
            GTQuality.MODID,
            builder -> builder.setDisplayStack(
                catalog.get(0)
                    .controllerStack())
                .setWidth(WIDTH)
                .setHeight(230)
                .setMaxRecipesPerPage(1)
                .setMultipleWidgetsAllowed(false)
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
        return "blockrenderer6343:textures/void.png";
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (ID.equals(outputId) || "all".equals(outputId)) {
            for (GT6MultiblockCatalog.Page page : catalog) arecipes.add(new CachedPage(page));
        } else super.loadCraftingRecipes(outputId, results);
    }

    @Override
    public void loadUsageRecipes(String inputId, Object... ingredients) {
        if (ID.equals(inputId) || "all".equals(inputId)) loadCraftingRecipes(ID);
        else super.loadUsageRecipes(inputId, ingredients);
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (GT6MultiblockCatalog.Page page : catalog) {
            if (sameType(page.controllerStack(), result)) arecipes.add(new CachedPage(page));
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        for (GT6MultiblockCatalog.Page page : catalog) {
            if (sameType(page.controllerStack(), ingredient) || page.ingredients.stream()
                .anyMatch(stack -> sameType(stack, ingredient))) {
                arecipes.add(new CachedPage(page));
            }
        }
    }

    private static boolean sameType(ItemStack first, ItemStack second) {
        // Machine contents, facing and other saved state do not change its structure recipe.
        return first != null && second != null
            && first.getItem() == second.getItem()
            && first.getItemDamage() == second.getItemDamage();
    }

    @Override
    public int recipiesPerPage() {
        return 1;
    }

    @Override
    public int getRecipeHeight(int recipe) {
        return MATERIAL_Y + ((page(recipe).ingredients.size() + 8) / 9) * 18 + 2;
    }

    private CachedPage page(int recipe) {
        return (CachedPage) arecipes.get(recipe);
    }

    private MultiblockScene scene(int recipe) {
        CachedPage next = page(recipe);
        if (scene == null) scene = new MultiblockScene();
        if (activePage != next) {
            scene.load(next.page);
            activePage = next;
        }
        return scene;
    }

    @Override
    public void drawBackground(int recipe) {
        GuiRecipe<?> gui = (GuiRecipe<?>) NEIClientUtils.getGuiContainer();
        Point mouse = GuiDraw.getMousePosition();
        Point relative = relativeMouse(gui, recipe);
        scene(recipe).draw(mouse.x - relative.x, mouse.y - relative.y, mouse);
    }

    @Override
    public void drawExtras(int recipe) {
        MultiblockScene view = scene(recipe);
        var font = Minecraft.getMinecraft().fontRenderer;
        font.drawString(
            font.trimStringToWidth(
                page(recipe).page.controllerStack()
                    .getDisplayName(),
                WIDTH - 22),
            22,
            5,
            0x404040);
        String[] buttons = { "<", ">", "-", "+", translate("all"), "R" };
        int[] positions = { 0, 24, 48, 72, 96, 142 };
        for (int i = 0; i < buttons.length; i++) {
            int x = positions[i], width = i == 4 ? 42 : 22;
            net.minecraft.client.gui.Gui.drawRect(x, BUTTON_Y, x + width, BUTTON_Y + 14, 0xffbbbbbb);
            font.drawString(buttons[i], x + 4, BUTTON_Y + 3, 0x303030);
        }
        font.drawString(translate("layer", view.layerText()), 0, 151, 0x404040);
        font.drawString(translate("materials"), 0, 164, 0x404040);
    }

    @Override
    public boolean mouseClicked(GuiRecipe<?> gui, int button, int recipe) {
        Point mouse = relativeMouse(gui, recipe);
        return scene(recipe).click(mouse.x, mouse.y, button);
    }

    @Override
    public boolean mouseScrolled(GuiRecipe<?> gui, int scroll, int recipe) {
        Point mouse = relativeMouse(gui, recipe);
        if (mouse.x < 0 || mouse.x >= WIDTH || mouse.y < SCENE_Y || mouse.y >= SCENE_Y + SCENE_HEIGHT) return false;
        scene(recipe).zoom(scroll);
        return true;
    }

    private static Point relativeMouse(GuiRecipe<?> gui, int recipe) {
        Point mouse = GuiDraw.getMousePosition();
        Point offset = gui.getRecipePosition(recipe);
        try {
            return new Point(mouse.x - GUI_LEFT.getInt(gui) - offset.x, mouse.y - GUI_TOP.getInt(gui) - offset.y);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot read NEI GUI position", exception);
        }
    }

    @Override
    public List<String> handleTooltip(GuiRecipe<?> gui, List<String> currenttip, int recipe) {
        if (!currenttip.isEmpty()) return currenttip;
        Point mouse = relativeMouse(gui, recipe);
        if (mouse.x >= 0 && mouse.x < WIDTH && mouse.y >= SCENE_Y && mouse.y < SCENE_Y + SCENE_HEIGHT) {
            MovingObjectPosition hit = scene(recipe).hit();
            if (hit != null) {
                ItemStack stack = page(recipe).page.stackAt(hit.blockX, hit.blockY, hit.blockZ);
                if (stack != null) currenttip.addAll(stack.getTooltip(Minecraft.getMinecraft().thePlayer, false));
                else if (page(recipe).page.bedrock) currenttip.add(translate("bedrock"));
            }
            currenttip.add(translate("help"));
        } else if (mouse.x >= 0 && mouse.x < WIDTH && mouse.y >= BUTTON_Y && mouse.y < BUTTON_Y + 14) {
            currenttip.add(translate("buttons"));
        }
        return currenttip;
    }

    static String translate(String key, Object... arguments) {
        return StatCollector.translateToLocalFormatted("gtquality.nei.multiblock." + key, arguments);
    }

    private final class CachedPage extends CachedRecipe {

        final GT6MultiblockCatalog.Page page;
        final List<PositionedStack> ingredients = new ArrayList<>();
        final PositionedStack controller;

        CachedPage(GT6MultiblockCatalog.Page page) {
            this.page = page;
            controller = new PositionedStack(page.controllerStack(), 1, 1, false);
            for (int i = 0; i < page.ingredients.size(); i++) {
                ingredients.add(
                    new PositionedStack(
                        page.ingredients.get(i)
                            .copy(),
                        (i % 9) * 18 + 1,
                        MATERIAL_Y + (i / 9) * 18,
                        false));
            }
        }

        @Override
        public PositionedStack getResult() {
            return controller;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return ingredients;
        }
    }
}
