package com.plainston.gtquality.integration.nei;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.stream.IntStream;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

class WorldgenCatalogTest {

    @Test
    void largeOrePreviewUsesPhysicalHostWithoutGenericOreItem() throws Exception {
        TestOreBlock oreBlock = new TestOreBlock();
        // Headless tests have no FML LaunchClassLoader; insert fixture entries without invoking mod registration.
        java.lang.reflect.Method register = Block.blockRegistry.getClass()
            .getDeclaredMethod("addObjectRaw", int.class, String.class, Object.class);
        register.setAccessible(true);
        register.invoke(Block.blockRegistry, 4094, "gtquality_test_ore", oreBlock);
        register.invoke(Item.itemRegistry, 4094, "gtquality_test_ore", new ItemBlock(oreBlock));
        ItemStack preview = WorldgenCatalog.mappedOreStack(oreBlock, (short) 223);
        assertNotNull(preview);
        assertNotNull(preview.getItem());
        assertEquals(223, preview.getItemDamage());
        assertEquals(1, preview.stackSize);
    }

    private static final class TestOreBlock extends Block {

        TestOreBlock() {
            super(Material.rock);
        }

    }

    @Test
    void previewGroupsCycleFormsWithoutMergingRolesOrChances() {
        Item item = new Item().setUnlocalizedName("worldgenTestOre");
        WorldgenCatalog.Page page = new WorldgenCatalog.Page("layer", "test");
        page.host("host", new ItemStack(item, 1, 10));
        page.companion(new ItemStack(item, 1, 0), "Ore", "1/24");
        page.companion(new ItemStack(item, 1, 1), "Ore", "1/24");
        page.companion(new ItemStack(item, 1, 0), "Ore", "1/48");
        page.detail("height", "24–80");
        assertEquals(
            3,
            page.previewGroups()
                .size());
        assertEquals(
            2,
            page.previewGroups()
                .get(1)
                .size());
        assertEquals(
            1,
            page.previewGroups()
                .get(2)
                .size());
    }

    @Test
    void companionConditionsRequireEqualHeightAndBiomeSets() {
        assertTrue(WorldgenCatalog.sameConditions(24, 80, Collections.emptySet(), 24, 80, Collections.emptySet()));
        assertTrue(
            WorldgenCatalog.sameConditions(
                24,
                80,
                new HashSet<>(Arrays.asList("plains", "river")),
                24,
                80,
                new HashSet<>(Arrays.asList("river", "plains"))));
        assertFalse(WorldgenCatalog.sameConditions(24, 80, Collections.emptySet(), 25, 80, Collections.emptySet()));
        assertFalse(WorldgenCatalog.sameConditions(24, 80, Collections.emptySet(), 24, 81, Collections.emptySet()));
        assertFalse(
            WorldgenCatalog.sameConditions(24, 80, Collections.emptySet(), 24, 80, Collections.singleton("plains")));
    }

    @Test
    void companionsAreDeduplicatedPreviewsWithIndependentChances() {
        Item item = new Item().setUnlocalizedName("worldgenTestOre");
        WorldgenCatalog.Page page = new WorldgenCatalog.Page("layer", "test");
        page.companion(new ItemStack(item, 1, 0), "Ore", "1/24");
        page.companion(new ItemStack(item, 3, 0), "Ore", "1/24");
        page.companion(new ItemStack(item, 1, 1), "Ore", "1/24");
        page.companion(new ItemStack(item, 1, 0), "Ore", "1/48");
        assertEquals(3, page.rows.size());
        assertEquals(2, page.resources.size());
        assertTrue(page.materials.isEmpty());
        assertTrue(page.hosts.isEmpty());
        assertTrue(page.matches(new ItemStack(item, 1, 0), false));
        assertTrue(page.matches(new ItemStack(item, 1, 1), true));
    }

    @Test
    void chancesReduceWithoutRounding() {
        assertEquals("1/24", WorldgenCatalog.reducedChance(151200, 3628800));
        assertEquals("4/21", WorldgenCatalog.reducedChance(691200, 3628800));
        assertEquals("2/3", WorldgenCatalog.reducedChance(6, 9));
        assertEquals("1/1", WorldgenCatalog.reducedChance(3628800, 3628800));
        assertEquals("0/1", WorldgenCatalog.reducedChance(0, 3628800));
        assertEquals("7/13", WorldgenCatalog.reducedChance(7, 13));
    }

    @Test
    void boundaryHostsDeduplicateEachSideAndPreserveMetadata() {
        Item item = new Item().setUnlocalizedName("worldgenTestStone");
        WorldgenCatalog.Page page = new WorldgenCatalog.Page("boundary", "test");
        page.host("host_top", new ItemStack(item, 1, 0));
        page.host("host_top", new ItemStack(item, 3, 0));
        page.host("host_top", new ItemStack(item, 1, 1));
        page.host("host_bottom", new ItemStack(item, 1, 0));
        page.host("host_bottom", new ItemStack(item, 1, 0));
        assertEquals(3, page.rows.size());
        assertEquals(3, page.hosts.size());
        assertEquals(
            1,
            page.hosts.get(1)
                .getItemDamage());
        assertEquals("gtquality.nei.worldgen.host_bottom", page.rows.get(2).key);
    }

    @Test
    void smallOreAttemptRangeIncludesEveryPossibleRoll() {
        // Enumerate the generator's random domain, including odd Amount and the minimum of one attempt.
        for (int amount = 1; amount <= 128; amount++) {
            final int configuredAmount = amount;
            IntSummaryStatistics attempts = IntStream.rangeClosed(0, amount)
                .map(roll -> Math.max(1, configuredAmount / 2 + roll / 2))
                .summaryStatistics();
            assertEquals(
                attempts.getMin() + "–" + attempts.getMax(),
                WorldgenCatalog.smallAttempts(amount),
                "Amount=" + amount);
        }
    }
}
