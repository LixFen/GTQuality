package com.plainston.gtquality.integration.nei;

import static gregapi.data.CS.NBT_INPUT;
import static gregapi.data.CS.NBT_MATERIAL;
import static gregapi.data.CS.NBT_RECIPEMAP;
import static gregapi.data.CS.NBT_TEXTURE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import gregapi.tileentity.machines.MultiTileEntityBasicMachine;
import gregapi.tileentity.machines.MultiTileEntityBasicMachineElectric;
import gregapi.tileentity.multiblocks.MultiTileEntityMultiBlockPart;
import gregtech.tileentity.sensors.MultiTileEntityLuminometer;
import gregtech.tileentity.sensors.MultiTileEntityThermometer;
import gregtech.tileentity.tools.MultiTileEntityBasin;
import gregtech.tileentity.tools.MultiTileEntityMold;

class GT6ItemGroupsTest {

    @TempDir
    Path directory;

    @Test
    void stockFluidRuleIsExtendedAndRestoredWithoutLosingOtherDefaults() throws IOException {
        Path file = directory.resolve("collapsibleitems.cfg");
        List<String> defaults = Arrays.asList("minecraft:spawn_egg", "# fluid display", "neifluiddisplay");
        Files.write(file, defaults, Charset.defaultCharset());
        GT6ItemGroups.updateConfig(file, Arrays.asList(GT6ItemGroupRules.FLUIDS));
        List<String> lines = Files.readAllLines(file, Charset.defaultCharset());
        assertEquals(GT6ItemGroupRules.FLUIDS, lines.get(2));
        assertFalse(lines.contains("neifluiddisplay"));
        GT6ItemGroups.updateConfig(file, Collections.emptyList());
        assertEquals(defaults, Files.readAllLines(file, Charset.defaultCharset()));
    }

    @Test
    void buildingFamiliesCombineVariantsWithoutCombiningUnrelatedMaterials() {
        assertEquals("spikes", GT6ItemGroupRules.blockFamily("gregtech:gt.block.spikes.steel"));
        assertEquals("spikes", GT6ItemGroupRules.blockFamily("gregtech:gt.block.spikes.fancy"));
        assertEquals("glass", GT6ItemGroupRules.blockFamily("gregtech:gt.block.glass.glow"));
        assertEquals("paths", GT6ItemGroupRules.blockFamily("gregtech:gt.block.paths"));
        assertEquals("crystals", GT6ItemGroupRules.blockFamily("gregtech:gt.block.crystalores"));
        assertEquals("construction-foam", GT6ItemGroupRules.blockFamily("gregtech:gt.block.cfoam.fresh"));
        assertEquals("rails", GT6ItemGroupRules.blockFamily("gregtech:gt.block.rail.detector.steel"));
        assertEquals("road-markings", GT6ItemGroupRules.blockFamily("gregtech:gt.block.rail.road"));
        assertNull(GT6ItemGroupRules.blockFamily("othermod:gt.block.spikes.steel"));
        assertNotEquals(
            GT6ItemGroupRules.blockFamily("gregtech:gt.block.concrete"),
            GT6ItemGroupRules.blockFamily("gregtech:gt.block.concrete.reinforced"));
    }

    @Test
    void componentTiersCombineWithinEachTypeAndExcludeGaps() {
        String item = "gregtech:gt.multiitem.technological";
        for (int base = 12000; base < 12160; base += 20) {
            assertEquals(GT6ItemGroupRules.itemFamily(item, base), GT6ItemGroupRules.itemFamily(item, base + 9));
            assertNull(GT6ItemGroupRules.itemFamily(item, base + 10));
            assertNotEquals(GT6ItemGroupRules.itemFamily(item, base), GT6ItemGroupRules.itemFamily(item, base + 20));
        }
    }

