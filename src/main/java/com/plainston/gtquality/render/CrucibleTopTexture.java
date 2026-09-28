package com.plainston.gtquality.render;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import gregapi.render.BlockTextureDefault;
import gregapi.render.BlockTextureMulti;
import gregapi.render.IIconContainer;
import gregapi.render.ITexture;
import gregtech.tileentity.multiblocks.MultiTileEntityCrucible;

public final class CrucibleTopTexture {

    private CrucibleTopTexture() {}

    public static ITexture forWall(MultiTileEntityCrucible crucible, int renderPass, byte side) {
        IIconContainer[] textures = side == crucible.mFacing ? crucible.mTexturesFront : crucible.mTextures;
        double minU = renderPass == 0 ? 0 : renderPass == 2 ? 5.0 / 6.0 : 0;
        double maxU = renderPass == 0 ? 1.0 / 6.0 : 1;
        double minV = renderPass == 1 ? 0 : renderPass == 3 ? 5.0 / 6.0 : 0;
        double maxV = renderPass == 1 ? 1.0 / 6.0 : 1;

        return BlockTextureMulti.get(
            BlockTextureDefault
                .get(new CroppedContainer(textures[1], minU, maxU, minV, maxV), crucible.mRenderedRGBA, true),
            BlockTextureDefault.get(new CroppedContainer(textures[4], minU, maxU, minV, maxV), true));
    }

    private static final class CroppedContainer implements IIconContainer {

        private final IIconContainer source;
        private final double minU, maxU, minV, maxV;

        private CroppedContainer(IIconContainer source, double minU, double maxU, double minV, double maxV) {
            this.source = source;
            this.minU = minU;
            this.maxU = maxU;
            this.minV = minV;
            this.maxV = maxV;
        }

        @Override
        public IIcon getIcon(int renderPass) {
            IIcon icon = source.getIcon(renderPass);
            return icon == null ? null : new CroppedIcon(icon, minU, maxU, minV, maxV);
        }

        @Override
        public boolean isUsingColorModulation(int renderPass) {
            return source.isUsingColorModulation(renderPass);
        }

        @Override
        public short[] getIconColor(int renderPass) {
            return source.getIconColor(renderPass);
        }

        @Override
        public int getIconPasses() {
            return source.getIconPasses();
        }

        @Override
        public ResourceLocation getTextureFile() {
            return source.getTextureFile();
        }

        @Override
        public void registerIcons(IIconRegister register) {
            source.registerIcons(register);
        }
    }

    private static final class CroppedIcon implements IIcon {

        private final IIcon source;
        private final double minU, maxU, minV, maxV;

        private CroppedIcon(IIcon source, double minU, double maxU, double minV, double maxV) {
            this.source = source;
            this.minU = minU;
            this.maxU = maxU;
            this.minV = minV;
            this.maxV = maxV;
        }

        @Override
        public int getIconWidth() {
            return source.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return source.getIconHeight();
        }

        @Override
        public float getMinU() {
            return source.getInterpolatedU(minU * 16);
        }

        @Override
        public float getMaxU() {
            return source.getInterpolatedU(maxU * 16);
        }

        @Override
        public float getInterpolatedU(double position) {
            return source.getInterpolatedU((minU + (maxU - minU) * position / 16) * 16);
        }

        @Override
        public float getMinV() {
            return source.getInterpolatedV(minV * 16);
        }

        @Override
        public float getMaxV() {
            return source.getInterpolatedV(maxV * 16);
        }

        @Override
        public float getInterpolatedV(double position) {
            return source.getInterpolatedV((minV + (maxV - minV) * position / 16) * 16);
        }

        @Override
        public String getIconName() {
            return source.getIconName();
        }
    }
}
