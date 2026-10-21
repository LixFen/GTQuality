package com.plainston.gtquality.integration.ae2;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.junit.jupiter.api.Test;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;

class PowderCellChangesTest {

    @Test
    void remainderOnlyInputDoesNotPublishPhantomSmallDust() {
        Object monitor = new Object();
        PowderCellChanges.begin(monitor);
        try {
            IAEItemStack input = stack("smallIron", 3);
            PowderCellChanges.converted(input, 3, stack("iron", 0));
            assertTrue(
                PowderCellChanges.corrected(monitor, input, null)
                    .isEmpty());
        } finally {
            PowderCellChanges.end(monitor);
        }
    }

    @Test
    void mixedDestinationsKeepUnconvertedItemsAndReportNewWholeDust() {
        Object monitor = new Object();
        PowderCellChanges.begin(monitor);
        try {
            IAEItemStack input = stack("smallIron", 10);
            PowderCellChanges.converted(input, 4, stack("iron", 1));
            // 4 items converted, 4 stored in an ordinary cell, 2 rejected.
            List<IAEStack> changes = PowderCellChanges.corrected(monitor, input, stack("smallIron", 2));
            assertEquals(2, changes.size());
            assertEquals(
                4,
                changes.get(0)
                    .getStackSize());
            assertEquals(
                1,
                changes.get(1)
                    .getStackSize());
            assertEquals(10, input.getStackSize());
        } finally {
            PowderCellChanges.end(monitor);
        }
    }

    @Test
    void nestedChestAndNetworkMonitorsBothReceiveTheConversion() {
        Object network = new Object(), chest = new Object();
        IAEItemStack input = stack("tinyIron", 9);
        PowderCellChanges.begin(network);
        try {
            PowderCellChanges.begin(chest);
            try {
                PowderCellChanges.converted(input, 9, stack("iron", 1));
                List<IAEStack> changes = PowderCellChanges.corrected(chest, input, null);
                assertEquals(1, changes.size());
                assertEquals(stack("iron", 1), changes.get(0));
            } finally {
                PowderCellChanges.end(chest);
            }
            assertEquals(
                1,
                PowderCellChanges.corrected(network, input, null)
                    .size());
        } finally {
            PowderCellChanges.end(network);
        }
        PowderCellChanges.begin(network);
        try {
            assertNull(PowderCellChanges.corrected(network, input, null));
        } finally {
            PowderCellChanges.end(network);
        }
    }

    @Test
    void ordinaryDustInputLeavesDefaultNotificationsAlone() {
        Object monitor = new Object();
        PowderCellChanges.begin(monitor);
        try {
            IAEItemStack input = stack("iron", 8);
            PowderCellChanges.converted(input, 8, stack("iron", 8));
            assertNull(PowderCellChanges.corrected(monitor, input, null));
        } finally {
            PowderCellChanges.end(monitor);
        }
    }

    private static IAEItemStack stack(String type, long count) {
        return (IAEItemStack) Proxy.newProxyInstance(
            PowderCellChangesTest.class.getClassLoader(),
            new Class<?>[] { IAEItemStack.class },
            new Stack(type, count));
    }

    private static final class Stack implements InvocationHandler {

        final String type;
        long count;

        Stack(String type, long count) {
            this.type = type;
            this.count = count;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "copy":
                    return stack(type, count);
                case "getStackSize":
                    return count;
                case "setStackSize":
                    count = (long) args[0];
                    return proxy;
                case "incStackSize":
                    count += (long) args[0];
                    return null;
                case "equals":
                    return args[0] != null && Proxy.isProxyClass(args[0].getClass())
                        && type.equals(((Stack) Proxy.getInvocationHandler(args[0])).type);
                case "hashCode":
                    return type.hashCode();
                case "toString":
                    return type + ":" + count;
                default:
                    throw new UnsupportedOperationException(method.getName());
            }
        }
    }
}
