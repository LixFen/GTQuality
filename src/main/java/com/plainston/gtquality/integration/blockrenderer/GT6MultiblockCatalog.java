package com.plainston.gtquality.integration.blockrenderer;

import static gregapi.data.CS.SIDE_Z_NEG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blockrenderer6343.client.world.TrackedDummyWorld;
import gregapi.block.multitileentity.MultiTileEntityClassContainer;
import gregapi.block.multitileentity.MultiTileEntityContainer;
import gregapi.block.multitileentity.MultiTileEntityRegistry;
import gregapi.tileentity.base.TileEntityBase09FacingSingle;
import gregapi.tileentity.multiblocks.ITileEntityMultiBlockController;
import gregapi.tileentity.multiblocks.MultiTileEntityMultiBlockPart;
import gregapi.tileentity.multiblocks.TileEntityBase10MultiBlockBase;
import gregapi.tileentity.multiblocks.TileEntityBase10MultiBlockMachine;
import gregtech.tileentity.multiblocks.MultiTileEntityBedrockDrill;
import gregtech.tileentity.multiblocks.MultiTileEntityFusionReactor;
import gregtech.tileentity.multiblocks.MultiTileEntityLightningRod;
import gregtech.tileentity.multiblocks.MultiTileEntityMatterFabricator;

final class GT6MultiblockCatalog {

    private static final Logger LOG = LogManager.getLogger("GTQuality Multiblock Preview");
    static final int X = 0, Y = 64, Z = 0;

    static List<Page> collect() {
        MultiTileEntityRegistry registry = MultiTileEntityRegistry.getRegistry("gt.multitileentity");
        if (registry == null) return Collections.emptyList();
        List<Page> pages = new ArrayList<>();
        TrackedDummyWorld world = new TrackedDummyWorld();
        // Avoid server tick registrations, energy networks and placement callbacks in GT6's NBT initialization.
        world.isRemote = true;
        try {
            for (MultiTileEntityClassContainer entry : registry.mRegistrations) {
                if (entry.mHidden || !supportsController(entry.mClass)) continue;
                try {
                    world.clear();
                    TileEntityBase09FacingSingle controller = (TileEntityBase09FacingSingle) place(
                        world,
                        registry,
                        entry.mID,
                        X,
                        Y,
                        Z);
                    if (controller.getValidSides()[SIDE_Z_NEG]) controller.mFacing = SIDE_Z_NEG;
                    seedEnvironment(world, controller);
                    PreviewLayout layout = new PreviewLayout(kind(controller), X, Y, Z, 1);
                    try (PreviewCapture ignored = new PreviewCapture(controller, layout)) {
                        if (!checkStructure(controller) || layout.parts()
                            .isEmpty()) {
                            throw new IllegalStateException("Could not capture a complete structure");
                        }
                    }
                    List<PreviewLayout.Part> parts = new ArrayList<>(layout.parts());
                    populateParts(world, controller, parts);
                    // Run the original checks without capture against real GT6 parts before publishing the page.
                    if (!checkStructure(controller)) {
                        throw new IllegalStateException("Generated structure failed GT6 validation");
                    }
                    pages.add(
                        new Page(
                            registry,
                            entry.mID,
                            controller.mFacing,
                            parts,
                            controller instanceof MultiTileEntityBedrockDrill));
                } catch (RuntimeException exception) {
                    LOG.warn("Could not preview GT6 controller {}", entry.mID, exception);
                }
            }
        } finally {
            world.clear();
        }
        LOG.info("Registered {} GT6 multiblock previews", pages.size());
        return Collections.unmodifiableList(pages);
    }

    static boolean supportsController(Class<?> type) {
        // Recipe multiblocks and other controllers use separate, unrelated GT6 base classes.
        return TileEntityBase10MultiBlockBase.class.isAssignableFrom(type)
            || TileEntityBase10MultiBlockMachine.class.isAssignableFrom(type);
    }