    @Test
    void booksSpraysAndFoodSeriesHaveIndependentGroups() {
        String books = "gregtech:gt.multiitem.books";
        assertEquals(GT6ItemGroupRules.itemFamily(books, 32002), GT6ItemGroupRules.itemFamily(books, 32766));
        assertNotEquals(GT6ItemGroupRules.itemFamily(books, 32002), GT6ItemGroupRules.itemFamily(books, 1));
        String tools = "gregtech:gt.multiitem.randomtools";
        assertEquals("paint-sprays", GT6ItemGroupRules.itemFamily(tools, 1000));
        assertEquals("spray-cans", GT6ItemGroupRules.itemFamily(tools, 1999));
        assertEquals("empty-wooden-buckets", GT6ItemGroupRules.itemFamily(tools, 2000));
        String food = "gregtech:gt.multiitem.food";
        assertEquals("fruit", GT6ItemGroupRules.itemFamily(food, 210));
        assertEquals("nuts", GT6ItemGroupRules.itemFamily(food, 250));
        assertEquals("vegetables", GT6ItemGroupRules.itemFamily(food, 40));
        assertEquals(GT6ItemGroupRules.itemFamily(food, 5001), GT6ItemGroupRules.itemFamily(food, 5018));
        assertNotEquals(GT6ItemGroupRules.itemFamily(food, 5001), GT6ItemGroupRules.itemFamily(food, 6010));
        assertNull(GT6ItemGroupRules.itemFamily(food, 12050));
        String bottles = "gregtech:gt.multiitem.bottles";
        assertNotEquals(GT6ItemGroupRules.itemFamily(bottles, 100), GT6ItemGroupRules.itemFamily(bottles, 111));
        assertEquals(GT6ItemGroupRules.itemFamily(bottles, 100), GT6ItemGroupRules.itemFamily(bottles, 200));
    }

    @Test
    void everyBottleRegistrationInTheGT6SnapshotHasAContentGroup() throws IOException {
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(getClass().getResourceAsStream("gt6-bottles.tsv"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split("\t", 2);
                int id = Integer.parseInt(columns[0]);
                org.junit.jupiter.api.Assertions.assertNotNull(GT6ItemGroupRules.bottleFamily(id), line);
            }
        }
        for (int id = 32100; id <= 32147; id++) assertEquals("bottled-dyes", GT6ItemGroupRules.bottleFamily(id));
        assertNull(GT6ItemGroupRules.bottleFamily(32148));
        assertNull(GT6ItemGroupRules.bottleFamily(9999));
    }

    @Test
    void neighbouringBottleIdsDoNotMixJuiceWineVinegarAndSmoothies() {
        assertEquals("juices", GT6ItemGroupRules.bottleFamily(100));
        assertEquals("wine", GT6ItemGroupRules.bottleFamily(101));
        assertEquals("vinegar", GT6ItemGroupRules.bottleFamily(102));
        assertEquals("smoothies", GT6ItemGroupRules.bottleFamily(105));
        assertEquals("cooking-oils", GT6ItemGroupRules.bottleFamily(1000));
        assertEquals("sauces", GT6ItemGroupRules.bottleFamily(3101));
        assertEquals("bottled-dyes", GT6ItemGroupRules.bottleFamily(32116));
        assertEquals("chemical-bottles", GT6ItemGroupRules.bottleFamily(32765));
    }

    @Test
    void woodPlantsAndLongDistanceConnectionsHaveSeparateFamilies() {
        assertEquals("stripped-logs", GT6ItemGroupRules.blockFamily("gregtech:gt.block.beam.1.fireproof"));
        assertEquals("logs", GT6ItemGroupRules.blockFamily("gregtech:gt.block.log.c"));
        assertEquals("planks", GT6ItemGroupRules.blockFamily("gregtech:gt.block.planks2.fireproof"));
        assertEquals("lilypads", GT6ItemGroupRules.blockFamily("gregtech:gt.block.lilypad.glowtus"));
        assertEquals("long-distance-wires", GT6ItemGroupRules.blockFamily("gregtech:gt.block.longdistwire.01"));
        assertEquals("long-distance-pipes", GT6ItemGroupRules.blockFamily("gregtech:gt.block.longdistpipe.01"));
    }

