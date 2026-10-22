package com.plainston.gtquality.integration.nei;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import gregapi.block.metatype.BlockMetaType;
import gregapi.block.metatype.BlockStones;
import gregapi.item.multiitem.MultiItemRandom;

/** GT6's registered block families and stable metadata ranges from its MultiItem registrations. */
final class GT6ItemGroupRules {

    static final String FLUIDS = "neifluiddisplay | <gregapi:gt.display.fluid:32767>";

    private GT6ItemGroupRules() {}

    static List<String> filters() {
        Map<String, TreeSet<String>> groups = new TreeMap<>();
        for (Object value : Item.itemRegistry) {
            Item item = (Item) value;
            String name = Item.itemRegistry.getNameForObject(item);
            if (!name.startsWith("gregtech:")) continue;
            Block block = Block.getBlockFromItem(item);
            String family;
            if (block instanceof BlockMetaType meta && meta.mIsSlab) {
                family = "slabs";
            } else if (block instanceof BlockStones stone) {
                family = "stone:" + stone.mMaterial.mNameInternal;
            } else {
                family = blockFamily(name);
            }
            if (family != null) {
                add(groups, family, "<" + name + ":32767>");
            } else if (item instanceof MultiItemRandom multi) {
                Map<String, TreeSet<Integer>> variants = new TreeMap<>();
                for (int id = multi.mEnabledItems.nextSetBit(0); id >= 0; id = multi.mEnabledItems.nextSetBit(id + 1)) {
                    family = itemFamily(name, id);
                    if (family != null) variants.computeIfAbsent(family, key -> new TreeSet<>())
                        .add(id);
                }
                for (Map.Entry<String, TreeSet<Integer>> entry : variants.entrySet()) {
                    add(groups, entry.getKey(), GT6ItemGroups.machineFilter(name, entry.getValue()));
                }
            } else if (name.equals("gregtech:gt.block.diggables")) {
                // Some blocks mix unrelated metadata (diggables: mud, turf and five kinds of clay).
                for (int id = 0; id < 16; id++) {
                    family = itemFamily(name, id);
                    if (family != null) add(groups, family, "<" + name + ":" + id + ">");
                }
            }
        }
        List<String> filters = new ArrayList<>();
        filters.add(FLUIDS);
        for (TreeSet<String> rules : groups.values()) filters.add(String.join(" | ", rules));
        return filters;
    }

    private static void add(Map<String, TreeSet<String>> groups, String family, String filter) {
        groups.computeIfAbsent(family, key -> new TreeSet<>())
            .add(filter);
    }

    static String blockFamily(String name) {
        if (!name.startsWith("gregtech:gt.block.")) return null;
        String block = name.substring("gregtech:gt.block.".length());
        if (block.startsWith("spikes.")) return "spikes";
        if (block.equals("glass") || block.equals("glass.glow")) return "glass";
        if (block.equals("paths")) return "paths";
        if (block.equals("crystalores")) return "crystals";
        if (block.equals("cfoam") || block.equals("cfoam.fresh")) return "construction-foam";
        if (block.equals("asphalt")) return "asphalt";
        if (block.equals("concrete")) return "concrete";
        if (block.equals("concrete.reinforced")) return "reinforced-concrete";
        if (block.startsWith("beam.")) return "stripped-logs";
        if (block.startsWith("log.")) return "logs";
        if (block.equals("planks") || block.startsWith("planks.")
            || block.equals("planks2")
            || block.startsWith("planks2.")) return "planks";
        if (block.equals("leaves") || block.startsWith("leaves.")) return "leaves";
        if (block.equals("sapling") || block.startsWith("sapling.")) return "saplings";
        if (block.equals("lilypad.glowtus")) return "lilypads";
        if (block.startsWith("longdistwire.")) return "long-distance-wires";
        if (block.startsWith("longdistpipe.")) return "long-distance-pipes";
        if (block.startsWith("bars.")) return "bars";
        if (block.startsWith("flower.")) return "flowers";
        if (block.startsWith("bale.")) return "bales";
        if (block.equals("sands")) return "sand";
        if (block.equals("grass")) return "grass-blocks";
        if (block.equals("rail.road")) return "road-markings";
        if (block.startsWith("rail.") && !block.equals("rail.road")) return "rails";
        return null;
    }

