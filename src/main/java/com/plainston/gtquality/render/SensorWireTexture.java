package com.plainston.gtquality.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;

import gregapi.old.Textures;
import gregapi.oredict.OreDictMaterial;
import gregapi.render.BlockTextureDefault;
import gregapi.render.IIconContainer;
import gregapi.render.ITexture;
import gregapi.tileentity.connectors.MultiTileEntityWireRedstone;
import gregapi.tileentity.connectors.MultiTileEntityWireRedstoneInsulated;
import gregapi.util.UT;

/** Render sensor connections in local frames that preserve GT6's texture mapping. */
public final class SensorWireTexture implements ITexture {

    private final ITexture source;
    private final int dx, dy, dz;

    public static ITexture segment(MultiTileEntityWireRedstoneInsulated wire, OreDictMaterial material, byte face,
        byte facing, float diameter, double hubDiameter, boolean trunk) {
        IIconContainer icon;
        int color;
        boolean glow;
        if (wire instanceof MultiTileEntityWireRedstone) {
            icon = material.mTextureSetsBlock.get(wire.getIconIndexSide(face, wire.getDirectionData(), diameter, 0));
            color = wire.getPaint();
            glow = ((MultiTileEntityWireRedstone) wire).mState > 0;
        } else {
            icon = Textures.BlockIcons.INSULATION_FULL;
            color = wire.isPainted() ? wire.getPaint() : UT.Code.getRGBInt(96, 64, 64);
            glow = false;
        }
        return BlockTextureDefault
            .get(new SensorWireIcon(icon, face, facing, diameter, hubDiameter, trunk), color, glow);
    }

    public SensorWireTexture(ITexture source, byte wireSide, byte facing) {
        this(source, SensorWireBend.frontOffset(wireSide, facing));
    }

    public static ITexture lateral(ITexture source, byte wireSide) {
        // The added lateral arm lies wholly inside the sensor block. Its local bounds stay
        // within [0, 1], so vanilla keeps the original wire's pixel scale instead of squeezing
        // a complete icon into this short extension outside the wire's own block.
        return new SensorWireTexture(source, SensorWireBend.sensorOffset(wireSide));
    }

    private SensorWireTexture(ITexture source, int[] offset) {
        this.source = source;
        dx = offset[0];
        dy = offset[1];
        dz = offset[2];
    }

    private void render(byte face, RenderBlocks renderer, Block block, int x, int y, int z, int brightness,
        boolean changedBounds) {
        double minX = renderer.renderMinX, minY = renderer.renderMinY, minZ = renderer.renderMinZ;
        double maxX = renderer.renderMaxX, maxY = renderer.renderMaxY, maxZ = renderer.renderMaxZ;
        // Vanilla resets UVs to the full icon when transverse bounds leave [0, 1]. Move the
        // origin to the hypothetical front wire, keeping the final world vertices unchanged.
        renderer.renderMinX = minX - dx;
        renderer.renderMinY = minY - dy;
        renderer.renderMinZ = minZ - dz;
        renderer.renderMaxX = maxX - dx;
        renderer.renderMaxY = maxY - dy;
        renderer.renderMaxZ = maxZ - dz;
        try {
            switch (face) {
                case 0:
                    source.renderYNeg(renderer, block, x + dx, y + dy, z + dz, brightness, changedBounds);
                    break;
                case 1:
                    source.renderYPos(renderer, block, x + dx, y + dy, z + dz, brightness, changedBounds);
                    break;
                case 2:
                    source.renderZNeg(renderer, block, x + dx, y + dy, z + dz, brightness, changedBounds);
                    break;
                case 3:
                    source.renderZPos(renderer, block, x + dx, y + dy, z + dz, brightness, changedBounds);
                    break;
                case 4:
                    source.renderXNeg(renderer, block, x + dx, y + dy, z + dz, brightness, changedBounds);
                    break;
                case 5:
                    source.renderXPos(renderer, block, x + dx, y + dy, z + dz, brightness, changedBounds);
                    break;
            }
        } finally {
            renderer.renderMinX = minX;
            renderer.renderMinY = minY;
            renderer.renderMinZ = minZ;
            renderer.renderMaxX = maxX;
            renderer.renderMaxY = maxY;
            renderer.renderMaxZ = maxZ;
        }
    }

    @Override
    public void renderYNeg(RenderBlocks renderer, Block block, int x, int y, int z, int brightness,
        boolean changedBounds) {
        render((byte) 0, renderer, block, x, y, z, brightness, changedBounds);
    }

    @Override
    public void renderYPos(RenderBlocks renderer, Block block, int x, int y, int z, int brightness,
        boolean changedBounds) {
        render((byte) 1, renderer, block, x, y, z, brightness, changedBounds);
    }

    @Override
    public void renderZNeg(RenderBlocks renderer, Block block, int x, int y, int z, int brightness,
        boolean changedBounds) {
        render((byte) 2, renderer, block, x, y, z, brightness, changedBounds);
    }

    @Override
    public void renderZPos(RenderBlocks renderer, Block block, int x, int y, int z, int brightness,
        boolean changedBounds) {
        render((byte) 3, renderer, block, x, y, z, brightness, changedBounds);
    }

    @Override
    public void renderXNeg(RenderBlocks renderer, Block block, int x, int y, int z, int brightness,
        boolean changedBounds) {
        render((byte) 4, renderer, block, x, y, z, brightness, changedBounds);
    }

    @Override
    public void renderXPos(RenderBlocks renderer, Block block, int x, int y, int z, int brightness,
        boolean changedBounds) {
        render((byte) 5, renderer, block, x, y, z, brightness, changedBounds);
    }

    @Override
    public boolean isValidTexture() {
        return source.isValidTexture();
    }
}
