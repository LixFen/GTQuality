package com.plainston.gtquality.integration.blockrenderer;

/** Only the exact controller being inspected on this thread may supply preview cells. No renderer dependency here. */
public final class PreviewCapture implements AutoCloseable {

    private static final ThreadLocal<PreviewCapture> ACTIVE = new ThreadLocal<>();
    private final Object controller;
    private final PartVisitor visitor;

    public PreviewCapture(Object controller, PartVisitor visitor) {
        if (ACTIVE.get() != null) throw new IllegalStateException("Nested multiblock preview capture");
        this.controller = controller;
        this.visitor = visitor;
        ACTIVE.set(this);
    }

    public static Boolean visit(Object controller, int x, int y, int z, int id, int registry, int design, int mode) {
        PreviewCapture capture = ACTIVE.get();
        return capture != null && capture.controller == controller
            ? capture.visitor.visit(x, y, z, id, registry, design, mode)
            : null;
    }

    @Override
    public void close() {
        ACTIVE.remove();
    }

    @FunctionalInterface
    public interface PartVisitor {

        boolean visit(int x, int y, int z, int id, int registry, int design, int mode);
    }
}
