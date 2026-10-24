package com.plainston.gtquality.render;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;

import org.junit.jupiter.api.Test;

import gregapi.render.BlockTextureDefault;
import gregapi.render.IIconContainer;
import gregapi.render.ITexture;

class SensorWireTextureTest {

    @Test
    void sharedTrunkMatchesOriginalFrontWireUvsRegardlessOfOwner() throws Exception {
        IIconContainer icons = icons();
        ITexture source = texture(icons);
        boolean reproducedOldUvMismatch = false;
        for (byte facing = 0; facing < 6; facing++) {
            for (byte side = 0; side < 6; side++) {
                if (side == facing) continue;
                for (double diameter : new double[] { .125, .25, .5 }) {
                    double[] actualBounds = SensorWireBend.bounds(side, facing, diameter, diameter, true);
                    double[] frontBounds = originalArm(facing, diameter);
                    int[] origin = new int[3];
                    origin[axis(side)] += side % 2 == 0 ? -1 : 1;
                    origin[axis(facing)] += facing % 2 == 0 ? -1 : 1;
                    for (byte face = 0; face < 6; face++) {
                        if (axis(face) == axis(facing)) continue;
                        double[][] original = vertices(source, frontBounds, face, origin);
                        double[][] corrected = vertices(
                            new SensorWireTexture(
                                texture(new SensorWireIcon(icons, face, facing, diameter, diameter, true)),
                                side,
                                facing),
                            actualBounds,
                            face,
                            new int[3]);
                        double[][] old = vertices(
                            new SensorWireTexture(source, side, facing),
                            actualBounds,
                            face,
                            new int[3]);
                        for (int vertex = 0; vertex < 4; vertex++) {
                            for (int uv = 3; uv < 5; uv++) {
                                double expected = interpolate(original, corrected[vertex], uv);
                                assertEquals(
                                    expected,
                                    corrected[vertex][uv],
                                    .000001,
                                    "facing=" + facing + ", owner=" + side + ", face=" + face);
                                reproducedOldUvMismatch |= Math.abs(old[vertex][uv] - expected) > .001;
                            }
                        }
                    }
                }
            }
        }
        assertTrue(reproducedOldUvMismatch, "the previous origin-only correction must fail against a complete GT6 arm");
    }

