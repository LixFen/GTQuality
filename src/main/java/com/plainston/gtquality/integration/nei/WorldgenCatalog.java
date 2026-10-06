package com.plainston.gtquality.integration.nei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import codechicken.nei.recipe.StackInfo;
import gregapi.code.ItemStackContainer;
import gregapi.data.ANY;
import gregapi.data.CS;
import gregapi.data.FL;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.oredict.OreDictItemData;
import gregapi.oredict.OreDictManager;
import gregapi.oredict.OreDictMaterial;
import gregapi.util.ST;
import gregapi.worldgen.GT6WorldGenerator;
import gregapi.worldgen.StoneLayer;
import gregapi.worldgen.StoneLayerOres;
import gregapi.worldgen.WorldgenObject;
import gregapi.worldgen.WorldgenOresBedrock;
import gregapi.worldgen.WorldgenOresLarge;
import gregapi.worldgen.WorldgenOresSmall;
import gregapi.worldgen.WorldgenOresVanilla;
import gregtech.blocks.stone.BlockRockOres;
import gregtech.worldgen.WorldgenBlackSand;
import gregtech.worldgen.WorldgenFluidSpring;
import gregtech.worldgen.WorldgenPit;
import gregtech.worldgen.WorldgenStoneLayers;
import gregtech.worldgen.WorldgenTurf;
import gregtech.worldgen.overworld.WorldgenColtan;

/** A client-side snapshot of the configured GT6 generation paths, not a scan of a world. */
final class WorldgenCatalog {

    private final Map<WorldgenObject, Page> generators = new IdentityHashMap<>();
    private final Set<String> layerWorlds = new LinkedHashSet<>();
    private final List<Page> pages = new ArrayList<>();

    static List<Page> collect() {
        WorldgenCatalog catalog = new WorldgenCatalog();
        catalog.collectDimensions();
        catalog.collectLayers();
        catalog.pages.sort(
            Comparator.comparing((Page page) -> page.type)
                .thenComparing(page -> page.id));
        for (Page page : catalog.pages) {
            // Keep preview order within each section while moving every item/flower ahead of the rule text.
            page.rows.sort(Comparator.comparing(row -> row.stack == null));
        }
        return Collections.unmodifiableList(catalog.pages);
    }

    private void collectDimensions() {
        // Follow GT6WorldGenerator's dispatch, including the TFC/PFAA precedence and layer-mode switches.
        dimension(
            "overworld",
            GT6WorldGenerator.TFC ? CS.GEN_TFC
                : GT6WorldGenerator.PFAA ? CS.GEN_PFAA : CS.GENERATE_STONE ? CS.GEN_GT : CS.GEN_OVERWORLD,
            GT6WorldGenerator.TFC ? CS.ORE_TFC
                : GT6WorldGenerator.PFAA ? CS.ORE_PFAA : CS.GENERATE_STONE ? null : CS.ORE_OVERWORLD);
        dimension("nether", CS.GEN_NETHER, CS.ORE_NETHER);
        dimension("end", CS.GEN_END, CS.ORE_END);
        dimension("envm", CS.GENERATE_STONE ? CS.GEN_ENVM_GT : CS.GEN_ENVM, CS.GENERATE_STONE ? null : CS.ORE_ENVM);
        dimension("a97", CS.GENERATE_STONE ? CS.GEN_A97_GT : CS.GEN_A97, CS.GENERATE_STONE ? null : CS.ORE_A97);
        dimension(
            "aquacavern",
            CS.GENERATE_STONE ? CS.GEN_CW2_AquaCavern_GT : CS.GEN_CW2_AquaCavern,
            CS.GENERATE_STONE ? null : CS.ORE_CW2_AquaCavern);
        dimension(
            "caveland",
            CS.GENERATE_STONE ? CS.GEN_CW2_Caveland_GT : CS.GEN_CW2_Caveland,
            CS.GENERATE_STONE ? null : CS.ORE_CW2_Caveland);
        dimension(
            "cavenia",
            CS.GENERATE_STONE ? CS.GEN_CW2_Cavenia_GT : CS.GEN_CW2_Cavenia,
            CS.GENERATE_STONE ? null : CS.ORE_CW2_Cavenia);
        dimension(
            "cavern",
            CS.GENERATE_STONE ? CS.GEN_CW2_Cavern_GT : CS.GEN_CW2_Cavern,
            CS.GENERATE_STONE ? null : CS.ORE_CW2_Cavern);
        dimension(
            "caveworld",
            CS.GENERATE_STONE ? CS.GEN_CW2_Caveworld_GT : CS.GEN_CW2_Caveworld,
            CS.GENERATE_STONE ? null : CS.ORE_CW2_Caveworld);
        dimension("twilight", CS.GEN_TWILIGHT, CS.ORE_TWILIGHT);
        dimension("aether", CS.GEN_AETHER, CS.ORE_AETHER);
        dimension("erebus", CS.GEN_EREBUS, CS.ORE_EREBUS);
        dimension("betweenlands", CS.GEN_BETWEENLANDS, CS.ORE_BETWEENLANDS);
        dimension("atum", CS.GEN_ATUM, CS.ORE_ATUM);
        dimension("alfheim", CS.GEN_ALFHEIM, CS.ORE_ALFHEIM);
        dimension("deepdark", CS.GEN_DEEPDARK, CS.ORE_DEEPDARK);
        dimension("tropics", CS.GEN_TROPICS, CS.ORE_TROPICS);
        dimension("candy", CS.GEN_CANDY, CS.ORE_CANDY);
        dimension("moon", CS.GEN_MOON, CS.ORE_MOON);
        dimension("mars", CS.GEN_MARS, CS.ORE_MARS);
        dimension("asteroids", CS.GEN_ASTEROIDS, CS.ORE_ASTEROIDS);
        dimension("planets", CS.GEN_PLANETS, CS.ORE_PLANETS);
    }

