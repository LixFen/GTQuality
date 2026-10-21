package com.plainston.gtquality.integration.blockrenderer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import gregapi.tileentity.machines.MultiTileEntityBasicMachine;
import gregtech.tileentity.multiblocks.MultiTileEntityCokeOven;
import gregtech.tileentity.multiblocks.MultiTileEntityCrusher;
import gregtech.tileentity.multiblocks.MultiTileEntityElectrolyzer;
import gregtech.tileentity.multiblocks.MultiTileEntityLargeBoiler;
import gregtech.tileentity.multiblocks.MultiTileEntityShredder;
import gregtech.tileentity.multiblocks.MultiTileEntityTank3x3x3Metal;

class GT6MultiblockCatalogTest {

    @Test
    void recipeMachinesAreIncludedAlongsideTanksAndOtherMultiblocks() {
        assertTrue(GT6MultiblockCatalog.supportsController(MultiTileEntityCrusher.class));
        assertTrue(GT6MultiblockCatalog.supportsController(MultiTileEntityShredder.class));
        assertTrue(GT6MultiblockCatalog.supportsController(MultiTileEntityElectrolyzer.class));
        assertTrue(GT6MultiblockCatalog.supportsController(MultiTileEntityCokeOven.class));
        assertTrue(GT6MultiblockCatalog.supportsController(MultiTileEntityLargeBoiler.class));
        assertTrue(GT6MultiblockCatalog.supportsController(MultiTileEntityTank3x3x3Metal.class));
        // Ordinary single-block recipe machines must not be included.
        assertFalse(GT6MultiblockCatalog.supportsController(MultiTileEntityBasicMachine.class));
    }
}