    @Test
    void frontBranchKeepsTheSameUvScaleWhenSplitAtADifferentSizeHub() throws Exception {
        IIconContainer icons = icons();
        for (byte facing = 0; facing < 6; facing++) {
            for (double diameter : new double[] { .125, .25, .5 }) {
                for (double hub : new double[] { diameter, .5 }) {
                    double[] full = originalArm(facing, diameter), branch = full.clone();
                    branch[axis(facing) + (facing % 2 == 0 ? 3 : 0)] = facing % 2 == 0 ? 1.5 - hub / 2 : -.5 + hub / 2;
                    for (byte face = 0; face < 6; face++) {
                        if (axis(face) == axis(facing)) continue;
                        double[][] original = vertices(texture(icons), full, face, new int[3]);
                        double[][] split = vertices(
                            texture(new SensorWireIcon(icons, face, facing, diameter, hub, false)),
                            branch,
                            face,
                            new int[3]);
                        for (double[] vertex : split) {
                            for (int uv = 3; uv < 5; uv++) {
                                assertEquals(
                                    interpolate(original, vertex, uv),
                                    vertex[uv],
                                    .000001,
                                    "facing=" + facing + ", face=" + face + ", diameter=" + diameter + ", hub=" + hub);
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    void lateralExtensionsKeepTheOriginalWirePixelScaleWithoutMovingVertices() throws Exception {
        ITexture source = texture(icons());
        boolean reproducedCompression = false;
        for (byte facing = 0; facing < 6; facing++) {
            for (byte side = 0; side < 6; side++) {
                if (!SensorWireBend.isLateral(side, facing)) continue;
                for (double diameter : new double[] { .125, .25, .5 }) {
                    for (double hub : new double[] { diameter, .5 }) {
                        double[] bounds = SensorWireBend.bounds(side, facing, diameter, hub, false);
                        for (byte face = 0; face < 6; face++) {
                            if (axis(face) == axis(side)) continue;
                            double[][] old = vertices(source, bounds, face, new int[3]);
                            double[][] corrected = vertices(
                                SensorWireTexture.lateral(source, side),
                                bounds,
                                face,
                                new int[3]);
                            for (int vertex = 0; vertex < 4; vertex++) {
                                for (int coordinate = 0; coordinate < 3; coordinate++) {
                                    assertEquals(old[vertex][coordinate], corrected[vertex][coordinate]);
                                }
                                for (int uv = 3; uv < 5; uv++) {
                                    assertTrue(corrected[vertex][uv] >= 0 && corrected[vertex][uv] <= 1);
                                }
                                int next = (vertex + 1) % 4;
                                double length = 0;
                                for (int coordinate = 0; coordinate < 3; coordinate++) {
                                    length += Math.abs(corrected[vertex][coordinate] - corrected[next][coordinate]);
                                }
                                double uvLength = Math.max(
                                    Math.abs(corrected[vertex][3] - corrected[next][3]),
                                    Math.abs(corrected[vertex][4] - corrected[next][4]));
                                // A 16px wire icon must retain 16 pixels per block on both face axes,
                                // just like the original arm inside its own block.
                                assertEquals(
                                    length,
                                    uvLength,
                                    .000001,
                                    "facing=" + facing + ", side=" + side + ", face=" + face);
                                double oldUvLength = Math.max(
                                    Math.abs(old[vertex][3] - old[next][3]),
                                    Math.abs(old[vertex][4] - old[next][4]));
                                reproducedCompression |= oldUvLength > length + .001;
                            }
                        }
                    }
                }
            }
        }
        assertTrue(reproducedCompression, "the previous side extension must reproduce texture compression");
    }

    private static double[] originalArm(byte facing, double diameter) {
        // Directly reproduce GT6's setBlockBounds2: body edge to 14/16 into the adjacent sensor.
        double radius = diameter / 2;
        double[] full = { .5 - radius, .5 - radius, .5 - radius, .5 + radius, .5 + radius, .5 + radius };
        if (facing % 2 != 0) {
            full[axis(facing)] = -.875;
            full[axis(facing) + 3] = .5 - radius;
        } else {
            full[axis(facing)] = .5 + radius;
            full[axis(facing) + 3] = 1.875;
        }
        return full;
    }

    private static double interpolate(double[][] quad, double[] point, int uv) {
        double value = quad[0][uv];
        for (int adjacent : new int[] { 1, 3 }) {
            for (int axis = 0; axis < 3; axis++) {
                double length = quad[adjacent][axis] - quad[0][axis];
                if (Math.abs(length) > .000001) {
                    value += (point[axis] - quad[0][axis]) / length * (quad[adjacent][uv] - quad[0][uv]);
                    break;
                }
            }
        }
        return value;
    }

    private static double[][] vertices(ITexture texture, double[] bounds, byte face, int[] origin) throws Exception {
        RenderBlocks renderer = new RenderBlocks();
        renderer.renderMinX = bounds[0];
        renderer.renderMinY = bounds[1];
        renderer.renderMinZ = bounds[2];
        renderer.renderMaxX = bounds[3];
        renderer.renderMaxY = bounds[4];
        renderer.renderMaxZ = bounds[5];
        renderer.partialRenderBounds = true;
        boolean partial = renderer.partialRenderBounds;
        // Match GT6's outer renderer on north/east faces.
        renderer.flipTexture = face == 2 || face == 5;
        Block block = new Block(Material.rock) {};
        Tessellator tessellator = Tessellator.instance;
        Field drawing = Tessellator.class.getDeclaredField("isDrawing");
        drawing.setAccessible(true);
        tessellator.startDrawingQuads();
        try {
            int x = origin[0], y = origin[1], z = origin[2];
            switch (face) {
                case 0:
                    texture.renderYNeg(renderer, block, x, y, z, 240, true);
                    break;
                case 1:
                    texture.renderYPos(renderer, block, x, y, z, 240, true);
                    break;
                case 2:
                    texture.renderZNeg(renderer, block, x, y, z, 240, true);
                    break;
                case 3:
                    texture.renderZPos(renderer, block, x, y, z, 240, true);
                    break;
                case 4:
                    texture.renderXNeg(renderer, block, x, y, z, 240, true);
                    break;
                case 5:
                    texture.renderXPos(renderer, block, x, y, z, 240, true);
                    break;
            }
            assertArrayEquals(
                bounds,
                new double[] { renderer.renderMinX, renderer.renderMinY, renderer.renderMinZ, renderer.renderMaxX,
                    renderer.renderMaxY, renderer.renderMaxZ });
            assertEquals(partial, renderer.partialRenderBounds);
            Field buffer = Tessellator.class.getDeclaredField("rawBuffer");
            buffer.setAccessible(true);
            int[] raw = (int[]) buffer.get(tessellator);
            double[][] result = new double[4][5];
            for (int vertex = 0; vertex < 4; vertex++) {
                for (int component = 0; component < 5; component++) {
                    result[vertex][component] = Float.intBitsToFloat(raw[vertex * 8 + component]);
                }
            }
            return result;
        } finally {
            // Inspect submitted vertices without needing an OpenGL context or GPU flush.
            drawing.setBoolean(tessellator, false);
        }
    }

    private static int axis(byte side) {
        return side < 2 ? 1 : side < 4 ? 2 : 0;
    }

    private static IIconContainer icons() {
        IIcon icon = (IIcon) Proxy
            .newProxyInstance(IIcon.class.getClassLoader(), new Class<?>[] { IIcon.class }, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getInterpolatedU":
                    case "getInterpolatedV":
                        return (float) ((double) args[0] / 16);
                    case "getMinU":
                    case "getMinV":
                        return 0F;
                    case "getMaxU":
                    case "getMaxV":
                        return 1F;
                    case "getIconWidth":
                    case "getIconHeight":
                        return 16;
                    case "getIconName":
                        return "sensor-wire-uv";
                    default:
                        throw new AssertionError(method);
                }
            });
        IIconContainer container = (IIconContainer) Proxy.newProxyInstance(
            IIconContainer.class.getClassLoader(),
            new Class<?>[] { IIconContainer.class },
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getIcon":
                        return icon;
                    case "getIconPasses":
                        return 1;
                    case "isUsingColorModulation":
                        return false;
                    case "getIconColor":
                        return new short[] { 255, 255, 255, 255 };
                    default:
                        throw new AssertionError(method);
                }
            });
        return container;
    }

    private static ITexture texture(IIconContainer icons) {
        return new BlockTextureDefault(icons, new short[] { 255, 255, 255, 255 }, false, false, false, true);
    }
}