    private void dimension(String name, List<WorldgenObject> normal, List<WorldgenObject> large) {
        for (WorldgenObject generator : normal) {
            if (generator instanceof WorldgenStoneLayers) {
                // This generator overrides enabled() to use GENERATE_STONE.
                if (CS.GENERATE_STONE) layerWorlds.add(name);
            } else {
                addGenerator(generator, name);
            }
        }
        if (large != null) for (WorldgenObject generator : large) addGenerator(generator, name);
    }

    private void addGenerator(WorldgenObject generator, String world) {
        if (!generator.mEnabled || generator.mInvalid) return;
        Page page = generators.get(generator);
        if (page == null) {
            page = describe(generator);
            if (page == null) return;
            generators.put(generator, page);
            pages.add(page);
        }
        page.worlds.add(world);
    }

    private Page describe(WorldgenObject generator) {
        if (generator instanceof WorldgenOresBedrock ore) {
            Page page = new Page("bedrock", ore.mName);
            page.resource("ore", ore.mMaterial, OP.oreBedrock.mat(ore.mMaterial, 1));
            page.detail("bedrock_height");
            page.detail("roll", "1/" + ore.mProbability);
            page.detail("bedrock_conditions");
            page.indicators(ore.mIndicatorRocks, ore.mMaterial);
            page.detail(ore.mIndicatorFlowers ? "flowers_yes" : "flowers_no");
            if (ore.mIndicatorFlowers) page.host("flower", ST.make(ore.mFlower, 1, ore.mFlowerMeta));
            return page;
        }
        if (generator instanceof WorldgenOresSmall ore) {
            Page page = new Page("small", ore.mName);
            page.resource("ore", ore.mMaterial, OP.oreSmall.mat(ore.mMaterial, 1));
            page.detail("height", ore.mMinY + "–" + (ore.mMaxY - 1));
            page.detail("attempts", smallAttempts(ore.mAmount));
            return page;
        }
        if (generator instanceof WorldgenOresLarge ore) {
            Page page = new Page("large", ore.mName);
            page.resource("top", ore.mTop, normalOreStack(ore.mTop.mID));
            page.resource("bottom", ore.mBottom, normalOreStack(ore.mBottom.mID));
            page.resource("between", ore.mBetween, normalOreStack(ore.mBetween.mID));
            page.resource("spread", ore.mSpread, normalOreStack(ore.mSpread.mID));
            page.detail("height_config", ore.mMinY + "–" + ore.mMaxY);
            page.detail("weight", ore.mWeight);
            page.detail("density", ore.mDensity);
            page.detail("size", ore.mSize);
            page.detail("distance", ore.mDistance);
            page.detail("large_conditions");
            page.indicators(ore.mIndicatorRocks, ore.mTop, ore.mBottom, ore.mBetween, ore.mSpread);
            return page;
        }
        if (generator instanceof WorldgenOresVanilla ore) {
            Page page = new Page("blob", ore.mName);
            ItemStack stack = ST.make(ore.mBlock, 1, ore.mBlockMeta);
            page.resource("ore", material(stack), stack);
            page.detail("height_origin", ore.mMinY + "–" + (ore.mMaxY - 1));
            page.detail("roll", "1/" + ore.mProbability);
            page.detail("attempts", ore.mAmount);
            page.detail("size", ore.mSize);
            page.biomes(ore.mBiomeList);
            if (ore.mReplaceBlock == null) page.detail("replace_default");
            else page.host("host", ST.make(ore.mReplaceBlock, 1, ore.mReplaceMeta));
            page.detail(ore.mAllowToGenerateinVoid ? "void_yes" : "void_no");
            return page;
        }
        if (generator instanceof WorldgenFluidSpring spring) {
            Page page = new Page("spring", spring.mName);
            page.resource("deposit", null, ST.make(spring.mBlock, 1, spring.mMeta));
            page.detail("spring_height");
            page.detail("roll", "1/" + spring.mProbability);
            page.detail("spring_conditions");
            if (spring.mSpringFluid != null) {
                page.fluid = spring.mSpringFluid.getFluid();
                page.resource("fluid", null, FL.display(spring.mSpringFluid, false, false));
                page.detail("spring_interval", spring.mSpringFluid.amount > 0 ? spring.mSpringFluid.amount : 600);
            } else page.detail("spring_finite");
            page.detail("indicator_type", spring.mIndicatorType);
            return page;
        }
        if (generator instanceof WorldgenColtan coltan) {
            Page page = new Page("coltan", coltan.mName);
            page.resource("ore", MT.OREMATS.Coltan, OP.oreSmall.mat(MT.OREMATS.Coltan, 1));
            page.resource("ore", MT.OREMATS.Columbite, OP.oreSmall.mat(MT.OREMATS.Columbite, 1));
            page.resource("ore", MT.OREMATS.Tantalite, OP.oreSmall.mat(MT.OREMATS.Tantalite, 1));
            page.detail("height", coltan.mMinY + "–" + (coltan.mMaxY - 1));
            page.detail("attempts", smallAttempts(coltan.mAmount));
            page.detail("coltan_range", coltan.mRange);
            page.detail("coltan_conditions");
            return page;
        }
        if (generator instanceof WorldgenPit pit) {
            Page page = new Page("pit", pit.mName);
            page.resource("deposit", material(ST.make(pit.mBlock, 1, pit.mMeta)), ST.make(pit.mBlock, 1, pit.mMeta));
            page.detail("roll", reducedChance(Math.min((long) pit.mChance + 1, pit.mDivider), pit.mDivider));
            page.detail("pit_height");
            Set<String> biomes = new TreeSet<>(CS.BIOMES_PLAINS);
            biomes.addAll(CS.BIOMES_SAVANNA);
            page.biomes(biomes);
            page.detail("surface_conditions");
            return page;
        }
        if (generator instanceof WorldgenBlackSand || generator instanceof WorldgenTurf) {
            boolean sand = generator instanceof WorldgenBlackSand;
            Page page = new Page(sand ? "sand" : "turf", generator.mName);
            if (sand) {
                for (int meta = 0; meta < 3; meta++)
                    page.resource("deposit", null, ST.make(CS.BlocksGT.Sands, 1, meta));
                page.biomes(CS.BIOMES_RIVER);
                Set<String> excluded = new TreeSet<>(CS.BIOMES_OCEAN_BEACH);
                excluded.addAll(CS.BIOMES_SWAMP);
                page.detail("excluded_biomes", String.join(", ", excluded));
            } else {
                page.resource("deposit", null, ST.make(CS.BlocksGT.Diggables, 1, 2));
                page.biomes(CS.BIOMES_SWAMP);
            }
            page.detail(sand ? "sand_height" : "turf_height");
            page.detail("roll", sand ? "1/64" : "1/32");
            page.detail("surface_conditions");
            return page;
        }
        return null;
    }

