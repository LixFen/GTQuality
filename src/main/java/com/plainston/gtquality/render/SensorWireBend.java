package com.plainston.gtquality.render;

/** Geometry in wire-local coordinates; Minecraft sides are Y-, Y+, Z-, Z+, X-, X+. */
public final class SensorWireBend {

    private SensorWireBend() {}

    public static boolean isLateral(byte wireSide, byte facing) {
        return wireSide / 2 != facing / 2;
    }

    public static byte hubSide(double[] diameters) {
        byte owner = -1;
        for (byte side = 0; side < 6; side++) {
            if (diameters[side] > 0 && (owner < 0 || diameters[side] > diameters[owner])) owner = side;
        }
        return owner;
    }

    static int[] frontOffset(byte wireSide, byte facing) {
        int[] offset = sensorOffset(wireSide);
        offset[axis(facing)] += sign(facing);
        return offset;
    }

    static int[] sensorOffset(byte wireSide) {
        int[] offset = new int[3];
        offset[axis(wireSide)] = sign(wireSide);
        return offset;
    }

    public static double[] bounds(byte wireSide, byte facing, double diameter, double hubDiameter, boolean elbow) {
        int incomingAxis = axis(wireSide), panelAxis = axis(facing);
        double incomingSign = sign(wireSide), panelSign = sign(facing), radius = diameter / 2;
        double[] center = { .5, .5, .5 };
        center[incomingAxis] += incomingSign;
        double boxRadius = elbow ? hubDiameter / 2 : radius;
        double[] box = { center[0] - boxRadius, center[1] - boxRadius, center[2] - boxRadius, center[0] + boxRadius,
            center[1] + boxRadius, center[2] + boxRadius };
        if (elbow) {
            // The panel occupies the last 2/16 of its block, opposite its display facing.
            double surface = center[panelAxis] - panelSign * .375;
            box[panelAxis] = Math.min(surface, center[panelAxis] - boxRadius);
            box[panelAxis + 3] = Math.max(surface, center[panelAxis] + boxRadius);
        } else {
            // Stop at the elbow's side, avoiding overlapping coplanar faces at the corner.
            double boundary = .5 + incomingSign * .5;
            double corner = center[incomingAxis] - incomingSign * hubDiameter / 2;
            box[incomingAxis] = Math.min(boundary, corner);
            box[incomingAxis + 3] = Math.max(boundary, corner);
        }
        return box;
    }

    private static int axis(byte side) {
        return side < 2 ? 1 : side < 4 ? 2 : 0;
    }

    private static int sign(byte side) {
        return side % 2 == 0 ? -1 : 1;
    }
}
