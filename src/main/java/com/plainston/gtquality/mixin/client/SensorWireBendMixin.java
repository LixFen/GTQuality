package com.plainston.gtquality.mixin.client;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.plainston.gtquality.GTQuality;
import com.plainston.gtquality.render.SensorWireBend;
import com.plainston.gtquality.render.SensorWireTexture;

import gregapi.render.ITexture;
import gregapi.tileentity.connectors.MultiTileEntityWireRedstoneInsulated;
import gregapi.tileentity.connectors.TileEntityBase09Connector;
import gregapi.tileentity.connectors.TileEntityBase10ConnectorRendered;
import gregapi.tileentity.delegate.DelegatorTileEntity;
import gregapi.tileentity.machines.MultiTileEntitySensor;

@Mixin(value = TileEntityBase10ConnectorRendered.class, remap = false)
public abstract class SensorWireBendMixin extends TileEntityBase09Connector {

    @Unique
    private MultiTileEntitySensor gtquality$sensor(byte side) {
        TileEntityBase10ConnectorRendered wire = (TileEntityBase10ConnectorRendered) (Object) this;
        if (!(wire instanceof MultiTileEntityWireRedstoneInsulated) || wire.getWorldObj() == null
            || wire.mFoamDried
            || wire.mDiameter >= 1
            || !wire.connected(side)) return null;
        TileEntity neighbor = wire.getAdjacentTileEntity(side, false, false).mTileEntity;
        if (!(neighbor instanceof MultiTileEntitySensor)) return null;
        MultiTileEntitySensor sensor = (MultiTileEntitySensor) neighbor;
        return side != sensor.mFacing ? sensor : null;
    }

    @Unique
    private double[] gtquality$diameters(MultiTileEntitySensor sensor) {
        double[] diameters = new double[6];
        for (byte side = 0; side < 6; side++) {
            if (side == (sensor.mFacing ^ 1)) continue;
            TileEntity neighbor = sensor.getAdjacentTileEntity(side, false, false).mTileEntity;
            if (!(neighbor instanceof MultiTileEntityWireRedstoneInsulated)) continue;
            MultiTileEntityWireRedstoneInsulated wire = (MultiTileEntityWireRedstoneInsulated) neighbor;
            if (!wire.mFoamDried && wire.mDiameter < 1 && wire.connected((byte) (side ^ 1))) {
                diameters[side] = wire.mDiameter;
            }
        }
        return diameters;
    }

    @Inject(method = "getRenderPasses2", at = @At("RETURN"), cancellable = true, remap = false)
    private void gtquality$addBends(Block block, boolean[] shouldRender, CallbackInfoReturnable<Integer> cir) {
        if (!GTQuality.sensorWireConnections) return;
        if (!((Object) this instanceof MultiTileEntityWireRedstoneInsulated)) return;
        for (byte side = 0; side < 6; side++) {
            if (gtquality$sensor(side) == null) continue;
            // Reserve 7 for GT6 construction foam; each side gets a straight and an elbow pass.
            cir.setReturnValue(20);
            return;
        }
    }