    private void collectLayers() {
        if (layerWorlds.isEmpty()) return;
        List<StoneLayer> layers = new ArrayList<>(StoneLayer.LAYERS);
        if (StoneLayer.DEEPSLATE != null && !layers.contains(StoneLayer.DEEPSLATE)) layers.add(StoneLayer.DEEPSLATE);
        for (StoneLayer layer : layers) {
            if (layer.mStone instanceof BlockRockOres && layer.mOres.isEmpty()) {
                Page page = new Page("rock_layer", layer.mMaterial.mNameInternal + "/" + layer.mMetaStone);
                page.worlds.addAll(layerWorlds);
                page.resource("layer_ore", layer.mMaterial, ST.make(layer.mStone, 1, layer.mMetaStone));
                page.detail("rock_layer_conditions");
                pages.add(page);
            }
            for (int i = 0; i < layer.mOres.size(); i++) {
                StoneLayerOres ore = layer.mOres.get(i);
                int min = layer.mNoDeep ? Math.max(24, ore.mMinY) : ore.mMinY;
                int max = ore.mMaxY;
                // The fallback deep layer replaces mNoDeep layers below Y=24.
                if (layer == StoneLayer.DEEPSLATE && !StoneLayer.LAYERS.contains(layer)) max = Math.min(23, max);
                if (min > max) continue;
                Page page = layerPage("layer", layer.mMaterial.mNameInternal + "/" + i, ore, min, max, i);
                page.host("host", ST.make(layer.mStone, 1, layer.mMetaStone));
                addLayerResources(page, layer, ore);
                addCompanions(page, Collections.singletonList(layer), layer.mOres, ore, min, max);
                pages.add(page);
            }
        }
        for (Map.Entry<OreDictMaterial, Map<OreDictMaterial, List<StoneLayerOres>>> top : StoneLayer.MAP.entrySet()) {
            for (Map.Entry<OreDictMaterial, List<StoneLayerOres>> bottom : top.getValue()
                .entrySet()) {
                List<StoneLayer> tops = layersFor(layers, top.getKey());
                List<StoneLayer> bottoms = layersFor(layers, bottom.getKey());
                if (tops.isEmpty() || bottoms.isEmpty()) continue;
                for (int i = 0; i < bottom.getValue()
                    .size(); i++) {
                    StoneLayerOres ore = bottom.getValue()
                        .get(i);
                    Page page = layerPage(
                        "boundary",
                        top.getKey().mNameInternal + "/" + bottom.getKey().mNameInternal + "/" + i,
                        ore,
                        ore.mMinY,
                        ore.mMaxY,
                        i);
                    for (StoneLayer layer : tops) page.host("host_top", ST.make(layer.mStone, 1, layer.mMetaStone));
                    for (StoneLayer layer : bottoms)
                        page.host("host_bottom", ST.make(layer.mStone, 1, layer.mMetaStone));
                    // A boundary is placed in its current layer, which can be either side of the transition.
                    Set<StoneLayer> hosts = new LinkedHashSet<>(tops);
                    hosts.addAll(bottoms);
                    for (StoneLayer layer : hosts) addLayerResources(page, layer, ore);
                    addCompanions(page, hosts, bottom.getValue(), ore, ore.mMinY, ore.mMaxY);
                    page.detail("boundary_conditions");
                    pages.add(page);
                }
            }
        }
        if (!StoneLayer.RANDOM_SMALL_GEM_ORES.isEmpty()) {
            Page page = new Page("gems", "random_boundary_gems");
            page.worlds.addAll(layerWorlds);
            for (OreDictMaterial gem : StoneLayer.RANDOM_SMALL_GEM_ORES)
                page.resource("ore", gem, OP.oreSmall.mat(gem, 1));
            page.detail("gem_conditions");
            pages.add(page);
        }
    }

