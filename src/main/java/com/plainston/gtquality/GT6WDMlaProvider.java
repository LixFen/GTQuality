package com.plainston.gtquality;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import com.gtnewhorizons.wdmla.api.accessor.BlockAccessor;
import com.gtnewhorizons.wdmla.api.provider.IBlockComponentProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerDataProvider;
import com.gtnewhorizons.wdmla.api.ui.IComponent;
import com.gtnewhorizons.wdmla.api.ui.ITooltip;
import com.gtnewhorizons.wdmla.api.ui.MessageType;
import com.gtnewhorizons.wdmla.impl.ui.ThemeHelper;
import com.gtnewhorizons.wdmla.impl.ui.component.FluidComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.HPanelComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.ProgressComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.TextComponent;
import com.gtnewhorizons.wdmla.impl.ui.component.VPanelComponent;
import com.gtnewhorizons.wdmla.impl.ui.drawable.FluidDrawable;
import com.gtnewhorizons.wdmla.impl.ui.sizer.Padding;
import com.gtnewhorizons.wdmla.impl.ui.style.ProgressStyle;

import mcp.mobius.waila.overlay.DisplayUtil;
import gregapi.block.multitileentity.MultiTileEntityRegistry;
import gregapi.data.CS;
import gregapi.data.FM;
import gregapi.data.MT;
import gregapi.oredict.OreDictMaterial;
import gregapi.recipes.Recipe;
import gregapi.tileentity.connectors.MultiTileEntityAxle;
import gregapi.tileentity.connectors.MultiTileEntityPipeFluid;
import gregapi.tileentity.connectors.MultiTileEntityWireElectric;
import gregapi.tileentity.machines.MultiTileEntityBasicMachine;
import gregapi.tileentity.machines.ITileEntityRunningActively;
import gregapi.tileentity.machines.ITileEntityRunningPassively;
import gregapi.tileentity.machines.ITileEntitySwitchableOnOff;
import gregapi.tileentity.multiblocks.MultiTileEntityMultiBlockPart;
import gregapi.tileentity.tank.TileEntityBase08Barrel;
import gregapi.tileentity.tank.TileEntityBase08FluidContainer;
import gregtech.tileentity.energy.converters.MultiTileEntityBoilerTank;
import gregtech.tileentity.energy.converters.MultiTileEntityEngineSteam;
import gregtech.tileentity.energy.converters.MultiTileEntityTurbineSteam;
import gregtech.tileentity.energy.generators.MultiTileEntityGeneratorFluidBed;
import gregtech.tileentity.energy.generators.MultiTileEntityGeneratorLiquid;
import gregtech.tileentity.energy.generators.MultiTileEntityGeneratorSolid;
import gregtech.tileentity.energy.generators.MultiTileEntityMotorLiquid;
import gregtech.tileentity.energy.reactors.MultiTileEntityReactorCore;
import gregtech.tileentity.misc.MultiTileEntityFluidSpring;
import gregtech.tileentity.misc.MultiTileEntityRock;
import gregtech.tileentity.multiblocks.MultiTileEntityCokeOven;
import gregtech.tileentity.multiblocks.MultiTileEntityCrucible;
import gregtech.tileentity.multiblocks.MultiTileEntityTank;
import gregtech.tileentity.plants.MultiTileEntityBush;
import gregtech.tileentity.tools.MultiTileEntityAnvil;
import gregtech.tileentity.tools.MultiTileEntityMixingBowl;
import gregtech.tileentity.tools.MultiTileEntitySiftingTable;
import gregtech.tileentity.tools.MultiTileEntitySmeltery;