    static String itemFamily(String name, int id) {
        switch (name) {
            case "gregtech:gt.multiitem.technological":
                if (id >= 10000 && id <= 10031) return "extruder-shapes";
                if (id >= 10200 && id <= 10231) return "low-heat-extruder-shapes";
                if (id >= 12000 && id < 12160 && id % 20 < 10) return "component:" + (id - 12000) / 20;
                if (id >= 10800 && id <= 10805) return "food-molds";
                if (id >= 10896 && id <= 10898) return "bullet-casing-molds";
                if (id >= 10900 && id <= 10907) return "slicer-blades";
                if (id >= 11000 && id <= 11008) return "laser-emitters";
                if (id >= 20000 && id <= 20009) return "battery-cells";
                if (id >= 29987 && id <= 29999) return "electrodes";
                if (id >= 30000 && id <= 30504) return "circuit-parts:" + id / 100;
                if (id >= 32001 && id <= 32004) return "usb-sticks";
                if (id >= 32011 && id <= 32014) return "usb-cables";
                if (id >= 32021 && id <= 32024) return "usb-drives";
                break;
            case "gregtech:gt.multiitem.randomtools":
                if (id >= 900 && id <= 929) return "clay-molds";
                if (id >= 987 && id <= 997) return "unfired-clay-tools";
                if (id >= 1000 && id <= 1031 || id == 1096 || id == 1097) return "paint-sprays";
                if (id == 999 || id >= 1000 && id <= 1199 || id == 1998 || id == 1999) return "spray-cans";
                if (id >= 2000 && id <= 2818 && id % 100 <= 18)
                    return id % 100 == 0 ? "empty-wooden-buckets" : "filled-wooden-buckets";
                if (id >= 5000 && id <= 5015) return "fire-starters";
                if (id >= 8000 && id <= 8009) return "robot-tool-tips";
                if (id >= 8500 && id <= 8509) return "single-use-tools";
                if (id >= 10000 && id <= 10004) return "measuring-instruments";
                if (id >= 11000 && id <= 11004) return "compasses";
                if (id >= 12000 && id <= 12009) return "tapes";
                if (id >= 30000 && id <= 30009) return "keys";
                break;
            case "gregtech:gt.multiitem.books":
                return id == 32002 || id == 32003 || id == 32766 ? "material-dictionaries" : "guide-books";
            case "gregtech:gt.multiitem.cans":
                if (id >= 1 && id <= 86) return "canned-food:" + id / 10;
                if (id >= 32764 && id <= 32766) return "canned-air";
                break;
            case "gregtech:gt.multiitem.bottles":
                return bottleFamily(id);
            case "gregtech:gt.multiitem.food":
                if (id < 300) {
                    int crop = id / 10;
                    if (crop == 25 || crop == 26) return "nuts";
                    if (crop == 28) return "spices";
                    if (crop >= 1 && crop <= 5 || crop == 8) return "vegetables";
                    return "fruit";
                }
                if (id >= 1000 && id < 1100) return "dairy-eggs";
                if (id >= 1100 && id < 2000) return "meat";
                if (id >= 2000 && id < 12000) return "food-series:" + id / 1000;
                if (id >= 32000 && id < 32100) return "dough";
                if (id >= 32100 && id < 32200) return "food-bars";
                if (id == 32700 || id == 32701) return "potato-snacks";
                if (id >= 12000 && id <= 12003) return "cut-grass";
                if (id >= 12004 && id <= 12007) return "cereal-crops";
                if (id == 12098 || id == 12099) return "slimeballs";
                if (id >= 12100 && id <= 12103) return "plant-remains";
                if (id >= 12310 && id <= 12314) return "clay-balls";
                if (id >= 12400 && id <= 12403) return "tusks";
                if (id >= 12500 && id <= 12505) return "hooves";
                if (id >= 12600 && id <= 12602) return "horns";
                if (id == 12700 || id == 12701) return "antlers";
                if (id >= 30000 && id <= 30203) return "bee-combs";
                if (id >= 31000 && id <= 31005 || id == 31999) return "medicines";
                break;
            case "gregtech:gt.block.diggables":
                if (id == 1 || id >= 3 && id <= 6) return "clay-blocks";
                break;
            default:
                break;
        }
        return null;
    }

    static String bottleFamily(int id) {
        // IDs are explicit registration IDs, not numeric families: grape juice, wine and vinegar
        // are neighbours in GT6 but belong to different groups. Match contents, not the bottle item.
        if (id == 32000 || id == 32001 || id >= 32100 && id <= 32147) return "bottled-dyes";
        return switch (id) {
            case 0, 1, 4, 5, 6, 7, 8, 1700, 3400 -> "bottled-water";
            case 2, 3, 201, 204, 2210 -> "soft-drinks";
            case 100, 103, 109, 110, 200, 300, 400, 900, 1400, 1500, 1800, 2000, 2100, 2300, 2400, 2500, 2600, 2700, 2800, 2900, 3000, 3100, 3200, 3300, 3800, 3900, 4000, 4100, 4200, 4300, 4400, 4500, 4600, 4700, 4800, 4900, 5000, 5100, 5200, 5300, 5400, 5500, 5700 -> "juices";
            case 105, 106, 107, 108, 205, 905, 2005, 2105, 2305, 2405, 2505, 2605, 2705, 2805, 2905, 3005, 3805, 3905, 4005, 4105, 4205, 4305, 4405, 4505, 4605, 4705, 4805, 5005, 5105, 5205, 5305, 5405, 5505, 5605 -> "smoothies";
            case 101, 104, 111 -> "wine";
            case 501, 701 -> "beer";
            case 901, 1401, 3901 -> "cider";
            case 202, 301, 401, 601, 1801 -> "spirits";
            case 112, 203, 302, 402, 404, 502, 602, 1501 -> "cocktails";
            case 1304, 1305 -> "mead";
            case 102, 403, 902, 1802 -> "vinegar";
            case 500, 600, 700 -> "brewing-mash";
            case 800, 801, 802, 803, 804, 805, 1020, 1021, 3101 -> "sauces";
            case 1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007 -> "cooking-oils";
            case 1100, 1101, 1102, 1200, 5600, 5604 -> "milk-and-cream";
            case 1300, 1301, 1302, 1303, 3501 -> "honey-and-syrup";
            case 3500, 3700 -> "tree-sap";
            case 1900, 1901, 3601 -> "spreads";
            case 1600, 1601 -> "spoiled-drinks";
            case 2200, 2201, 2202 -> "bottled-slime";
            case 30000, 30001 -> "bottled-medicines";
            case 32760, 32761, 32762, 32763, 32764, 32765, 32766 -> "chemical-bottles";
            default -> null;
        };
    }
}