    private Page layerPage(String type, String id, StoneLayerOres ore, int min, int max, int order) {
        Page page = new Page(type, id);
        page.worlds.addAll(layerWorlds);
        page.detail("height", min + "–" + max);
        page.detail("layer_chance", reducedChance(ore.mChance, CS.U));
        page.detail("priority", order + 1);
        page.biomes(ore.mTargetBiomes);
        page.indicators(ore.mGenerateIndicators, ore.mMaterial.mTargetCrushing.mMaterial);
        page.detail("layer_conditions");
        return page;
    }

    private static void addLayerResources(Page page, StoneLayer layer, StoneLayerOres ore) {
        for (ItemStack stack : layerStacks(layer, ore)) page.resource("ore", ore.mMaterial, stack);
    }

    private static List<ItemStack> layerStacks(StoneLayer layer, StoneLayerOres ore) {
        List<ItemStack> stacks = new ArrayList<>();
        if (ore.mBlock != null) {
            stacks.add(ST.make(ore.mBlock, 1, ore.mMeta));
        } else {
            if (layer.mOre instanceof Block block) stacks.add(ST.make(block, 1, ore.mMaterial.mID));
            if (layer.mOreSmall instanceof Block block) stacks.add(ST.make(block, 1, ore.mMaterial.mID));
        }
        return stacks;
    }