    @Inject(method = "usesRenderPass2", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtquality$useBend(int pass, boolean[] shouldRender, CallbackInfoReturnable<Boolean> cir) {
        if (!GTQuality.sensorWireConnections) return;
        if (pass >= 8 && pass < 20) {
            byte side = (byte) ((pass - 8) / 2);
            MultiTileEntitySensor sensor = gtquality$sensor(side);
            cir.setReturnValue(
                sensor != null && (pass % 2 == 0 ? SensorWireBend.isLateral(side, sensor.mFacing)
                    : SensorWireBend.hubSide(gtquality$diameters(sensor)) == (side ^ 1)));
        }
        if (pass == 7) {
            TileEntityBase10ConnectorRendered wire = (TileEntityBase10ConnectorRendered) (Object) this;
            cir.setReturnValue(wire.mFoam);
        }
    }

    @Inject(method = "getConnectorLength", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtquality$stopFrontAtHub(byte side, DelegatorTileEntity<TileEntity> neighbor,
        CallbackInfoReturnable<Float> cir) {
        if (!GTQuality.sensorWireConnections) return;
        MultiTileEntitySensor sensor = gtquality$sensor(side);
        if (sensor == null || SensorWireBend.isLateral(side, sensor.mFacing)) return;
        double[] diameters = gtquality$diameters(sensor);
        cir.setReturnValue((float) (.5 - diameters[SensorWireBend.hubSide(diameters)] / 2));
    }

    @Inject(method = "setBlockBounds2", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtquality$bendBounds(Block block, int pass, boolean[] shouldRender,
        CallbackInfoReturnable<Boolean> cir) {
        if (!GTQuality.sensorWireConnections) return;
        if (pass < 8 || pass >= 20) return;
        byte side = (byte) ((pass - 8) / 2);
        MultiTileEntitySensor sensor = gtquality$sensor(side);
        if (sensor == null) {
            cir.setReturnValue(false);
            return;
        }
        TileEntityBase10ConnectorRendered wire = (TileEntityBase10ConnectorRendered) (Object) this;
        double[] diameters = gtquality$diameters(sensor);
        double hubDiameter = diameters[SensorWireBend.hubSide(diameters)];
        cir.setReturnValue(
            wire.box(block, SensorWireBend.bounds(side, sensor.mFacing, wire.mDiameter, hubDiameter, pass % 2 != 0)));
    }

    @Inject(method = "getTexture2", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtquality$bendTexture(Block block, int pass, byte face, boolean[] shouldRender,
        CallbackInfoReturnable<ITexture> cir) {
        if (!GTQuality.sensorWireConnections) return;
        TileEntityBase10ConnectorRendered wire = (TileEntityBase10ConnectorRendered) (Object) this;
        if (pass >= 1 && pass <= 6) {
            MultiTileEntitySensor sensor = gtquality$sensor((byte) (pass - 1));
            if (sensor != null && !SensorWireBend.isLateral((byte) (pass - 1), sensor.mFacing)) {
                if (face / 2 == (pass - 1) / 2) {
                    cir.setReturnValue(null);
                } else {
                    double[] diameters = gtquality$diameters(sensor);
                    cir.setReturnValue(
                        SensorWireTexture.segment(
                            (MultiTileEntityWireRedstoneInsulated) wire,
                            mMaterial,
                            face,
                            sensor.mFacing,
                            wire.mDiameter,
                            diameters[SensorWireBend.hubSide(diameters)],
                            false));
                }
            }
            return;
        }
        if (pass < 8 || pass >= 20) return;
        byte side = (byte) ((pass - 8) / 2);
        MultiTileEntitySensor sensor = gtquality$sensor(side);
        if (sensor == null) {
            cir.setReturnValue(null);
            return;
        }
        // The straight segment's two caps meet the existing wire and the elbow.
        if ((pass % 2 == 0 && face / 2 == side / 2) || (pass % 2 != 0 && face == (sensor.mFacing ^ 1))) {
            cir.setReturnValue(null);
            return;
        }
        double[] diameters = gtquality$diameters(sensor);
        float hubDiameter = (float) diameters[SensorWireBend.hubSide(diameters)];
        ITexture texture = pass % 2 == 0 ? wire.getTextureSide(face, wire.getDirectionData(), wire.mDiameter, 0)
            : SensorWireTexture.segment(
                (MultiTileEntityWireRedstoneInsulated) wire,
                mMaterial,
                face,
                sensor.mFacing,
                hubDiameter,
                hubDiameter,
                true);
        cir.setReturnValue(
            pass % 2 == 0 ? SensorWireTexture.lateral(texture, side)
                : new SensorWireTexture(texture, side, sensor.mFacing));
    }
}
