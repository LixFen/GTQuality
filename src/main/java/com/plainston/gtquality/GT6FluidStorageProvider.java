package com.plainston.gtquality;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.gtnewhorizons.wdmla.api.accessor.Accessor;
import com.gtnewhorizons.wdmla.api.provider.IClientExtensionProvider;
import com.gtnewhorizons.wdmla.api.provider.IServerExtensionProvider;
import com.gtnewhorizons.wdmla.api.view.ClientViewGroup;
import com.gtnewhorizons.wdmla.api.view.FluidView;
import com.gtnewhorizons.wdmla.api.view.ViewGroup;

import gregapi.tileentity.tank.TileEntityBase08FluidContainer;

/** Supplies GT6 tank contents to WDMla's native fluid gauge renderer. */
public enum GT6FluidStorageProvider
    implements IServerExtensionProvider<FluidView.Data>, IClientExtensionProvider<FluidView.Data, FluidView> {

    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation("gtquality", "gt6_fluid_storage");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public boolean shouldRequestData(Accessor accessor) {
        return accessor.getTarget() instanceof IFluidHandler
            || accessor.getTarget() instanceof TileEntityBase08FluidContainer;
    }

    @Override
    public List<ViewGroup<FluidView.Data>> getGroups(Accessor accessor) {
        Object target = accessor.getTarget();
        FluidTankInfo[] tanks;
        if (target instanceof TileEntityBase08FluidContainer) {
            tanks = new FluidTankInfo[] { ((TileEntityBase08FluidContainer) target).mTank.getInfo() };
        } else if (target instanceof IFluidHandler) {
            tanks = ((IFluidHandler) target).getTankInfo(ForgeDirection.UNKNOWN);
        } else {
            return null;
        }
        if (tanks == null || tanks.length == 0) return Collections.emptyList();

        List<FluidView.Data> views = new ArrayList<>(tanks.length);
        for (FluidTankInfo tank : tanks) {
            if (tank == null || tank.fluid == null || tank.fluid.amount <= 0) continue;
            views.add(new FluidView.Data(tank.fluid.copy(), tank.capacity));
        }
        return views.isEmpty() ? Collections.emptyList() : Collections.singletonList(new ViewGroup<>(views));
    }

    @Override
    public List<ClientViewGroup<FluidView>> getClientGroups(Accessor accessor, List<ViewGroup<FluidView.Data>> groups) {
        return ClientViewGroup.map(groups, FluidView::readDefault, null);
    }
}
