package com.plainston.gtquality.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;

import gregapi.old.Textures;
import gregapi.render.BlockTextureDefault;
import gregapi.render.BlockTextureMulti;
import gregapi.render.IIconContainer;
import gregapi.render.ITexture;

public final class PipeFlowTexture {

    // World directions at each face's top, bottom, left and right texture border.
    // Bottom arrows use a 180-degree UV rotation to keep them aligned on off-centre pipe arms.
    private static final byte[][] BORDER_SIDES = { { 3, 2, 5, 4 }, { 2, 3, 4, 5 }, { 1, 0, 5, 4 }, { 1, 0, 4, 5 },
        { 1, 0, 2, 3 }, { 1, 0, 3, 2 } };

    private static final IIconContainer[] ARROWS = { new Textures.BlockIcons.CustomIcon("gtquality:pipe/flow_up"),
        new Textures.BlockIcons.CustomIcon("gtquality:pipe/flow_down"),
        new Textures.BlockIcons.CustomIcon("gtquality:pipe/flow_left"),
        new Textures.BlockIcons.CustomIcon("gtquality:pipe/flow_right") };

    private PipeFlowTexture() {}

    public static void init() {
        // Register icons before GT6's texture stitch callbacks run.
    }

    public static ITexture withArrows(ITexture base, byte face, int disabledInputs) {
        ITexture[] layers = new ITexture[5];
        layers[0] = base;
        for (int border = 0; border < 4; border++) {
            if ((disabledInputs & (1 << BORDER_SIDES[face][border])) != 0)
                layers[border + 1] = new ArrowTexture(ARROWS[border]);
        }
        return new BlockTextureMulti(layers);
    }

    private static final class ArrowTexture extends BlockTextureDefault {

        private ArrowTexture(IIconContainer icon) {
            super(icon, new short[] { 255, 255, 255, 255 }, false, false, false, true);
        }

        // Keep GT6's normal north/east flip handling: the outer renderer has already set flipTexture.
        // Its bottom-face helper instead mirrors V within the current bounds; rotation 3 gives absolute UVs.
        @Override
        public void renderYNeg(RenderBlocks renderer, Block block, int x, int y, int z, int brightness,
            boolean changedBounds) {
            int previousRotation = renderer.uvRotateBottom;
            renderer.uvRotateBottom = 3;
            try {
                super.renderYNeg(renderer, block, x, y, z, brightness, changedBounds);
            } finally {
                renderer.uvRotateBottom = previousRotation;
            }
        }
    }
}