    static ItemStack normalOreStack(short materialId) {
        if (materialId <= 0) return null;
        // The generic ore prefix need not have an item. Use the physical hosts that WD.setOre uses instead.
        Object stoneOre = CS.BlocksGT.stoneToNormalOres.get(new ItemStackContainer(Blocks.stone, 1, 0));
        ItemStack stoneStack = mappedOreStack(stoneOre, materialId);
        if (stoneStack != null) return stoneStack;
        for (Object hostOre : CS.BlocksGT.stoneToNormalOres.values()) {
            ItemStack stack = mappedOreStack(hostOre, materialId);
            if (stack != null) return stack;
        }
        return null;
    }

    static ItemStack mappedOreStack(Object hostOre, short materialId) {
        if (!(hostOre instanceof Block block)) return null;
        Item item = Item.getItemFromBlock(block);
        return item == null ? null : new ItemStack(item, 1, materialId);
    }

    private static void addCompanions(Page page, Collection<StoneLayer> hosts, List<StoneLayerOres> candidates,
        StoneLayerOres target, int min, int max) {
        int previousRows = page.rows.size();
        for (StoneLayerOres candidate : candidates) {
            if (candidate.mMaterial == target.mMaterial || candidate.mMaterial.mID <= 0) continue;
            for (StoneLayer host : hosts) {
                int candidateMin = candidate.mMinY;
                int candidateMax = candidate.mMaxY;
                if ("layer".equals(page.type)) {
                    if (host.mNoDeep) candidateMin = Math.max(24, candidateMin);
                    if (host == StoneLayer.DEEPSLATE && !StoneLayer.LAYERS.contains(host))
                        candidateMax = Math.min(23, candidateMax);
                }
                if (!sameConditions(
                    min,
                    max,
                    target.mTargetBiomes,
                    candidateMin,
                    candidateMax,
                    candidate.mTargetBiomes)) continue;
                for (ItemStack stack : layerStacks(host, candidate)) {
                    if (ST.valid(stack)) page.materials.add(candidate.mMaterial);
                    page.companion(stack, candidate.mMaterial.getLocal(), reducedChance(candidate.mChance, CS.U));
                }
            }
        }
        if (page.rows.size() > previousRows) page.detail("companion_conditions");
    }

    static boolean sameConditions(int min, int max, Set<String> biomes, int otherMin, int otherMax,
        Set<String> otherBiomes) {
        return min == otherMin && max == otherMax && biomes.equals(otherBiomes);
    }

    private static List<StoneLayer> layersFor(List<StoneLayer> layers, OreDictMaterial material) {
        List<StoneLayer> result = new ArrayList<>();
        for (StoneLayer layer : layers) if (layer.mMaterial == material) result.add(layer);
        return result;
    }

    static String smallAttempts(int amount) {
        return Math.max(1, amount / 2) + "–" + Math.max(1, amount / 2 + amount / 2);
    }

    static String reducedChance(long numerator, long denominator) {
        long a = numerator;
        long b = denominator;
        while (b != 0) {
            long remainder = a % b;
            a = b;
            b = remainder;
        }
        return numerator / a + "/" + denominator / a;
    }

    private static OreDictMaterial material(ItemStack stack) {
        OreDictItemData data = OreDictManager.INSTANCE.getItemData(stack);
        return data != null && data.mMaterial != null ? data.mMaterial.mMaterial : null;
    }

