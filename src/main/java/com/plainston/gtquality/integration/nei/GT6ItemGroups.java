package com.plainston.gtquality.integration.nei;

import static gregapi.data.CS.NBT_RECIPEMAP;
import static gregapi.data.CS.NBT_TEXTURE;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

import com.plainston.gtquality.GTQuality;

import codechicken.nei.ClientHandler;
import codechicken.nei.NEIClientConfig;
import gregapi.block.multitileentity.MultiTileEntityClassContainer;
import gregapi.block.multitileentity.MultiTileEntityRegistry;
import gregapi.block.prefixblock.PrefixBlock;
import gregapi.item.prefixitem.PrefixItem;

/** Supplies automatic groups through NEI's supported collapsibleitems.cfg format. */
public final class GT6ItemGroups {

    private static final String BEGIN = "# BEGIN GTQuality automatic item groups";
    private static final String END = "# END GTQuality automatic item groups";

    private GT6ItemGroups() {}

    public static void register() {
        boolean enabled = GTQuality.neiItemGroups && NEIClientConfig.enableCollapsibleItems();
        Path file = NEIClientConfig.configDir.toPath()
            .resolve("collapsibleitems.cfg");
        // Let NEI create its normal defaults before appending our rules.
        if (enabled && !Files.exists(file)) ClientHandler.loadSettingsFile("collapsibleitems.cfg", lines -> {});
        try {
            updateConfig(file, enabled ? filters() : new ArrayList<>());
        } catch (IOException exception) {
            NEIClientConfig.logger.error("Could not update GTQuality's NEI item groups", exception);
        }
    }

    static void updateConfig(Path file, List<String> filters) throws IOException {
        // Match NEI's FileReader/FileWriter encoding, preserving user-written rules and comments.
        Charset charset = Charset.defaultCharset();
        List<String> original = Files.exists(file) ? Files.readAllLines(file, charset) : new ArrayList<>();
        List<String> updated = new ArrayList<>();
        boolean inside = false;
        for (String line : original) {
            if (BEGIN.equals(line)) {
                if (inside) throw new IOException("Nested GTQuality item group markers in " + file);
                inside = true;
            } else if (END.equals(line)) {
                if (!inside) throw new IOException("Unmatched GTQuality item group marker in " + file);
                inside = false;
            } else if (!inside) {
                // NEI ships this fluid-only rule before our section. Extend it so the two display items
                // form one group, and restore NEI's rule when our integration is disabled.
                if (line.equals("neifluiddisplay") && filters.contains(GT6ItemGroupRules.FLUIDS)) {
                    updated.add(GT6ItemGroupRules.FLUIDS);
                } else if (line.equals(GT6ItemGroupRules.FLUIDS) && filters.isEmpty()) {
                    updated.add("neifluiddisplay");
                } else {
                    updated.add(line);
                }
            }
        }
        if (inside) throw new IOException("Unclosed GTQuality item group marker in " + file);
        // First matching group wins in NEI, so user rules must precede automatic rules.
        if (!filters.isEmpty()) {
            updated.add(BEGIN);
            updated.addAll(filters);
            updated.add(END);
        }
        if (!original.equals(updated)) {
            Path temporary = Files.createTempFile(file.getParent(), "gtquality-groups-", ".tmp");
            try {
                Files.write(temporary, updated, charset);
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temporary);
            }
        }
    }

    public static List<String> filters() {
        List<String> filters = GT6ItemGroupRules.filters();
        MultiTileEntityRegistry registry = MultiTileEntityRegistry.getRegistry("gt.multitileentity");
        if (registry != null) {
            Map<String, TreeSet<Integer>> groups = new TreeMap<>();
            for (MultiTileEntityClassContainer entry : registry.mRegistrations) {
                if (entry.mHidden) continue;
                groups.computeIfAbsent(machineKey(entry.mClass, entry.mParameters), key -> new TreeSet<>())
                    .add((int) entry.mID);
            }
            String itemName = Item.itemRegistry.getNameForObject(Item.getItemFromBlock(registry.mBlock));
            for (TreeSet<Integer> ids : groups.values()) {
                // A single registered ID can still have many NBT variants (coins, shaped molds).
                filters.add(machineFilter(itemName, ids));
            }
        }

        // Each prefix item/block represents one shape (ingot, plate, storage block, ore host, etc.).
        // Keep separate registry entries separate, especially ores in different host rocks.
        TreeSet<String> materialItems = new TreeSet<>();
        for (Object value : Item.itemRegistry) {
            Item item = (Item) value;
            String name = Item.itemRegistry.getNameForObject(item);
            if (name.startsWith("gregtech:")
                && (item instanceof PrefixItem || Block.getBlockFromItem(item) instanceof PrefixBlock)) {
                materialItems.add("<" + name + ":32767>");
            }
        }
        filters.addAll(materialItems);
        return filters;
    }

    static String machineKey(Class<?> type, NBTTagCompound parameters) {
        if (type.getName()
            .startsWith("gregtech.tileentity.sensors.")) return "sensors";
        // Generic machine and multiblock-part classes serve multiple unrelated functions.
        // Numeric textures select wood/material variants; string textures identify the function.
        return type.getName() + "|"
            + parameters.getString(NBT_RECIPEMAP)
            + "|"
            + (parameters.hasKey(NBT_TEXTURE, 8) ? parameters.getString(NBT_TEXTURE) : "");
    }

    static String machineFilter(String itemName, Iterable<Integer> ids) {
        TreeSet<Integer> sorted = new TreeSet<>();
        ids.forEach(sorted::add);
        // Stable ordering also keeps NEI's filter-derived group UUID stable between loads.
        return sorted.stream()
            .map(id -> "<" + itemName + ":" + id + ">")
            .collect(Collectors.joining(" | "));
    }
}