    @Test
    void ordinaryAndLowHeatExtruderShapesNeverShareAGroup() {
        String tech = "gregtech:gt.multiitem.technological";
        assertEquals("extruder-shapes", GT6ItemGroupRules.itemFamily(tech, 10000));
        assertEquals("extruder-shapes", GT6ItemGroupRules.itemFamily(tech, 10031));
        assertEquals("low-heat-extruder-shapes", GT6ItemGroupRules.itemFamily(tech, 10200));
        assertEquals("low-heat-extruder-shapes", GT6ItemGroupRules.itemFamily(tech, 10231));
        assertNull(GT6ItemGroupRules.itemFamily(tech, 10032));
        assertNull(GT6ItemGroupRules.itemFamily(tech, 10232));
    }

    @Test
    void filledWoodenBucketsAndNonFoodMaterialsAreIncluded() {
        String tools = "gregtech:gt.multiitem.randomtools";
        assertEquals("filled-wooden-buckets", GT6ItemGroupRules.itemFamily(tools, 2001));
        assertEquals("filled-wooden-buckets", GT6ItemGroupRules.itemFamily(tools, 2818));
        assertNull(GT6ItemGroupRules.itemFamily(tools, 2019));
        String food = "gregtech:gt.multiitem.food";
        assertEquals("clay-balls", GT6ItemGroupRules.itemFamily(food, 12310));
        assertEquals("hooves", GT6ItemGroupRules.itemFamily(food, 12500));
        assertEquals("hooves", GT6ItemGroupRules.itemFamily(food, 12505));
        assertEquals("clay-blocks", GT6ItemGroupRules.itemFamily("gregtech:gt.block.diggables", 1));
        assertNull(GT6ItemGroupRules.itemFamily("gregtech:gt.block.diggables", 2));
    }

    @Test
    void blockSensorsCombineWhileCastingMoldsAndBasinsRemainSeparate() {
        assertEquals(
            GT6ItemGroups.machineKey(MultiTileEntityThermometer.class, new NBTTagCompound()),
            GT6ItemGroups.machineKey(MultiTileEntityLuminometer.class, new NBTTagCompound()));
        assertNotEquals(
            GT6ItemGroups.machineKey(MultiTileEntityMold.class, new NBTTagCompound()),
            GT6ItemGroups.machineKey(MultiTileEntityBasin.class, new NBTTagCompound()));
    }

    @Test
    void generatedRulesFollowUserRulesAndReloadWithoutDuplicates() throws IOException {
        Path file = directory.resolve("collapsibleitems.cfg");
        List<String> userRules = Arrays
            .asList("# My groups", "minecraft:potion", "; {\"displayName\":\"自定义\"}", "my:item");
        Files.write(file, userRules, Charset.defaultCharset());
        GT6ItemGroups.updateConfig(file, Arrays.asList("old:rule"));
        // Even user rules added after the managed block must retain priority.
        Files.write(
            file,
            Arrays.asList("another:user_rule"),
            Charset.defaultCharset(),
            java.nio.file.StandardOpenOption.APPEND);
        GT6ItemGroups.updateConfig(file, Arrays.asList("new:rule"));
        List<String> expected = Arrays.asList(
            "# My groups",
            "minecraft:potion",
            "; {\"displayName\":\"自定义\"}",
            "my:item",
            "another:user_rule",
            "# BEGIN GTQuality automatic item groups",
            "new:rule",
            "# END GTQuality automatic item groups");
        assertEquals(expected, Files.readAllLines(file, Charset.defaultCharset()));
        GT6ItemGroups.updateConfig(file, Arrays.asList("new:rule"));
        assertEquals(expected, Files.readAllLines(file, Charset.defaultCharset()));
    }

