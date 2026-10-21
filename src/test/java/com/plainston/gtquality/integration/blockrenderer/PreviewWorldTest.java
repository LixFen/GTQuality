package com.plainston.gtquality.integration.blockrenderer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;

import org.junit.jupiter.api.Test;

class PreviewWorldTest {

    @Test
    void hiddenLayersCannotOccludeTheSelectedLayerAndRemainIntactAfterRendering() {
        PreviewWorld world = new PreviewWorld();
        world.isRemote = true;
        TileEntity below = new TileEntity();
        TileEntity current = new TileEntity();
        TileEntity above = new TileEntity();
        Block block = new Block(Material.rock) {};
        for (int y = 63; y <= 65; y++) world.setBlock(0, y, 0, block, 3, 0);
        world.setTileEntity(0, 63, 0, below);
        world.setTileEntity(0, 64, 0, current);
        world.setTileEntity(0, 65, 0, above);

        world.setRenderLayer(64);
        assertSame(block, world.getBlock(0, 64, 0));
        assertEquals(3, world.getBlockMetadata(0, 64, 0));
        assertSame(current, world.getTileEntity(0, 64, 0));
        for (int y : new int[] { 63, 65 }) {
            assertSame(Blocks.air, world.getBlock(0, y, 0));
            assertEquals(0, world.getBlockMetadata(0, y, 0));
            assertNull(world.getTileEntity(0, y, 0));
        }

        world.setRenderLayer(65);
        assertSame(above, world.getTileEntity(0, 65, 0));
        assertNull(world.getTileEntity(0, 64, 0));

        world.setRenderLayer(-1);
        assertSame(below, world.getTileEntity(0, 63, 0));
        assertSame(current, world.getTileEntity(0, 64, 0));
        assertSame(above, world.getTileEntity(0, 65, 0));
        for (int y = 63; y <= 65; y++) {
            assertSame(block, world.getBlock(0, y, 0));
            assertEquals(3, world.getBlockMetadata(0, y, 0));
        }
    }

}