    static final class Row {

        final String key;
        final Object[] arguments;
        final ItemStack stack;

        Row(String key, ItemStack stack, Object... arguments) {
            this.key = "gtquality.nei.worldgen." + key;
            this.arguments = arguments;
            this.stack = stack;
        }
    }

    static final class Page {

        final String type;
        final String id;
        final List<Row> rows = new ArrayList<>();
        final Set<String> worlds = new LinkedHashSet<>();
        final Set<OreDictMaterial> materials = new LinkedHashSet<>();
        final Set<OreDictMaterial> rocks = new LinkedHashSet<>();
        final List<ItemStack> resources = new ArrayList<>();
        final List<ItemStack> hosts = new ArrayList<>();
        Fluid fluid;

        Page(String type, String id) {
            this.type = type;
            this.id = id;
        }

        void detail(String key, Object... arguments) {
            rows.add(new Row(key, null, arguments));
        }

        void resource(String key, OreDictMaterial material, ItemStack stack) {
            if (material != null && material.mID > 0) {
                materials.add(material);
                if (material == ANY.Hexorium) materials.addAll(ANY.Hexorium.mToThis);
            }
            if (!ST.valid(stack)) return;
            if (hasPreview(key, stack)) return;
            ItemStack copy = stack.copy();
            copy.stackSize = 1;
            resources.add(copy);
            rows.add(new Row(key, copy, material == null ? copy.getDisplayName() : material.getLocal()));
        }

        void host(String key, ItemStack stack) {
            if (!ST.valid(stack)) return;
            if (hasPreview(key, stack)) return;
            ItemStack copy = stack.copy();
            copy.stackSize = 1;
            hosts.add(copy);
            rows.add(new Row(key, copy, copy.getDisplayName()));
        }

        void companion(ItemStack stack, String name, String chance) {
            if (!ST.valid(stack)) return;
            for (Row row : rows) {
                if (row.key.equals("gtquality.nei.worldgen.companion") && row.stack.isItemEqual(stack)
                    && row.arguments[1].equals(chance)) return;
            }
            ItemStack copy = stack.copy();
            copy.stackSize = 1;
            if (resources.stream()
                .noneMatch(resource -> resource.isItemEqual(copy))) resources.add(copy);
            rows.add(new Row("companion", copy, name, chance));
        }

        List<List<Row>> previewGroups() {
            Map<List<Object>, List<Row>> groups = new LinkedHashMap<>();
            for (Row row : rows) {
                if (row.stack == null) continue;
                List<Object> key = new ArrayList<>();
                key.add(row.key);
                key.addAll(Arrays.asList(row.arguments));
                groups.computeIfAbsent(key, ignored -> new ArrayList<>())
                    .add(row);
            }
            return new ArrayList<>(groups.values());
        }

        private boolean hasPreview(String key, ItemStack stack) {
            for (Row row : rows) {
                if (row.key.equals("gtquality.nei.worldgen." + key) && row.stack != null
                    && row.stack.isItemEqual(stack)) return true;
            }
            return false;
        }

        void biomes(Collection<String> names) {
            if (names == null || names.isEmpty()) detail("biomes_any");
            else detail("biomes", String.join(", ", new TreeSet<>(names)));
        }

        void indicators(boolean enabled, OreDictMaterial... materials) {
            detail(enabled ? "rocks_yes" : "rocks_no");
            if (enabled) Collections.addAll(rocks, materials);
        }

        boolean matches(ItemStack stack, boolean usage) {
            for (ItemStack resource : resources) if (resource.isItemEqual(stack)) return true;
            if (usage) for (ItemStack host : hosts) if (host.isItemEqual(stack)) return true;
            OreDictItemData data = OreDictManager.INSTANCE.getItemData(stack);
            if (data != null && data.mPrefix != null && data.mMaterial != null) {
                OreDictMaterial material = data.mMaterial.mMaterial;
                if (data.mPrefix.mNameInternal.startsWith("ore") && materials.contains(material)) return true;
                if (data.mPrefix == OP.rockGt && rocks.contains(material)) return true;
            }
            FluidStack contained = fluid == null ? null : StackInfo.getFluid(stack);
            return contained != null && contained.getFluid() == fluid;
        }
    }
}
