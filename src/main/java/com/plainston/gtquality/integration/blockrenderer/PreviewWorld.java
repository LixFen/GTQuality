package com.plainston.gtquality.integration.blockrenderer;

import java.lang.reflect.Field;
import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;

import blockrenderer6343.client.world.TrackedDummyWorld;
import cpw.mods.fml.relauncher.ReflectionHelper;
import gregapi.tileentity.base.TileEntityBase02AdjacentTEBuffer;

/** GT6 checks adjacent TileEntities itself instead of honoring RenderBlocks.renderAllFaces. */
final class PreviewWorld extends TrackedDummyWorld {

    private static final Field ADJACENT_CACHE = ReflectionHelper
        .findField(TileEntityBase02AdjacentTEBuffer.class, "mBufferedTileEntities");

    private int renderLayer = -1;

    void setRenderLayer(int y) {
        if (renderLayer == y) return;
        renderLayer = y;
        // Controllers cache both neighbors and missing-neighbor markers, bypassing getTileEntity below.
        // Clear both when entering or leaving a slice so neither view leaks into the other.
        for (TileEntity tile : tileMap.values()) {
            if (!(tile instanceof TileEntityBase02AdjacentTEBuffer)) continue;
            try {
                Arrays.fill((TileEntity[]) ADJACENT_CACHE.get(tile), null);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException("Cannot clear GT6 preview neighbor cache", exception);
            }
        }
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return hidden(y) ? Blocks.air : super.getBlock(x, y, z);
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        return hidden(y) ? 0 : super.getBlockMetadata(x, y, z);
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        return hidden(y) ? null : super.getTileEntity(x, y, z);
    }

    private boolean hidden(int y) {
        return renderLayer >= 0 && y != renderLayer;
    }
}