    @Test
    void disablingRemovesOnlyAutomaticRules() throws IOException {
        Path file = directory.resolve("collapsibleitems.cfg");
        List<String> userRules = Arrays.asList("# User rules", "minecraft:potion", "");
        Files.write(file, userRules, Charset.defaultCharset());
        GT6ItemGroups.updateConfig(file, Arrays.asList("generated:rule"));
        GT6ItemGroups.updateConfig(file, Collections.emptyList());
        assertEquals(userRules, Files.readAllLines(file, Charset.defaultCharset()));
    }

    @Test
    void disabledIntegrationDoesNotCreateAConfig() throws IOException {
        Path file = directory.resolve("collapsibleitems.cfg");
        GT6ItemGroups.updateConfig(file, Collections.emptyList());
        assertFalse(Files.exists(file));
    }

    @Test
    void malformedMarkersLeaveTheOriginalFileUntouched() throws IOException {
        Path file = directory.resolve("collapsibleitems.cfg");
        List<String> original = Arrays.asList("# BEGIN GTQuality automatic item groups", "user:rule");
        Files.write(file, original, Charset.defaultCharset());
        assertThrows(IOException.class, () -> GT6ItemGroups.updateConfig(file, Arrays.asList("new:rule")));
        assertEquals(original, Files.readAllLines(file, Charset.defaultCharset()));
    }

    @Test
    void machineMaterialsAndTiersShareAGroup() {
        NBTTagCompound bronze = machine("oven", "oven");
        bronze.setString(NBT_MATERIAL, "Bronze");
        bronze.setLong(NBT_INPUT, 32);
        NBTTagCompound steel = machine("oven", "oven");
        steel.setString(NBT_MATERIAL, "Steel");
        steel.setLong(NBT_INPUT, 128);
        assertEquals(key(bronze), key(steel));
    }

    @Test
    void sharedImplementationDoesNotMergeDifferentMachineFunctions() {
        assertNotEquals(key(machine("oven", "oven")), key(machine("mixer", "mixer")));
        assertNotEquals(key(machine("smelter", "oven")), key(machine("smelter", "furnace")));
        assertNotEquals(
            key(machine("oven", "oven")),
            GT6ItemGroups.machineKey(MultiTileEntityBasicMachineElectric.class, machine("oven", "oven")));
    }

    @Test
    void multiblockWallsShareMaterialsButNotUnrelatedPartFunctions() {
        NBTTagCompound wall = machine("", "metalwall");
        NBTTagCompound bricks = machine("", "firebricks");
        assertNotEquals(
            GT6ItemGroups.machineKey(MultiTileEntityMultiBlockPart.class, wall),
            GT6ItemGroups.machineKey(MultiTileEntityMultiBlockPart.class, bricks));
    }

    @Test
    void numericWoodTexturesAreMaterialVariants() {
        NBTTagCompound oak = new NBTTagCompound();
        oak.setInteger(NBT_TEXTURE, 1);
        NBTTagCompound spruce = new NBTTagCompound();
        spruce.setInteger(NBT_TEXTURE, 2);
        assertEquals(key(oak), key(spruce));
    }

    @Test
    void filtersUseExactIdsAndStableOrderForSavedExpansionStates() {
        assertEquals(
            "<gregtech:gt.multitileentity:20001> | <gregtech:gt.multitileentity:20002>",
            GT6ItemGroups.machineFilter("gregtech:gt.multitileentity", Arrays.asList(20002, 20001, 20002)));
    }

    private static String key(NBTTagCompound parameters) {
        return GT6ItemGroups.machineKey(MultiTileEntityBasicMachine.class, parameters);
    }

    private static NBTTagCompound machine(String recipes, String texture) {
        NBTTagCompound parameters = new NBTTagCompound();
        parameters.setString(NBT_RECIPEMAP, recipes);
        parameters.setString(NBT_TEXTURE, texture);
        return parameters;
    }
}
