package com.plainston.gtquality.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class SensorWireBendTest {

    @Test
    void everyLateralConnectionMeetsTheWireElbowAndPanel() {
        for (byte facing = 0; facing < 6; facing++) {
            for (byte side = 0; side < 6; side++) {
                if (side / 2 == facing / 2) {
                    assertFalse(SensorWireBend.isLateral(side, facing));
                    continue;
                }
                assertTrue(SensorWireBend.isLateral(side, facing));
                for (double diameter : new double[] { .125, .25, .5 }) {
                    double[] straight = SensorWireBend.bounds(side, facing, diameter, diameter, false);
                    double[] elbow = SensorWireBend.bounds(side, facing, diameter, diameter, true);
                    int incoming = axis(side), panel = axis(facing);
                    boolean positive = side % 2 != 0, frontPositive = facing % 2 != 0;
                    // Entry is exactly on the shared block boundary, on the wire's centre line.
                    assertEquals(positive ? 1 : 0, straight[incoming + (positive ? 0 : 3)]);
                    for (int axis = 0; axis < 3; axis++) {
                        assertTrue(straight[axis] < straight[axis + 3]);
                        assertTrue(elbow[axis] < elbow[axis + 3]);
                        if (axis != incoming) {
                            assertEquals(.5 - diameter / 2, straight[axis]);
                            assertEquals(.5 + diameter / 2, straight[axis + 3]);
                        }
                    }
                    // The two segments touch without overlapping volume.
                    assertEquals(straight[incoming + (positive ? 3 : 0)], elbow[incoming + (positive ? 0 : 3)]);
                    // The bend ends on the exposed face of the 2/16-thick sensor panel.
                    assertEquals(frontPositive ? .125 : .875, elbow[panel + (frontPositive ? 0 : 3)]);
                    assertEquals(.5 + (positive ? 1 : -1), (elbow[incoming] + elbow[incoming + 3]) / 2);
                }
            }
        }
    }

    @Test
    void allFrontAndSideCombinationsHaveOneTrunkAndNoOverlappingSurfaces() {
        for (byte facing = 0; facing < 6; facing++) {
            for (int mask = 1; mask < 64; mask++) {
                if ((mask & (1 << (facing ^ 1))) != 0) continue;
                for (int pattern = 0; pattern < 3; pattern++) {
                    double[] diameters = new double[6];
                    for (byte side = 0; side < 6; side++) {
                        if ((mask & (1 << side)) != 0) {
                            diameters[side] = pattern == 0 ? .125 : .125 * (1 + (side + pattern) % 4);
                        }
                    }
                    byte owner = SensorWireBend.hubSide(diameters);
                    double hubDiameter = diameters[owner];
                    List<double[]> boxes = new ArrayList<>();
                    List<Integer> hiddenAxes = new ArrayList<>();
                    for (byte side = 0; side < 6; side++) {
                        if (diameters[side] == 0) continue;
                        assertTrue(hubDiameter >= diameters[side]);
                        byte wireSide = (byte) (side ^ 1);
                        boxes.add(sensorBounds(wireSide, facing, diameters[side], hubDiameter, false));
                        hiddenAxes.add(axis(side));
                    }
                    boxes.add(sensorBounds((byte) (owner ^ 1), facing, hubDiameter, hubDiameter, true));
                    hiddenAxes.add(-1);
                    for (int a = 0; a < boxes.size(); a++) {
                        for (int b = a + 1; b < boxes.size(); b++) {
                            double[] first = boxes.get(a), second = boxes.get(b);
                            boolean volumeOverlap = true;
                            for (int axis = 0; axis < 3; axis++) {
                                volumeOverlap &= overlaps(first, second, axis);
                            }
                            assertFalse(volumeOverlap, "branches must stop at the shared trunk");
                            for (byte face = 0; face < 6; face++) {
                                int normal = axis(face);
                                if (normal == hiddenAxes.get(a) || normal == hiddenAxes.get(b)) continue;
                                int plane = normal + (face % 2 == 0 ? 0 : 3);
                                if (first[plane] != second[plane]) continue;
                                boolean surfaceOverlap = true;
                                for (int axis = 0; axis < 3; axis++) {
                                    if (axis != normal) surfaceOverlap &= overlaps(first, second, axis);
                                }
                                assertFalse(surfaceOverlap, "visible coplanar faces would cause z-fighting");
                            }
                        }
                    }
                    double[] trunk = boxes.get(boxes.size() - 1);
                    assertEquals(facing % 2 == 0 ? .875 : .125, trunk[axis(facing) + (facing % 2 == 0 ? 3 : 0)]);
                }
            }
        }
    }

    private static double[] sensorBounds(byte wireSide, byte facing, double diameter, double hubDiameter,
        boolean elbow) {
        double[] box = SensorWireBend.bounds(wireSide, facing, diameter, hubDiameter, elbow);
        double offset = wireSide % 2 == 0 ? -1 : 1;
        box[axis(wireSide)] -= offset;
        box[axis(wireSide) + 3] -= offset;
        return box;
    }

    private static boolean overlaps(double[] a, double[] b, int axis) {
        return Math.min(a[axis + 3], b[axis + 3]) > Math.max(a[axis], b[axis]);
    }

    private static int axis(byte side) {
        return side < 2 ? 1 : side < 4 ? 2 : 0;
    }
}
