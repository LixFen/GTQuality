package com.plainston.gtquality.integration.ae2;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import gregapi.data.CS;

class PowderAmountsTest {

    @Test
    void mixedFractionsOnlyProduceWholeDustAndSurviveReload() {
        PowderAmounts storage = new PowderAmounts();
        assertEquals(3, storage.insert("Iron", CS.U / 4, 3, false));
        assertEquals(0, storage.whole("Iron"));
        assertEquals(1, storage.insert("Iron", CS.U / 4, 1, false));
        assertEquals(10, storage.insert("Iron", CS.U / 9, 10, false));
        assertEquals(2, storage.whole("Iron"));
        assertEquals(2, storage.extract("Iron", 64, false));
        assertEquals(0, storage.whole("Iron"));
        assertEquals(CS.U / 9, storage.materials.get("Iron"));

        PowderAmounts reloaded = new PowderAmounts();
        reloaded.read(storage.write());
        assertEquals(storage.materials, reloaded.materials);
        reloaded.insert("Iron", CS.U / 72, 64, false);
        assertEquals(1, reloaded.extract("Iron", 64, false));
        assertFalse(reloaded.materials.containsKey("Iron"));
    }

    @Test
    void countsMaterialsRatherThanPowderFormsAndKeepsRemainderOnlySlots() {
        PowderAmounts storage = new PowderAmounts();
        for (int i = 0; i < 64; i++) assertEquals(1, storage.insert("Material" + i, CS.U / 72, 1, false));
        assertEquals(0, storage.insert("New", CS.U, 1, false));
        assertEquals(71, storage.insert("Material0", CS.U / 72, 71, false));
        assertEquals(1, storage.extract("Material0", 1, false));
        assertEquals(1, storage.insert("New", CS.U * 9, 1, false));
        assertEquals(9, storage.whole("New"));
        assertEquals(64, storage.materials.size());
    }

    @Test
    void capacityReturnsPartialAcceptanceWithoutOverflow() {
        PowderAmounts storage = new PowderAmounts();
        assertEquals(PowderAmounts.MAX_DUST, storage.insert("Iron", CS.U, Long.MAX_VALUE, false));
        assertEquals(0, storage.insert("Iron", CS.U, 1, false));
        assertEquals(71, storage.insert("Iron", CS.U / 72, Long.MAX_VALUE, false));
        assertEquals(0, storage.insert("Iron", CS.U / 72, 1, false));
        assertEquals(PowderAmounts.MAX_DUST, storage.extract("Iron", Long.MAX_VALUE, false));
        assertEquals(71 * (CS.U / 72), storage.materials.get("Iron"));
    }

    @Test
    void simulationsDoNotConsumeCapacityOrChangeContents() {
        PowderAmounts storage = new PowderAmounts();
        assertEquals(9, storage.insert("Iron", CS.U, 9, true));
        assertTrue(storage.materials.isEmpty());
        storage.insert("Iron", CS.U, 9, false);
        assertEquals(9, storage.extract("Iron", 64, true));
        assertEquals(9, storage.whole("Iron"));
        assertEquals(0, storage.extract("Other", 64, false));
        assertEquals(0, storage.insert("Other", CS.U, -1, false));
    }
}
