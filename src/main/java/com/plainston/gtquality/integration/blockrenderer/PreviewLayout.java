package com.plainston.gtquality.integration.blockrenderer;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.bsideup.jabel.Desugar;

/** Select one valid example where GT6 accepts alternative parts at the same position. */
final class PreviewLayout implements PreviewCapture.PartVisitor {

    enum Kind {
        STANDARD,
        FUSION,
        MATTER,
        LIGHTNING
    }

    @Desugar
    record Position(int x, int y, int z) {}

    @Desugar
    record Part(Position position, int id, int registry, int design, int mode) {}

    private final Map<Position, Part> parts = new LinkedHashMap<>();
    private final Kind kind;
    private final Position controller;
    private final int rodHeight;

    PreviewLayout(Kind kind, int x, int y, int z, int rodHeight) {
        this.kind = kind;
        controller = new Position(x, y, z);
        this.rodHeight = rodHeight;
    }

    @Override
    public boolean visit(int x, int y, int z, int id, int registry, int design, int mode) {
        Position position = new Position(x, y, z);
        if (controller.equals(position)) return true;
        Part old = parts.get(position);
        if (old != null && (old.id != id || old.registry != registry)) return false;
        if (old == null) {
            if (kind == Kind.FUSION && ((id == 18200 && count(id) >= 3) || (id == 18201 && count(id) >= 12)))
                return false;
            if (kind == Kind.MATTER && id == 18202 && count(id) >= 4) return false;
            // GT6 probes upward until a rod is missing. Always terminate this loop in a preview.
            if (kind == Kind.LIGHTNING && id == 18104 && y >= controller.y + 5 + rodHeight) return false;
        }
        parts.put(position, new Part(position, id, registry, design, mode));
        return true;
    }

    private long count(int id) {
        return parts.values()
            .stream()
            .filter(part -> part.id == id)
            .count();
    }

    Collection<Part> parts() {
        return parts.values();
    }
}
