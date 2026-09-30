package com.plainston.gtquality.generators;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.world.World;

import gregapi.util.WD;

public final class CombustionAir {

    private CombustionAir() {}

    public static boolean hasCollision(World world, int x, int y, int z) {
        return !isClosedTrapDoor(world, x, y, z) && WD.hasCollide(world, x, y, z);
    }

    public static boolean hasOxygen(World world, int x, int y, int z) {
        return isClosedTrapDoor(world, x, y, z) || WD.oxygen(world, x, y, z);
    }

    private static boolean isClosedTrapDoor(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return block instanceof BlockTrapDoor && (world.getBlockMetadata(x, y, z) & 4) == 0;
    }
}
