package com.plainston.gtquality.integration.blockrenderer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

class PreviewCaptureTest {

    @Test
    void onlyTheSelectedControllerOnTheCapturingThreadIsIntercepted() {
        Object controller = new Object();
        try (PreviewCapture ignored = new PreviewCapture(controller, (x, y, z, id, registry, design, mode) -> true)) {
            assertEquals(Boolean.TRUE, visit(controller));
            assertNull(visit(new Object()));
            assertNull(
                CompletableFuture.supplyAsync(() -> visit(controller))
                    .join());
        }
        assertNull(visit(controller));
    }

    @Test
    void exceptionClearsCaptureAndNestedCaptureDoesNotReplaceOwner() {
        Object controller = new Object();
        assertThrows(IllegalArgumentException.class, () -> {
            try (PreviewCapture ignored = new PreviewCapture(
                controller,
                (x, y, z, id, registry, design, mode) -> false)) {
                assertThrows(
                    IllegalStateException.class,
                    () -> new PreviewCapture(new Object(), (x, y, z, id, registry, design, mode) -> true));
                assertEquals(Boolean.FALSE, visit(controller));
                throw new IllegalArgumentException("Test failure");
            }
        });
        assertNull(visit(controller));
    }

    private static Boolean visit(Object controller) {
        return PreviewCapture.visit(controller, 0, 64, 0, 18000, 1, 0, 0);
    }
}