/** Modern WDMla presentation for the GT6WailaCompact machine details. */
public enum GT6WDMlaProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation("gtquality", "gt6_details");
    private static final String PARAMS = "gtquality.parameters";
    private static final String TARGET = "gtquality.target";
    private static final String RECIPE_ACTIVE = "gtquality.recipe.active";
    private static final String RECIPE_COMPLETED = "gtquality.recipe.completed";
    private static final String RECIPE_ITEM_OUTPUTS = "gtquality.recipe.item_outputs";
    private static final String RECIPE_FLUID_OUTPUTS = "gtquality.recipe.fluid_outputs";
    private static final String STATE_SUPPORTED = "gtquality.state.supported";
    private static final String STATE_ON = "gtquality.state.on";
    private static final String STATE_PASSIVE = "gtquality.state.passive";
    private static final String STATE_ACTIVE = "gtquality.state.active";
    private static final Field BOILER_COOLDOWN = findBoilerCooldownField();

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        return isSupported(tile) && !(tile instanceof TileEntityBase08FluidContainer)
                && !(tile instanceof TileEntityBase08Barrel) && !(tile instanceof MultiTileEntityTank)
                && !(tile instanceof MultiTileEntityPipeFluid);
    }

    @Override
    public void appendServerData(NBTTagCompound data, BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        if (!isSupported(tile)) return;

        writeMachineData(tile, data);

        if (tile instanceof MultiTileEntityMultiBlockPart && data.hasKey("gt.target")) {
            int x = data.getInteger("gt.target.x");
            int y = data.getInteger("gt.target.y");
            int z = data.getInteger("gt.target.z");
            TileEntity target = accessor.getWorld().getTileEntity(x, y, z);
            if (target != null && !(target instanceof MultiTileEntityMultiBlockPart) && isSupported(target)) {
                NBTTagCompound targetData = new NBTTagCompound();
                writeMachineData(target, targetData);
                data.setTag(TARGET, targetData);
            }
        }
    }

    private static void writeMachineData(TileEntity tile, NBTTagCompound data) {
        tile.writeToNBT(data);
        int mteId = data.getInteger("gt.mte.id");
        if (mteId > 0) {
            try {
                MultiTileEntityRegistry registry = MultiTileEntityRegistry.getRegistry("gt.multitileentity");
                gregapi.block.multitileentity.MultiTileEntityClassContainer container = registry == null ? null
                        : registry.getClassContainer(mteId);
                if (container != null && container.mParameters != null) {
                    data.setTag(PARAMS, container.mParameters.copy());
                }
            } catch (RuntimeException ignored) {
                // Keep the live tile data useful even if a registry entry is unavailable.
            }
        }

        if (tile instanceof MultiTileEntityBoilerTank) {
            data.setInteger("gt.cooldown", readBoilerCooldown(tile));
        }
        if (tile instanceof MultiTileEntityAxle) {
            data.setLong("gt.transfer.ru", ((MultiTileEntityAxle) tile).mTransferredLast);
        } else if (tile instanceof MultiTileEntityWireElectric) {
            data.setLong("gt.transfer.eu", ((MultiTileEntityWireElectric) tile).mWattageLast);
        } else if (tile instanceof TileEntityBase08FluidContainer) {
            data.setInteger("gtquality.capacity", ((TileEntityBase08FluidContainer) tile).mTank.getCapacity());
        } else if (tile instanceof TileEntityBase08Barrel) {
            data.setInteger("gtquality.capacity", ((TileEntityBase08Barrel) tile).mTank.getCapacity());
        } else if (tile instanceof MultiTileEntityTank) {
            data.setInteger("gtquality.capacity", ((MultiTileEntityTank) tile).mTank.getCapacity());
        }

        if (tile instanceof MultiTileEntitySmeltery) {
            data.setLong("gtquality.max_temperature", ((MultiTileEntitySmeltery) tile).getTemperatureMax((byte) 0));
        } else if (tile instanceof MultiTileEntityCrucible) {
            data.setLong("gtquality.max_temperature", ((MultiTileEntityCrucible) tile).getTemperatureMax((byte) 0));
        }

        if (tile instanceof MultiTileEntityBasicMachine) {
            writeRecipeOutputs((MultiTileEntityBasicMachine) tile, data);
        }
        writeMachineState(tile, data);
    }

    private static void writeMachineState(TileEntity tile, NBTTagCompound data) {
        if (!(tile instanceof ITileEntityRunningActively) && !(tile instanceof ITileEntityRunningPassively)
                && !(tile instanceof ITileEntitySwitchableOnOff)) return;

        data.setBoolean(STATE_SUPPORTED, true);
        if (tile instanceof ITileEntitySwitchableOnOff) {
            data.setBoolean(STATE_ON, ((ITileEntitySwitchableOnOff) tile).getStateOnOff());
        } else {
            data.setBoolean(STATE_ON, !data.hasKey(CS.NBT_STOPPED));
        }
        data.setBoolean(
                STATE_PASSIVE,
                tile instanceof ITileEntityRunningPassively
                        ? ((ITileEntityRunningPassively) tile).getStateRunningPassively()
                        : data.getBoolean(STATE_ON));
        if (tile instanceof ITileEntityRunningActively) {
            data.setBoolean(STATE_ACTIVE, ((ITileEntityRunningActively) tile).getStateRunningActively());
        }
    }

    private static void writeRecipeOutputs(MultiTileEntityBasicMachine machine, NBTTagCompound data) {
        boolean completed = machine.mSuccessful && machine.mCurrentRecipe != null;
        data.setBoolean(RECIPE_COMPLETED, completed);
        boolean active = machine.mCurrentRecipe != null && machine.mMaxProgress > 0;
        data.setBoolean(RECIPE_ACTIVE, active);
        if (!active && !completed) return;

        NBTTagList itemOutputs = new NBTTagList();
        ItemStack[] itemStacks = active ? machine.mOutputItems : machine.mCurrentRecipe.mOutputs;
        if (itemStacks != null) {
            for (ItemStack stack : itemStacks) {
                if (stack == null || stack.stackSize <= 0) continue;
                NBTTagCompound itemData = new NBTTagCompound();
                stack.writeToNBT(itemData);
                itemOutputs.appendTag(itemData);
            }
        }
        data.setTag(RECIPE_ITEM_OUTPUTS, itemOutputs);

        NBTTagList fluidOutputs = new NBTTagList();
        FluidStack[] fluidStacks = active ? machine.mOutputFluids : machine.mCurrentRecipe.mFluidOutputs;
        if (fluidStacks != null) {
            for (FluidStack fluid : fluidStacks) {
                if (fluid == null || fluid.amount <= 0) continue;
                NBTTagCompound fluidData = new NBTTagCompound();
                fluid.writeToNBT(fluidData);
                fluidOutputs.appendTag(fluidData);
            }
        }
        data.setTag(RECIPE_FLUID_OUTPUTS, fluidOutputs);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor) {
        TileEntity tile = accessor.getTileEntity();
        NBTTagCompound data = accessor.getServerData();
        if (tile instanceof MultiTileEntityMultiBlockPart && data.hasKey(TARGET)) {
            int x = data.getInteger("gt.target.x");
            int y = data.getInteger("gt.target.y");
            int z = data.getInteger("gt.target.z");
            TileEntity target = accessor.getWorld().getTileEntity(x, y, z);
            if (target != null) appendMachineInfo(tooltip, target, data.getCompoundTag(TARGET), accessor, true);
            return;
        }
        if (tile != null) appendMachineInfo(tooltip, tile, data, accessor, false);
    }

    private static boolean isSupported(TileEntity tile) {
        return tile instanceof MultiTileEntityBasicMachine || tile instanceof MultiTileEntitySmeltery || tile instanceof MultiTileEntityCrucible
                || tile instanceof MultiTileEntityBoilerTank || tile instanceof MultiTileEntityRock
                || tile instanceof TileEntityBase08FluidContainer || tile instanceof MultiTileEntityGeneratorSolid
                || tile instanceof MultiTileEntityGeneratorLiquid || tile instanceof MultiTileEntityGeneratorFluidBed
                || tile instanceof MultiTileEntityMultiBlockPart || tile instanceof MultiTileEntityAnvil
                || tile instanceof MultiTileEntityEngineSteam || tile instanceof MultiTileEntityTurbineSteam
                || tile instanceof MultiTileEntityAxle || tile instanceof MultiTileEntityWireElectric
                || tile instanceof MultiTileEntityFluidSpring || tile instanceof MultiTileEntityMixingBowl
                || tile instanceof TileEntityBase08Barrel || tile instanceof MultiTileEntityPipeFluid
                || tile instanceof MultiTileEntitySiftingTable || tile instanceof MultiTileEntityMotorLiquid
                || tile instanceof MultiTileEntityReactorCore || tile instanceof MultiTileEntityBush
                || tile instanceof MultiTileEntityCokeOven || tile instanceof MultiTileEntityTank;
    }

    private static void appendMachineInfo(ITooltip tooltip, TileEntity tile, NBTTagCompound data,
            BlockAccessor accessor, boolean throughMultiblockPart) {
        if (data.getBoolean(STATE_SUPPORTED)) {
            if (tile instanceof MultiTileEntityBasicMachine) appendBasicMachineState(tooltip, data);
            else appendInterfaceMachineState(tooltip, data);
        }
        if (tile instanceof MultiTileEntityBasicMachine) appendBasicMachineInfo(tooltip, data);

        if (tile instanceof MultiTileEntitySmeltery || tile instanceof MultiTileEntityCrucible) {
            addLine(tooltip, "temperature", data.getShort("gt.temperature") + " / "
                    + data.getLong("gtquality.max_temperature") + " K");
            appendMaterials(tooltip, data.getCompoundTag("gt.materials"));
        } else if (tile instanceof MultiTileEntityBoilerTank) {
            appendBoiler(tooltip, data, throughMultiblockPart);
        } else if (tile instanceof MultiTileEntityGeneratorSolid) {
            appendSolidGenerator(tooltip, data);
        } else if (tile instanceof MultiTileEntityGeneratorFluidBed) {
            appendFluidBed(tooltip, data, throughMultiblockPart);
        } else if (tile instanceof MultiTileEntityGeneratorLiquid) {
            appendLiquidGenerator(tooltip, data, FM.Burn.mRecipeFluidMap, throughMultiblockPart);
        } else if (tile instanceof MultiTileEntityRock) {
            addItem(tooltip, "rock_content", getItem(data, "gt.value"));
        } else if (tile instanceof TileEntityBase08FluidContainer) {
            if (throughMultiblockPart) {
                addFluidGauge(tooltip, "tank", getFluid(data, "gt.tank"), data.getInteger("gtquality.capacity"));
            }
        } else if (tile instanceof MultiTileEntityMultiBlockPart) {
            return;
        } else if (tile instanceof MultiTileEntityAnvil) {
            appendAnvil(tooltip, data, accessor);
        } else if (tile instanceof MultiTileEntityEngineSteam) {
            appendSteamEngine(tooltip, data);
        } else if (tile instanceof MultiTileEntityTurbineSteam) {
            appendSteamTurbine(tooltip, data);
        } else if (tile instanceof MultiTileEntityAxle) {
            addLine(tooltip, "transfer_ru", data.getLong("gt.transfer.ru") + " RU/t");
        } else if (tile instanceof MultiTileEntityWireElectric) {
            addLine(tooltip, "transfer_eu", data.getLong("gt.transfer.eu") + " EU/t");
        } else if (tile instanceof MultiTileEntityFluidSpring) {
            FluidStack fluid = getFluid(data, "gt.spring");
            if (fluid != null) {
                addLine(tooltip, "spring_fluid", fluid.getLocalizedName());
                addLine(tooltip, "spring_rate", formatTime(fluid.amount) + " / bucket");
            }
        } else if (tile instanceof MultiTileEntityMixingBowl) {
            appendMixingBowl(tooltip, data, throughMultiblockPart);
        } else if (tile instanceof TileEntityBase08Barrel || tile instanceof MultiTileEntityTank) {
            if (throughMultiblockPart) {
                addFluidGauge(tooltip, "tank", getFluid(data, "gt.tank"), data.getInteger("gtquality.capacity"));
            }
        } else if (tile instanceof MultiTileEntityPipeFluid) {
            if (throughMultiblockPart) appendPipe(tooltip, data);
        } else if (tile instanceof MultiTileEntitySiftingTable) {
            appendSifter(tooltip, data);
        } else if (tile instanceof MultiTileEntityMotorLiquid) {
            appendLiquidMotor(tooltip, data, throughMultiblockPart);
        } else if (tile instanceof MultiTileEntityReactorCore) {
            appendReactor(tooltip, data, throughMultiblockPart);
        } else if (tile instanceof MultiTileEntityBush) {
            appendBush(tooltip, data);
        }
    }

    private static void appendBoiler(ITooltip tooltip, NBTTagCompound data, boolean throughMultiblockPart) {
        NBTTagCompound params = data.getCompoundTag(PARAMS);
        FluidStack fuel = getFluid(data, "gt.tank.0");
        FluidStack steam = getFluid(data, "gt.tank.1");
        long steamCapacity = Math.max(1, params.getLong("gt.capacity.su"));
        long heatCapacity = Math.max(1, params.getLong("gt.capacity"));
        if (throughMultiblockPart) {
            addFluidGauge(tooltip, "fuel", fuel, 4000);
            addProgressGauge(tooltip, "steam", steam == null ? 0 : steam.amount, steamCapacity, steam);
        }
        addProgressGauge(tooltip, "heat", data.getLong("gt.energy"), heatCapacity, null);

        long steamAmount = steam == null ? 0 : steam.amount;
        long outFactor = Math.max(0, Math.min(3, 4 * steamAmount / steamCapacity - 1));
        int state = (int) outFactor;
        if (data.getInteger("gt.cooldown") < 120 && state == 0 && data.getLong("gt.energy") == 0) state = 3;
        if (data.getLong("gt.energy") != 0 && (fuel == null || fuel.amount == 0)) state = 4;
        long efficiency = data.hasKey("gt.eff") ? data.getLong("gt.eff") : 10000;
        addLine(tooltip, "efficiency", formatPercent(efficiency / 100.0));
        addLine(tooltip, "steam_output", params.getLong("gt.output.su") * outFactor + " L/t");
        addLine(tooltip, "boiler_state", tr("state." + state));
    }

    private static void appendSolidGenerator(ITooltip tooltip, NBTTagCompound data) {
        ItemStack fuel = getInventory(data).get(0);
        ItemStack ash = getInventory(data).get(1);
        if (ash == null && fuel != null && FM.Furnace.mRecipeItemMap.get(fuel) == null) {
            ash = fuel;
            fuel = null;
        }
        addItem(tooltip, "fuel", fuel);
        addItem(tooltip, "ash", ash);
        if (data.hasKey("gt.active")) {
            long output = data.getCompoundTag(PARAMS).getLong("gt.output");
            long ticks = output <= 0 ? data.getLong("gt.energy") : data.getLong("gt.energy") / output;
            addLine(tooltip, "burn_time", formatTime(ticks));
            addLine(tooltip, "heat_output", output + " HU/t");
        }
    }

    private static void appendFluidBed(ITooltip tooltip, NBTTagCompound data, boolean throughMultiblockPart) {
        ItemStack fuel = getInventory(data).get(0);
        ItemStack ash = getInventory(data).get(1);
        if (ash == null && fuel != null && FM.FluidBed.mRecipeItemMap.get(fuel) == null) {
            ash = fuel;
            fuel = null;
        }
        if (throughMultiblockPart) addFluidGauge(tooltip, "calcite", getFluid(data, "gt.tank"), 1000);
        addItem(tooltip, "fuel", fuel);
        addItem(tooltip, "ash", ash);
        if (data.hasKey("gt.active")) {
            long output = data.getCompoundTag(PARAMS).getLong("gt.output");
            long ticks = output <= 0 ? data.getLong("gt.energy") : data.getLong("gt.energy") / output;
            addLine(tooltip, "burn_time", formatTime(ticks));
            addLine(tooltip, "heat_output", output + " HU/t");
        }
    }

    private static void appendLiquidGenerator(ITooltip tooltip, NBTTagCompound data, Map<String, ?> recipeMap,
            boolean throughMultiblockPart) {
        FluidStack fuel = getFluid(data, "gt.tank");
        long output = Math.max(1, data.getCompoundTag(PARAMS).getLong("gt.output"));
        if (throughMultiblockPart) addFluidGauge(tooltip, "fuel", fuel, output * 10);
        if (data.hasKey("gt.active")) {
            double rate = getFuelConsumption(recipeMap, fuel, output);
            addLine(tooltip, "fuel_rate", String.format(Locale.ROOT, "%.2f L/t", rate));
            addLine(tooltip, "heat_output", output + " HU/t");
        }
    }

    private static double getFuelConsumption(Map<String, ?> recipeMap, FluidStack fuel, long output) {
        if (fuel == null) return 0;
        Object recipesObject = recipeMap.get(fuel.getFluid().getName());
        if (!(recipesObject instanceof Iterable<?> recipes)) return 0;
        for (Object entry : recipes) {
            if (!(entry instanceof Recipe recipe)) continue;
            long fuelHeat = -recipe.mEUt * recipe.mDuration;
            if (fuelHeat <= 0) return 0;
            if (recipe.mFluidInputs != null) {
                for (FluidStack input : recipe.mFluidInputs) {
                    if (input != null && input.isFluidEqual(fuel) && input.amount > 0) {
                        fuelHeat /= input.amount;
                        break;
                    }
                }
            }
            return fuelHeat <= 0 ? 0 : output / (double) fuelHeat;
        }
        return 0;
    }

    private static void appendAnvil(ITooltip tooltip, NBTTagCompound data, BlockAccessor accessor) {
        long durability = data.getLong("gt.durability") / 1000;
        addLine(tooltip, "anvil_durability", (durability / 10) + "." + (durability % 10));
        int side = accessor.getHitResult() == null ? -1 : accessor.getHitResult().sideHit;
        int facing = data.getInteger("gt.facing");
        boolean endFace = side >= 0 && ((facing == 2 || facing == 3) ? side >= 4
                : (facing == 4 || facing == 5) && side > 1 && side < 4);
        if (endFace) {
            boolean bigEnd = facing == 2 || facing == 3 ? (side - facing) % 2 == 1 : (facing - side) % 2 == 1;
            addLine(tooltip, "anvil_end", tr(bigEnd ? "anvil_big" : "anvil_small"));
        }
    }

    private static void appendSteamEngine(ITooltip tooltip, NBTTagCompound data) {
        NBTTagCompound params = data.getCompoundTag(PARAMS);
        long capacity = Math.max(1, params.getLong(CS.NBT_CAPACITY));
        long energy = data.getLong(CS.NBT_ENERGY);
        addProgressGauge(tooltip, "stored_ku", energy, capacity, null);
        long output = params.getLong("gt.output");
        long state = data.getLong(CS.NBT_VISUAL);
        long efficiency = data.getLong(CS.NBT_EFFICIENCY);
        long ku = output * (state + 1) / 16;
        long steam = efficiency <= 0 ? 0 : ku * 2 * 10000 / efficiency;
        if (!data.hasKey(CS.NBT_ACTIVE)) {
            ku = 0;
            steam = 0;
        }
        addLine(tooltip, "energy_output", ku + " KU/t");
        addLine(tooltip, "steam_input", steam + " L/t");
    }

    private static void appendSteamTurbine(ITooltip tooltip, NBTTagCompound data) {
        long output = data.hasKey("gt.output.su") ? data.getLong("gt.output.su")
                : amount(getFluid(data, "gt.tank.0"));
        long input = data.getCompoundTag(PARAMS).getLong("gt.input");
        if (output > input * 2) {
            addLine(tooltip, "state", tr("overpowered"));
        } else {
            addLine(tooltip, "steam_input", output + " L/t");
            addLine(tooltip, "energy_output", output / 3 + " RU/t");
        }
    }

    private static void appendMixingBowl(ITooltip tooltip, NBTTagCompound data, boolean throughMultiblockPart) {
        Map<Integer, ItemStack> inventory = getInventory(data);
        VPanelComponent inputs = new VPanelComponent();
        for (Map.Entry<Integer, ItemStack> entry : inventory.entrySet()) {
            if (entry.getKey() != 6 && entry.getValue() != null) inputs.child(ThemeHelper.INSTANCE.itemStackFullLine(entry.getValue()));
        }
        if (inputs.childrenSize() > 0) {
            tooltip.child(new HPanelComponent().text(tr("item_inputs") + ":").child(inputs));
        }
        addItem(tooltip, "item_output", inventory.get(6));
        if (throughMultiblockPart) {
            for (int i = 0; i < 6; i++) addFluidSlot(tooltip, "fluid_input", i, getFluid(data, "gt.tank.in." + i));
            for (int i = 0; i < 2; i++) addFluidSlot(tooltip, "fluid_output", i, getFluid(data, "gt.tank.out." + i));
        }
    }

    private static void appendPipe(ITooltip tooltip, NBTTagCompound data) {
        boolean wide = data.hasKey("gt.mlast.8");
        int columns = wide ? 3 : 2;
        int count = columns * columns;
        HPanelComponent row = new HPanelComponent();
        for (int i = 0; i < count; i++) {
            FluidStack fluid = getFluid(data, "gt.tank." + i);
            HPanelComponent cell = new HPanelComponent();
            if (fluid != null) cell.child(new FluidComponent(fluid));
            cell.child(ThemeHelper.INSTANCE.info(
                    fluid == null ? tr("empty") : fluid.amount + " mB " + fluid.getLocalizedName()));
            row.child(cell);
            if (i % columns == columns - 1) {
                tooltip.child(row);
                row = new HPanelComponent();
            }
        }
    }

    private static void appendSifter(ITooltip tooltip, NBTTagCompound data) {
        Map<Integer, ItemStack> inventory = getInventory(data);
        addItem(tooltip, "item_input", inventory.get(0));
        int lastSlot = inventory.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        addItems(tooltip, "item_output", inventory, 1, lastSlot + 1);
    }

    private static void appendLiquidMotor(ITooltip tooltip, NBTTagCompound data, boolean throughMultiblockPart) {
        long output = data.getCompoundTag(PARAMS).getLong("gt.output");
        FluidStack fuel = getFluid(data, "gt.tank.0");
        if (throughMultiblockPart) {
            long capacity = Math.max(1, output * 10);
            addFluidGauge(tooltip, "fuel", fuel, capacity);
            addFluidGauge(tooltip, "exhaust", getFluid(data, "gt.tank.1"), capacity);
        }
        if (data.hasKey("gt.energy")) {
            addLine(tooltip, "fuel_rate", String.format(Locale.ROOT, "%.2f L/t", getFuelConsumption(FM.Engine.mRecipeFluidMap, fuel, output)));
            addLine(tooltip, "energy_output", output + " RU/t");
        }
    }

    private static void appendReactor(ITooltip tooltip, NBTTagCompound data, boolean throughMultiblockPart) {
        FluidStack coolant = getFluid(data, "gt.tank.0");
        if (throughMultiblockPart) {
            addFluidGauge(tooltip, "coolant", coolant, 64000);
            addFluidGauge(tooltip, "hot_fluid", getFluid(data, "gt.tank.1"), 64000);
        }
        Map<Integer, ItemStack> rods = getInventory(data);
        long heat = 0;
        for (Map.Entry<Integer, ItemStack> entry : rods.entrySet()) {
            ItemStack rod = entry.getValue();
            if (rod == null) continue;
            long neutron = data.getLong("gt.value.o." + entry.getKey());
            switch (getRodType(rod.getItemDamage())) {
                case 1 -> heat += neutron * 2;
                case 2 -> heat += neutron / 2;
                case 3 -> heat += neutron;
                default -> { }
            }
            String durability = rod.hasTagCompound()
                    ? " · " + formatTime(rod.getTagCompound().getLong(CS.NBT_DURABILITY) / 100)
                    : "";
            addLine(tooltip, "reactor_rod", entry.getKey() + ": " + rod.getDisplayName() + durability);
        }
        if (coolant != null && MT.Sn.mLiquid.isFluidEqual(coolant)) heat = (long) Math.ceil(heat / 3.0);
        else if (coolant != null && MT.Na.mLiquid.isFluidEqual(coolant)) heat = (long) Math.ceil(heat / 6.0);
        if (heat != 0) addLine(tooltip, "heat_output", heat + " HU/t");

        StringBuilder neutrons = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (!data.hasKey("gt.value.o." + i)) continue;
            if (neutrons.length() != 0) neutrons.append(" | ");
            neutrons.append(i).append(": ").append(data.getLong("gt.value.o." + i));
        }
        if (neutrons.length() != 0) addLine(tooltip, "neutron_count", neutrons.toString());
    }

    private static void appendBush(ITooltip tooltip, NBTTagCompound data) {
        long state = data.getLong("gt.state");
        long progress = data.getLong("gt.progress");
        if (state == 3) {
            addProgressGauge(tooltip, "growth", 1, 1, null, tr("mature"));
        } else {
            long current = state * 256 + progress + (progress < 0 ? 256 : 0);
            addProgressGauge(tooltip, "growth", Math.max(0, current), 768, null);
        }
        addItem(tooltip, "crop_output", getItem(data, "gt.value"));
    }

    private static void appendBasicMachineInfo(ITooltip tooltip, NBTTagCompound data) {
        boolean active = data.getBoolean(RECIPE_ACTIVE);
        boolean completed = data.getBoolean(RECIPE_COMPLETED);
        if (active || completed) {
            long maxProgress = Math.max(1, data.getLong("gt.maxprogress"));
            long progress = completed && !active ? maxProgress : data.getLong("gt.progress");
            addRecipeProgress(tooltip, progress, maxProgress);

            Map<Integer, ItemStack> itemOutputs = getItemList(data.getTagList(RECIPE_ITEM_OUTPUTS, 10));
            addRecipeItems(tooltip, "recipe_item_outputs", itemOutputs);

            NBTTagList fluidOutputs = data.getTagList(RECIPE_FLUID_OUTPUTS, 10);
            if (fluidOutputs.tagCount() > 0) tooltip.text(tr("recipe_fluid_outputs") + ":");
            for (int i = 0; i < fluidOutputs.tagCount(); i++) {
                FluidStack fluid = FluidStack.loadFluidStackFromNBT(fluidOutputs.getCompoundTagAt(i));
                addNamedFluid(tooltip, fluid);
            }
        }

        Map<Integer, ItemStack> inventory = getInventory(data);
        int lastSlot = inventory.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1);
        addItems(tooltip, "machine_items", inventory, 0, lastSlot + 1);
    }

    private static void appendBasicMachineState(ITooltip tooltip, NBTTagCompound data) {
        String state;
        if (!data.getBoolean(STATE_ON) || !data.getBoolean(STATE_PASSIVE)) {
            state = "state.stopped";
        } else if (data.getBoolean(STATE_ACTIVE)
                && ((data.getBoolean(RECIPE_ACTIVE) && data.getLong("gt.maxprogress") > 0)
                        || data.getBoolean(RECIPE_COMPLETED))) {
            state = "state.running";
        } else if (!data.getBoolean(RECIPE_ACTIVE) && data.getLong("gt.maxprogress") <= 0) {
            state = "state.idle";
        } else {
            state = "state.stopped";
        }
        addStateLine(tooltip, state);
    }

    private static void appendInterfaceMachineState(ITooltip tooltip, NBTTagCompound data) {
        String state = !data.getBoolean(STATE_ON) || !data.getBoolean(STATE_PASSIVE) ? "state.stopped"
                : data.getBoolean(STATE_ACTIVE) ? "state.running" : "state.idle";
        addStateLine(tooltip, state);
    }

    private static void addStateLine(ITooltip tooltip, String stateKey) {
        IComponent state = "state.running".equals(stateKey) ? ThemeHelper.INSTANCE.success(tr(stateKey))
                : "state.idle".equals(stateKey) ? ThemeHelper.INSTANCE.color(tr(stateKey), MessageType.MOD_NAME)
                        : ThemeHelper.INSTANCE.failure(tr("state.stopped"));
        tooltip.child(new HPanelComponent().text(tr("state") + ": ").child(state));
    }

    private static void appendMaterials(ITooltip tooltip, NBTTagCompound materials) {
        VPanelComponent rows = new VPanelComponent();
        StringBuilder row = new StringBuilder();
        int rowStart = 0;
        for (int i = 0; i < 32; i++) {
            String slot = Integer.toString(i);
            if (!materials.hasKey(slot)) break;
            NBTTagCompound materialTag = materials.getCompoundTag(slot);
            OreDictMaterial material = OreDictMaterial.get(materialTag.getShort("i"));
            if (material == null) continue;
            long amount = materialTag.getLong("a") / 648648;
            if (row.length() == 0) rowStart = i;
            else row.append(" · ");
            row.append(String.format(Locale.ROOT, "%.3f u %s", amount / 1000.0,
                    StatCollector.translateToLocal("gt.material." + material.mNameInternal)));
            if (i % 4 == 3 || i == 31 || !materials.hasKey(Integer.toString(i + 1))) {
                rows.text(String.format("%s %d–%d: %s", tr("materials"), rowStart + 1, i + 1, row));
                row.setLength(0);
            }
        }
        if (rows.childrenSize() > 0) tooltip.child(rows);
    }

    private static void addFluidSlot(ITooltip tooltip, String key, int slot, FluidStack fluid) {
        if (fluid == null) return;
        tooltip.text(tr(key) + " " + (slot + 1) + ":");
        HPanelComponent row = new HPanelComponent();
        row.child(new FluidComponent(fluid));
        row.child(ThemeHelper.INSTANCE.info(fluid.amount + " mB " + fluid.getLocalizedName()));
        tooltip.child(row);
    }

    private static void addNamedFluid(ITooltip tooltip, FluidStack fluid) {
        if (fluid == null || fluid.amount <= 0) return;
        HPanelComponent row = new HPanelComponent();
        row.child(new FluidComponent(fluid));
        row.child(ThemeHelper.INSTANCE.info(fluid.amount + " mB " + fluid.getLocalizedName()));
        tooltip.child(row);
    }

    private static void addFluidGauge(ITooltip tooltip, String key, FluidStack fluid, long capacity) {
        addProgressGauge(tooltip, key, amount(fluid), capacity, fluid);
    }

    private static void addRecipeProgress(ITooltip tooltip, long current, long max) {
        long safeMax = Math.max(1, max);
        long safeCurrent = Math.max(0, Math.min(current, safeMax));
        long percentage = Math.round(safeCurrent * 100.0 / safeMax);
        String description = tr("progress_short") + ": " + percentage + "%";
        tooltip.child(new ProgressComponent(safeCurrent, safeMax)
                .style(new ProgressStyle())
                .child(new TextComponent(description).padding(new Padding(2, 1, 2, 3))));
    }

    private static void addProgressGauge(ITooltip tooltip, String key, long current, long max, FluidStack overlay) {
        addProgressGauge(tooltip, key, current, max, overlay, null);
    }

    private static void addProgressGauge(ITooltip tooltip, String key, long current, long max, FluidStack overlay,
            String valueOverride) {
        long safeMax = Math.max(1, max);
        long safeCurrent = Math.max(0, Math.min(current, safeMax));
        String text = tr(key) + ": " + (valueOverride == null
                ? Math.max(0, current) + " / " + Math.max(0, max) + " (" + formatPercent(current * 100.0 / safeMax) + ")"
                : valueOverride);
        ProgressStyle style = new ProgressStyle();
        if (overlay != null) style.overlay(new FluidDrawable(overlay));
        tooltip.child(new ProgressComponent(safeCurrent, safeMax).style(style)
                .child(new TextComponent(text).padding(new Padding(2, 1, 2, 3))));
    }

    private static void addLine(ITooltip tooltip, String key, String value) {
        tooltip.child(new HPanelComponent().text(tr(key) + ": ").child(ThemeHelper.INSTANCE.info(value)));
    }

    private static void addItem(ITooltip tooltip, String key, ItemStack stack) {
        if (stack != null) {
            tooltip.text(tr(key) + ":");
            tooltip.child(ThemeHelper.INSTANCE.itemStackFullLine(stack));
        }
    }

    private static void addItems(ITooltip tooltip, String key, Map<Integer, ItemStack> inventory, int from, int to) {
        VPanelComponent list = new VPanelComponent();
        for (int i = from; i < to; i++) {
            ItemStack stack = inventory.get(i);
            if (stack != null) list.child(ThemeHelper.INSTANCE.itemStackFullLine(stack));
        }
        if (list.childrenSize() > 0) {
            tooltip.text(tr(key) + ":");
            tooltip.child(list);
        }
    }

    private static void addRecipeItems(ITooltip tooltip, String key, Map<Integer, ItemStack> items) {
        if (items.isEmpty()) return;
        tooltip.text(tr(key) + ":");
        VPanelComponent list = new VPanelComponent();
        for (ItemStack stack : items.values()) {
            HPanelComponent row = new HPanelComponent();
            row.child(ThemeHelper.INSTANCE.smallItem(stack));
            row.text(String.valueOf(stack.stackSize));
            row.text(StatCollector.translateToLocal("hud.msg.wdmla.item.count"));
            row.child(ThemeHelper.INSTANCE.info(
                    DisplayUtil.stripSymbols(DisplayUtil.itemDisplayNameShortFormatted(stack))));
            list.child(row);
        }
        tooltip.child(list);
    }

    private static Map<Integer, ItemStack> getInventory(NBTTagCompound data) {
        Map<Integer, ItemStack> items = new HashMap<>();
        NBTTagList inventory = data.getTagList("gt.invlist", 10);
        for (int i = 0; i < inventory.tagCount(); i++) {
            NBTTagCompound itemTag = inventory.getCompoundTagAt(i);
            ItemStack stack = ItemStack.loadItemStackFromNBT(itemTag);
            if (stack != null) items.put(itemTag.getInteger("s"), stack);
        }
        return items;
    }

    private static Map<Integer, ItemStack> getItemList(NBTTagList itemList) {
        Map<Integer, ItemStack> items = new HashMap<>();
        for (int i = 0; i < itemList.tagCount(); i++) {
            ItemStack stack = ItemStack.loadItemStackFromNBT(itemList.getCompoundTagAt(i));
            if (stack != null && stack.stackSize > 0) items.put(i, stack);
        }
        return items;
    }

    private static ItemStack getItem(NBTTagCompound data, String key) {
        return data.hasKey(key, 10) ? ItemStack.loadItemStackFromNBT(data.getCompoundTag(key)) : null;
    }

    private static FluidStack getFluid(NBTTagCompound data, String key) {
        return data.hasKey(key, 10) ? FluidStack.loadFluidStackFromNBT(data.getCompoundTag(key)) : null;
    }

    private static long amount(FluidStack fluid) {
        return fluid == null ? 0 : Math.max(0, fluid.amount);
    }

    private static int getRodType(int meta) {
        if (meta > 9400) return 1;
        if (meta > 9300) return 0;
        if (meta > 9209) return 3;
        if (meta == 9202) return 2;
        return 0;
    }

    private static Field findBoilerCooldownField() {
        try {
            Field field = MultiTileEntityBoilerTank.class.getDeclaredField("mCoolDownResetTimer");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static int readBoilerCooldown(TileEntity boiler) {
        if (BOILER_COOLDOWN == null) return 0;
        try {
            return BOILER_COOLDOWN.getShort(boiler);
        } catch (IllegalAccessException ignored) {
            return 0;
        }
    }

    private static String formatTime(long ticks) {
        if (ticks < 20) return ticks + " t";
        if (ticks < 1200) return (ticks / 20) + " s";
        if (ticks < 72000) return (ticks / 1200) + " min " + ((ticks % 1200) / 20) + " s";
        if (ticks < 1728000) return (ticks / 72000) + " h " + ((ticks % 72000) / 1200) + " min";
        return (ticks / 1728000) + " d " + ((ticks % 1728000) / 72000) + " h";
    }

    private static String formatPercent(double percent) {
        return String.format(Locale.ROOT, "%.1f%%", percent);
    }

    private static String tr(String key) {
        return StatCollector.translateToLocal("gtquality.wdmla." + key);
    }
}
