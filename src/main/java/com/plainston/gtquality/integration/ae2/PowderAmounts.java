package com.plainston.gtquality.integration.ae2;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import gregapi.data.CS;

/** Material units, including fractions which are never exposed as extractable items. */
final class PowderAmounts {

    static final int MAX_TYPES = 64;
    static final long MAX_DUST = Integer.MAX_VALUE;
    static final long MAX_UNITS = MAX_DUST * CS.U + CS.U - 1;
    final Map<String, Long> materials = new LinkedHashMap<>();

    long insert(String material, long unitsPerItem, long count, boolean simulate) {
        if (unitsPerItem <= 0 || count <= 0) return 0;
        long current = materials.getOrDefault(material, 0L);
        if (current == 0 && materials.size() >= MAX_TYPES) return 0;
        // Divide before multiplying: AE can offer a Long.MAX_VALUE-sized stack.
        long accepted = Math.min(count, (MAX_UNITS - current) / unitsPerItem);
        if (!simulate && accepted > 0) materials.put(material, current + accepted * unitsPerItem);
        return accepted;
    }

    long whole(String material) {
        return materials.getOrDefault(material, 0L) / CS.U;
    }

    long extract(String material, long count, boolean simulate) {
        long extracted = Math.min(Math.max(0, count), whole(material));
        if (!simulate && extracted > 0) {
            long remaining = materials.get(material) - extracted * CS.U;
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
        for (int i = 0; i < list.tagCount() && materials.size() < MAX_TYPES; i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            String material = entry.getString("material");
            long units = entry.getLong("units");
            if (!material.isEmpty() && units > 0 && units <= MAX_UNITS) materials.put(material, units);
        }
    }
}
