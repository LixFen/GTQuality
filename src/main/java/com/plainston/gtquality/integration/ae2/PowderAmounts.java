package com.plainston.gtquality.integration.ae2;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import gregapi.data.CS;

/** Material units, including fractions which are never exposed as extractable items. */
final class PowderAmounts {

    static final int MAX_TYPES = 63;
    static final long MAX_DUST = 1_000_000;
    static final long MAX_UNITS = MAX_DUST * CS.U + CS.U - 1;
    final Map<String, Long> materials = new LinkedHashMap<>();

    long insert(String material, long unitsPerItem, long count, boolean simulate) {
        if (unitsPerItem <= 0 || count <= 0) return 0;
        long current = materials.getOrDefault(material, 0L);
        if (current == 0 && materials.size() >= MAX_TYPES) return 0;
        // Divide before multiplying: AE can offer a Long.MAX_VALUE-sized stack.
        long accepted = Math.min(count, Math.max(0, MAX_UNITS - current) / unitsPerItem);
        if (!simulate && accepted > 0) materials.put(material, current + accepted * unitsPerItem);
        return accepted;
    }

    long whole(String material) {
        return whole(material, CS.U);
    }

    long whole(String material, long unitsPerItem) {
        return materials.getOrDefault(material, 0L) / unitsPerItem;
    }

    long extract(String material, long count, boolean simulate) {
        return extract(material, CS.U, count, simulate);
    }

    long extract(String material, long unitsPerItem, long count, boolean simulate) {
        long extracted = Math.min(Math.max(0, count), whole(material, unitsPerItem));
        if (!simulate && extracted > 0) {
            long remaining = materials.get(material) - extracted * unitsPerItem;
            if (remaining == 0) materials.remove(material);
            else materials.put(material, remaining);
        }
        return extracted;
    }

    NBTTagList write() {
        NBTTagList list = new NBTTagList();
        materials.forEach((material, units) -> {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("material", material);
            entry.setLong("units", units);
            list.appendTag(entry);
        });
        return list;
    }

    void read(NBTTagList list) {
        materials.clear();
        // Old cells could contain 64 materials; keep them extractable after lowering the limit.
        for (int i = 0; i < list.tagCount() && materials.size() < 64; i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            String material = entry.getString("material");
            long units = entry.getLong("units");
            // Preserve inventories saved under the previous, larger capacity; only new input is limited.
            if (!material.isEmpty() && units > 0 && units <= Integer.MAX_VALUE * CS.U + CS.U - 1) {
                materials.put(material, units);
            }
        }
    }
}
