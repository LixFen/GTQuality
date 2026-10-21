package com.plainston.gtquality.integration.ae2;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import gregapi.data.CS;

class PowderCellConfigTest {

    private static Item item;
    private static Item upgradeItem;

    @BeforeAll
    static void registerItemFixture() throws Exception {
        item = new Item();
        // Register only the fixture: a normal Forge launch is unavailable in headless tests.
        Method register = Item.itemRegistry.getClass()
            .getDeclaredMethod("addObjectRaw", int.class, String.class, Object.class);
        register.setAccessible(true);
        register.invoke(Item.itemRegistry, 4092, "gtquality_test_powder_marks", item);
        upgradeItem = new TestUpgradeItem();
        register.invoke(Item.itemRegistry, 4091, "gtquality_test_powder_upgrade", upgradeItem);
        Upgrades.INVERTER.registerItem(new ItemStack(item), 1);
    }

    @Test
    void onlyInverterEnablesWhitelistAndEmptyUpgradeSlotKeepsInputsUnrestricted() {
        ItemStack cell = new ItemStack(item);
        assertFalse(PowderCellConfig.hasInverter(cell));
        assertTrue(new PowderCellConfig(cell).acceptsMaterial("Iron"));
        InventoryBasic upgrades = new InventoryBasic("upgrades", false, 1);
        upgrades.setInventorySlotContents(0, new ItemStack(upgradeItem, 1, 0));
        assertTrue(PowderCellConfig.hasInverter(upgrades));
        upgrades.setInventorySlotContents(0, new ItemStack(upgradeItem, 1, 1));
        assertFalse(PowderCellConfig.hasInverter(upgrades));
        upgrades.setInventorySlotContents(0, null);
        assertFalse(PowderCellConfig.hasInverter(upgrades));
        assertTrue(new PowderCellConfig(cell).acceptsMaterial("Copper"));
    }

    @Test
    void workbenchMarksPersistInSlotOrderWithoutChangingStoredMaterials() {
        ItemStack cell = new ItemStack(item);
        PowderAmounts amounts = new PowderAmounts();
        amounts.insert("Iron", CS.U, 5, false);
        cell.setTagCompound(new NBTTagCompound());
        cell.getTagCompound()
            .setTag(PowderCellInventory.CONTENTS, amounts.write());
        PowderCellConfig config = new PowderCellConfig(cell);
        config.setInventorySlotContents(2, new ItemStack(item, 64, 1));
        config.setInventorySlotContents(62, new ItemStack(item, 1, 3));
        PowderCellConfig reloaded = new PowderCellConfig(cell);
        assertEquals(63, reloaded.getSizeInventory());
        assertEquals(
            1,
            reloaded.getStackInSlot(2)
                .getItemDamage());
        assertEquals(1, reloaded.getStackInSlot(2).stackSize);
        assertEquals(
            3,
            reloaded.getStackInSlot(62)
                .getItemDamage());
        assertNull(reloaded.getStackInSlot(0));
        assertEquals(
            amounts.write(),
            cell.getTagCompound()
                .getTagList(PowderCellInventory.CONTENTS, 10));
    }

    @Test
    void clearingMarksSurvivesReloadAndPreservesRemainderOnlyContents() {
        ItemStack cell = new ItemStack(item);
        PowderAmounts amounts = new PowderAmounts();
        amounts.insert("Iron", CS.U72, 1, false);
        cell.setTagCompound(new NBTTagCompound());
        cell.getTagCompound()
            .setTag(PowderCellInventory.CONTENTS, amounts.write());
        PowderCellConfig config = new PowderCellConfig(cell);
        config.setInventorySlotContents(0, new ItemStack(item, 1, 2));
        config.setInventorySlotContents(1, new ItemStack(item, 1, 3));
        config.setInventorySlotContents(0, null);
        PowderCellConfig reloaded = new PowderCellConfig(cell);
        assertNull(reloaded.getStackInSlot(0));
        assertEquals(
            3,
            reloaded.getStackInSlot(1)
                .getItemDamage());
        reloaded.setInventorySlotContents(1, null);
        PowderCellConfig cleared = new PowderCellConfig(cell);
        for (int slot = 0; slot < cleared.getSizeInventory(); slot++) assertNull(cleared.getStackInSlot(slot));
        assertEquals(
            amounts.write(),
            cell.getTagCompound()
                .getTagList(PowderCellInventory.CONTENTS, 10));
    }

    private static final class TestUpgradeItem extends Item implements IUpgradeModule {

        @Override
        public Upgrades getType(ItemStack stack) {
            return stack.getItemDamage() == 0 ? Upgrades.INVERTER : Upgrades.SPEED;
        }
    }
}
