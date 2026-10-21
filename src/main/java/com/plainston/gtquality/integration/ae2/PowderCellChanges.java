package com.plainston.gtquality.integration.ae2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;

/** Corrections to AE's assumption that accepted input appears unchanged in storage. */
public final class PowderCellChanges {

    private static final ThreadLocal<Deque<Frame>> FRAMES = ThreadLocal.withInitial(ArrayDeque::new);

    private PowderCellChanges() {}

    public static void begin(Object monitor) {
        FRAMES.get()
            .push(new Frame(monitor));
    }

    public static void end(Object monitor) {
        Deque<Frame> frames = FRAMES.get();
        if (!frames.isEmpty() && frames.peek().monitor == monitor) frames.pop();
        if (frames.isEmpty()) FRAMES.remove();
    }

    static void converted(IAEItemStack input, long accepted, IAEItemStack output) {
        if (input.equals(output) && accepted == output.getStackSize()) return;
        for (Frame frame : FRAMES.get()) {
            frame.corrections.add(
                input.copy()
                    .setStackSize(-accepted));
            if (output.getStackSize() > 0) frame.corrections.add(output.copy());
        }
    }

    public static List<IAEStack> corrected(Object monitor, IAEStack original, IAEStack leftovers) {
        Frame frame = FRAMES.get()
            .peek();
        if (frame == null || frame.monitor != monitor || frame.corrections.isEmpty()) return null;
        List<IAEStack> changes = new ArrayList<>();
        changes.add(
            original.copy()
                .setStackSize(original.getStackSize() - (leftovers == null ? 0 : leftovers.getStackSize())));
        for (IAEStack correction : frame.corrections) {
            IAEStack matching = null;
            for (IAEStack change : changes) {
                if (change.equals(correction)) {
                    matching = change;
                    break;
                }
            }
            if (matching == null) changes.add(correction.copy());
            else matching.incStackSize(correction.getStackSize());
        }
        changes.removeIf(change -> change.getStackSize() == 0);
        return changes;
    }

    private static final class Frame {

        final Object monitor;
        final List<IAEStack> corrections = new ArrayList<>();

        Frame(Object monitor) {
            this.monitor = monitor;
        }
    }
}
