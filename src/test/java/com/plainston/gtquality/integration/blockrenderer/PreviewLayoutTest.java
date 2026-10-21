package com.plainston.gtquality.integration.blockrenderer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class PreviewLayoutTest {

    @Test
    void fusionCoreContainsThreeVersatileTwelveLogicAndTwelveControlProcessors() {
        PreviewLayout layout = new PreviewLayout(PreviewLayout.Kind.FUSION, 0, 64, 0, 1);
        for (int x = -2; x <= 2; x++) for (int y = -2; y <= 2; y++) for (int z = -2; z <= 2; z++) {
            if (x * x + y * y + z * z >= 4) continue;
            boolean accepted = false;
            for (int id = 18200; id <= 18202 && !accepted; id++) {
                accepted = layout.visit(x, y + 64, z + 3, id, 1, 0, 0);
            }
            assertTrue(accepted);
        }
        Map<Integer, Long> counts = layout.parts()
            .stream()
            .collect(Collectors.groupingBy(PreviewLayout.Part::id, Collectors.counting()));
        assertEquals(3L, counts.get(18200));
        assertEquals(12L, counts.get(18201));
        assertEquals(12L, counts.get(18202));
    }

    @Test
    void matterProcessorChecksDoNotOverwritePreviouslySelectedAlternatives() {
        PreviewLayout layout = new PreviewLayout(PreviewLayout.Kind.MATTER, 0, 64, 0, 1);
        for (int x = 0; x < 8; x++) assertEquals(x < 4, layout.visit(x, 69, 1, 18202, 1, 0, 0));
        for (int x = 0; x < 8; x++) assertEquals(x >= 4, layout.visit(x, 69, 1, 18204, 1, 0, 0));
        assertEquals(
            8,
            layout.parts()
                .size());
    }

    @Test
    void lightningProbeTerminatesAfterRequestedHeight() {
        PreviewLayout layout = new PreviewLayout(PreviewLayout.Kind.LIGHTNING, 0, 64, 0, 3);
        for (int i = 0; i < 3; i++) assertTrue(layout.visit(0, 69 + i, 0, 18104, 1, 0, 0));
        assertFalse(layout.visit(0, 72, 0, 18104, 1, 0, 0));
        assertEquals(
            3,
            layout.parts()
                .size());
    }

    @Test
    void controllerIsNotCountedAsCasingAndCrucibleSecondPassUpdatesDesign() {
        PreviewLayout layout = new PreviewLayout(PreviewLayout.Kind.STANDARD, 0, 64, 0, 1);
        assertTrue(layout.visit(0, 64, 0, 18000, 1, 0, 0));
        assertTrue(
            layout.parts()
                .isEmpty());
        assertTrue(layout.visit(1, 64, 0, 18000, 1, 0, 7));
        assertTrue(layout.visit(1, 64, 0, 18000, 1, 4, 7));
        assertEquals(
            1,
            layout.parts()
                .size());
        assertEquals(
            4,
            layout.parts()
                .iterator()
                .next()
                .design());
    }
}
