package com.plainston.gtquality.integration.ae2;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

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
        for (int i = 0; i < 63; i++) assertEquals(1, storage.insert("Material" + i, CS.U / 72, 1, false));
        assertEquals(0, storage.insert("New", CS.U, 1, false));
        assertEquals(71, storage.insert("Material0", CS.U / 72, 71, false));
        assertEquals(1, storage.extract("Material0", 1, false));
        assertEquals(1, storage.insert("New", CS.U * 9, 1, false));
        assertEquals(9, storage.whole("New"));
        assertEquals(63, storage.materials.size());
    }

    @Test
    void legacySixtyFourMaterialsSurviveReloadButNewMaterialsWaitForSpace() {
        NBTTagList saved = new NBTTagList();
        for (int i = 0; i < 64; i++) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("material", "Material" + i);
            entry.setLong("units", CS.U);
            saved.appendTag(entry);
        }
        PowderAmounts storage = new PowderAmounts();
        storage.read(saved);
        assertEquals(64, storage.materials.size());
        assertEquals(0, storage.insert("New", CS.U, 1, false));
        assertEquals(1, storage.insert("Material0", CS.U, 1, false));
        storage.extract("Material0", 2, false);
        assertEquals(0, storage.insert("New", CS.U, 1, false));
        storage.extract("Material1", 1, false);
        assertEquals(1, storage.insert("New", CS.U, 1, false));
        assertEquals(63, storage.materials.size());
    }

    @Test
    void capacityReturnsPartialAcceptanceWithoutOverflow() {
        PowderAmounts storage = new PowderAmounts();
        assertEquals(1_000_000, PowderAmounts.MAX_DUST);
        assertEquals(PowderAmounts.MAX_DUST, storage.insert("Iron", CS.U, Long.MAX_VALUE, false));
        assertEquals(0, storage.insert("Iron", CS.U, 1, false));
        assertEquals(71, storage.insert("Iron", CS.U / 72, Long.MAX_VALUE, false));
        assertEquals(0, storage.insert("Iron", CS.U / 72, 1, false));
        assertEquals(PowderAmounts.MAX_DUST, storage.extract("Iron", Long.MAX_VALUE, false));
        assertEquals(71 * (CS.U / 72), storage.materials.get("Iron"));
    }

    @Test
    void loweringCapacityPreservesLegacyContentsAndRejectsFurtherInputUntilSpaceIsAvailable() {
        NBTTagCompound entry = new NBTTagCompound();
        entry.setString("material", "Iron");
        entry.setLong("units", 2_000_000 * CS.U);
        NBTTagList saved = new NBTTagList();
        saved.appendTag(entry);
        PowderAmounts storage = new PowderAmounts();
        storage.read(saved);
        assertEquals(2_000_000, storage.whole("Iron"));
        assertEquals(0, storage.insert("Iron", CS.U, 1, false));
        assertEquals(0, storage.insert("Iron", CS.U72, 1, true));
        assertEquals(saved, storage.write());
        assertEquals(1_000_001, storage.extract("Iron", 1_000_001, false));
        assertEquals(1, storage.insert("Iron", CS.U, 2, false));
        assertEquals(1_000_000, storage.whole("Iron"));
    }

    @Test
    void changingOutputFormsReinterpretsStoredUnitsWithoutChangingMaterialOrCapacity() {
        PowderAmounts storage = new PowderAmounts();
        storage.insert("Iron", CS.U, 2, false);
        storage.insert("Iron", CS.U / 9, 1, false);
        storage.insert("Copper", CS.U, 3, false);
        long originalUnits = storage.materials.get("Iron");
        assertEquals(8, storage.whole("Iron", CS.U / 4));
        assertEquals(19, storage.whole("Iron", CS.U / 9));
        assertEquals(0, storage.whole("Iron", CS.U * 9));
        assertEquals(2, storage.whole("Iron"));
        assertEquals(originalUnits, storage.materials.get("Iron"));
        assertEquals(3, storage.whole("Copper"));

        assertEquals(8, storage.extract("Iron", CS.U / 4, Long.MAX_VALUE, true));
        assertEquals(originalUnits, storage.materials.get("Iron"));
        assertEquals(8, storage.extract("Iron", CS.U / 4, Long.MAX_VALUE, false));
        assertEquals(CS.U / 9, storage.materials.get("Iron"));
        assertEquals(1, storage.extract("Iron", CS.U / 9, 1, false));
        assertFalse(storage.materials.containsKey("Iron"));
    }

    @Test
    void selectedBlockFormKeepsPartialBlocksHiddenAndSmallFormsSupportLongCounts() {
        PowderAmounts storage = new PowderAmounts();
        storage.insert("Iron", CS.U, 8, false);
        assertEquals(0, storage.extract("Iron", CS.U * 9, 1, false));
        storage.insert("Iron", CS.U, 1, false);
        assertEquals(1, storage.extract("Iron", CS.U * 9, Long.MAX_VALUE, false));
        assertTrue(storage.materials.isEmpty());
        storage.insert("Iron", CS.U, PowderAmounts.MAX_DUST, false);
        assertEquals(PowderAmounts.MAX_DUST * 72, storage.whole("Iron", CS.U72));
        assertEquals(PowderAmounts.MAX_DUST * 72, storage.extract("Iron", CS.U72, Long.MAX_VALUE, false));
        assertTrue(storage.materials.isEmpty());
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
