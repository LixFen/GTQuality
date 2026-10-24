package com.plainston.gtquality.render;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import gregapi.render.IIconContainer;

/** Crop a split connection out of the UV range of GT6's complete, extended front arm. */
public final class SensorWireIcon implements IIconContainer {

    private final IIconContainer source;
    private final double minU, maxU, minV, maxV;

    public SensorWireIcon(IIconContainer source, byte face, byte facing, double diameter, double hubDiameter,
        boolean trunk) {
        this.source = source;
        int axis = axis(facing), normal = axis(face), uAxis = face < 4 ? 0 : 2;
        double min = 0, max = 1;
        if (axis != normal) {
            double radius = diameter / 2, hubRadius = hubDiameter / 2;
            // Original arm length: 14/16 into the sensor + (1 - diameter)/2 to the wire body.
            // Split that full UV interval at the hub, rather than stretching a fresh icon on each part.
            double split = facing % 2 != 0 ? (.375 + hubRadius) / (1.375 - radius)
                : (1 - radius - hubRadius) / (1.375 - radius);
            boolean lower = (facing % 2 != 0) == trunk;
            min = lower ? 0 : split;
            max = lower ? split : 1;
            // Match GT6's negative-Y helper and RenderBlocks' default side-face directions.
            if (axis == 1 || (axis == 0 && face == 2) || (axis == 2 && face == 5)) {
                double previousMin = min;
                min = 1 - max;
                max = 1 - previousMin;
            }
        }
        minU = axis == uAxis && axis != normal ? min : 0;
        maxU = axis == uAxis && axis != normal ? max : 1;
        minV = axis != uAxis && axis != normal ? min : 0;
        maxV = axis != uAxis && axis != normal ? max : 1;
    }

    private static int axis(byte side) {
        return side < 2 ? 1 : side < 4 ? 2 : 0;
    }

    @Override
    public IIcon getIcon(int pass) {
        IIcon icon = source.getIcon(pass);
        return icon == null ? null : new CroppedIcon(icon);
    }

    @Override
    public boolean isUsingColorModulation(int pass) {
        return source.isUsingColorModulation(pass);
    }

    @Override
    public short[] getIconColor(int pass) {
        return source.getIconColor(pass);
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

    private final class CroppedIcon implements IIcon {

        private final IIcon icon;

        private CroppedIcon(IIcon icon) {
            this.icon = icon;
        }

        @Override
        public int getIconWidth() {
            return icon.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return icon.getIconHeight();
        }

        @Override
        public String getIconName() {
            return icon.getIconName();
        }

        @Override
        public float getMinU() {
            return icon.getInterpolatedU(minU * 16);
        }

        @Override
        public float getMaxU() {
            return icon.getInterpolatedU(maxU * 16);
        }

        @Override
        public float getMinV() {
            return icon.getInterpolatedV(minV * 16);
        }

        @Override
        public float getMaxV() {
            return icon.getInterpolatedV(maxV * 16);
        }

        @Override
        public float getInterpolatedU(double position) {
            return icon.getInterpolatedU(minU * 16 + (maxU - minU) * position);
        }

        @Override
        public float getInterpolatedV(double position) {
            return icon.getInterpolatedV(minV * 16 + (maxV - minV) * position);
        }
    }
}