    private static boolean checkStructure(TileEntityBase09FacingSingle controller) {
        if (controller instanceof TileEntityBase10MultiBlockBase base) return base.checkStructure2(null, null, null);
        return ((TileEntityBase10MultiBlockMachine) controller).checkStructure2(null, null, null);
    }

    private static PreviewLayout.Kind kind(TileEntity controller) {
        if (controller instanceof MultiTileEntityFusionReactor) return PreviewLayout.Kind.FUSION;
        if (controller instanceof MultiTileEntityMatterFabricator) return PreviewLayout.Kind.MATTER;
        if (controller instanceof MultiTileEntityLightningRod) return PreviewLayout.Kind.LIGHTNING;
        return PreviewLayout.Kind.STANDARD;
    }

    private static TileEntity place(TrackedDummyWorld world, MultiTileEntityRegistry registry, int id, int x, int y,
        int z) {
        MultiTileEntityContainer container = registry.getNewTileEntityContainer(world, x, y, z, id, null);
        if (container == null) throw new IllegalStateException("Missing GT6 part " + id);
        world.setBlock(x, y, z, container.mBlock, container.mBlockMetaData, 0);
        world.setTileEntity(x, y, z, container.mTileEntity);
        return container.mTileEntity;
    }

    private static void seedEnvironment(TrackedDummyWorld world, TileEntity controller) {
        if (controller instanceof MultiTileEntityBedrockDrill) {
            for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
                world.setBlock(X + x, Y - 5, Z + z, Blocks.bedrock, 0, 0);
            }
        }
    }

    private static void populateParts(TrackedDummyWorld world, TileEntityBase09FacingSingle controller,
        List<PreviewLayout.Part> parts) {
        for (PreviewLayout.Part cell : parts) {
            PreviewLayout.Position pos = cell.position();
            MultiTileEntityMultiBlockPart part = (MultiTileEntityMultiBlockPart) place(
                world,
                MultiTileEntityRegistry.getRegistry(cell.registry()),
                cell.id(),
                pos.x(),
                pos.y(),
                pos.z());
            part.setTarget((ITileEntityMultiBlockController) controller, cell.design(), cell.mode());
        }
    }

    static final class Page {

        final MultiTileEntityRegistry registry;
        final int id;
        final byte facing;
        final List<PreviewLayout.Part> parts;
        final boolean bedrock;
        final List<ItemStack> ingredients;

        Page(MultiTileEntityRegistry registry, int id, byte facing, List<PreviewLayout.Part> parts, boolean bedrock) {
            this.registry = registry;
            this.id = id;
            this.facing = facing;
            this.parts = parts;
            this.bedrock = bedrock;
            Map<String, ItemStack> counts = new LinkedHashMap<>();
            for (PreviewLayout.Part part : parts) {
                String key = part.registry() + ":" + part.id();
                ItemStack stack = counts.get(key);
                if (stack == null) counts.put(
                    key,
                    MultiTileEntityRegistry.getRegistry(part.registry())
                        .getItem(part.id()));
                else stack.stackSize++;
            }
            ingredients = new ArrayList<>(counts.values());
        }

        ItemStack controllerStack() {
            return registry.getItem(id);
        }

        void populate(TrackedDummyWorld world) {
            world.isRemote = true;
            TileEntityBase09FacingSingle controller = (TileEntityBase09FacingSingle) place(
                world,
                registry,
                id,
                X,
                Y,
                Z);
            controller.mFacing = facing;
            seedEnvironment(world, controller);
            populateParts(world, controller, parts);
            controller.setDirectionData((byte) (facing | 8));
        }

        ItemStack stackAt(int x, int y, int z) {
            if (x == X && y == Y && z == Z) return controllerStack();
            for (PreviewLayout.Part part : parts) {
                PreviewLayout.Position pos = part.position();
                if (pos.x() == x && pos.y() == y && pos.z() == z) {
                    return MultiTileEntityRegistry.getRegistry(part.registry())
                        .getItem(part.id());
                }
            }
            return null;
        }
    }
}
