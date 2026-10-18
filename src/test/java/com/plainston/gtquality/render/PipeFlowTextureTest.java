package com.plainston.gtquality.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import gregapi.render.IIconContainer;
import gregapi.render.ITexture;
import gregapi.render.RendererBlockTextured;

class PipeFlowTextureTest {

    private static IIconContainer[] icons;
    private static IIconContainer[] originalIcons;

    @BeforeAll
    static void installAtlasFixtures() throws Exception {
        Field field = PipeFlowTexture.class.getDeclaredField("ARROWS");
        field.setAccessible(true);
        icons = (IIconContainer[]) field.get(null);
        originalIcons = icons.clone();
        for (int border = 0; border < icons.length; border++) icons[border] = icon(border);
    }

    @AfterAll
    static void restoreAtlas() {
        if (originalIcons != null) System.arraycopy(originalIcons, 0, icons, 0, icons.length);
    }

    @TestFactory
    Stream<DynamicTest> arrowsStayAtTheirOutputPortOnEveryVisibleFace() {
        List<DynamicTest> tests = new ArrayList<>();
        for (byte face = 0; face < 6; face++) {
            for (byte port = 0; port < 6; port++) {
                if (face / 2 == port / 2) continue;
                for (double diameter : new double[] { 1, .25, .75 }) {
                    final byte renderedFace = face, outputPort = port;
                    tests.add(
                        DynamicTest.dynamicTest(
                            "face=" + face + ", port=" + port + ", diameter=" + diameter,
                            () -> checkArrow(renderedFace, outputPort, diameter)));
                }
            }
        }
        return tests.stream();
    }

    private static void checkArrow(byte face, byte port, double diameter) throws Exception {
        RenderBlocks renderer = new RenderBlocks();
        double[] min = { (1 - diameter) / 2, (1 - diameter) / 2, (1 - diameter) / 2 };
        double[] max = { 1 - min[0], 1 - min[1], 1 - min[2] };
        // Match a real pipe arm, including off-centre bounds that expose mirrored UV cropping.
        int axis = port < 2 ? 1 : port < 4 ? 2 : 0;
        if (diameter < 1) {
            if (port % 2 == 0) {
                max[axis] = min[axis];
                min[axis] = 0;
            } else {
                min[axis] = max[axis];
                max[axis] = 1;
            }
        }
        renderer.renderMinX = min[0];
        renderer.renderMinY = min[1];
        renderer.renderMinZ = min[2];
        renderer.renderMaxX = max[0];
        renderer.renderMaxY = max[1];
        renderer.renderMaxZ = max[2];
        ITexture texture = PipeFlowTexture.withArrows(null, face, 1 << port);
        Block block = new Block(Material.rock) {};
        Tessellator tessellator = Tessellator.instance;
        Field drawing = Tessellator.class.getDeclaredField("isDrawing");
        drawing.setAccessible(true);
        tessellator.startDrawingQuads();
        try {
            render(face, renderer, block, texture, diameter == 1);
            Field buffer = Tessellator.class.getDeclaredField("rawBuffer");
            buffer.setAccessible(true);
            int[] raw = (int[]) buffer.get(tessellator);
            double[][] vertices = new double[4][5];
            for (int vertex = 0; vertex < 4; vertex++) {
                for (int component = 0; component < 5; component++) {
                    vertices[vertex][component] = Float.intBitsToFloat(raw[vertex * 8 + component]);
                }
            }
            int border = (int) Math.floor(vertices[0][3] / 2);
            int uv = border < 2 ? 4 : 3;
            double tip = border % 2 == 0 ? .08 : .92;
            if (uv == 3) tip += border * 2;
            for (int vertex = 1; vertex < 4; vertex++) {
                double deltaUV = vertices[vertex][uv] - vertices[0][uv];
                if (Math.abs(deltaUV) < .00001) continue;
                double slope = (vertices[vertex][axis] - vertices[0][axis]) / deltaUV;
                double arrowDirection = slope * (border % 2 == 0 ? -1 : 1);
                assertTrue(arrowDirection * (port % 2 == 0 ? -1 : 1) > 0, "arrow points into the wrong port");
                double tipWorld = vertices[0][axis] + (tip - vertices[0][uv]) * slope;
                assertEquals(port % 2 == 0 ? .08 : .92, tipWorld, .00001, "arrow tip must stay near its port");
                return;
            }
            throw new AssertionError("Arrow did not produce a textured quad");
        } finally {
            // No OpenGL context is needed: inspect submitted vertices instead of flushing them to the GPU.
            drawing.setBoolean(tessellator, false);
        }
    }

    private static void render(byte face, RenderBlocks renderer, Block block, ITexture texture, boolean fullBlock) {
        switch (face) {
            case 0:
                RendererBlockTextured
                    .renderNegativeYFacing(null, renderer, block, 0, 0, 0, texture, fullBlock, true, null);
                break;
            case 1:
                RendererBlockTextured
                    .renderPositiveYFacing(null, renderer, block, 0, 0, 0, texture, fullBlock, true, null);
                break;
            case 2:
                RendererBlockTextured
                    .renderNegativeZFacing(null, renderer, block, 0, 0, 0, texture, fullBlock, true, null);
                break;
            case 3:
                RendererBlockTextured
                    .renderPositiveZFacing(null, renderer, block, 0, 0, 0, texture, fullBlock, true, null);
                break;
            case 4:
                RendererBlockTextured
                    .renderNegativeXFacing(null, renderer, block, 0, 0, 0, texture, fullBlock, true, null);
                break;
            case 5:
                RendererBlockTextured
                    .renderPositiveXFacing(null, renderer, block, 0, 0, 0, texture, fullBlock, true, null);
                break;
            default:
                throw new AssertionError(face);
        }
    }

    private static IIconContainer icon(int border) {
        IIcon atlasIcon = (IIcon) Proxy
            .newProxyInstance(IIcon.class.getClassLoader(), new Class<?>[] { IIcon.class }, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getInterpolatedU":
                        return (float) (border * 2 + (double) args[0] / 16);
                    case "getInterpolatedV":
                        return (float) ((double) args[0] / 16);
                    case "getMinU":
                        return (float) (border * 2);
                    case "getMaxU":
                        return (float) (border * 2 + 1);
                    case "getMinV":
                        return 0F;
                    case "getMaxV":
                        return 1F;
                    case "getIconWidth":
                    case "getIconHeight":
                        return 64;
                    case "getIconName":
                        return "arrow-" + border;
                    default:
                        throw new AssertionError(method);
                }
            });
        return new IIconContainer() {

            public IIcon getIcon(int pass) {
                return atlasIcon;
            }

            public boolean isUsingColorModulation(int pass) {
                return false;
            }

            public short[] getIconColor(int pass) {
                return new short[] { 255, 255, 255, 255 };
            }

            public int getIconPasses() {
                return 1;
            }

            public ResourceLocation getTextureFile() {
                return null;
            }

            public void registerIcons(IIconRegister register) {}
        };
    }
}
