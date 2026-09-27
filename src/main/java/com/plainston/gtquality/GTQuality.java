package com.plainston.gtquality;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import gregapi.data.CS;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

@Mod(
    modid = GTQuality.MODID,
    version = Tags.VERSION,
    name = "GTQuality",
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:gregtech")
public class GTQuality {

    public static final String MODID = "gtquality";

    static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

    public static boolean gtmToolBars;
    public static boolean allowObstructedInteraction;
    public static boolean guiFluidInteraction;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        gtmToolBars = config.getBoolean(
            "gtmToolBars", "client", true, "Replace GT6 tool charge and durability icons with GTM-style bars in GUIs.");
        allowObstructedInteraction = config.getBoolean(
            "allowObstructedInteraction", "general", true,
            "Disable GT6 obstruction checks, allowing interaction with machine faces blocked by adjacent blocks.");
        guiFluidInteraction = config.getBoolean(
            "guiFluidInteraction", "general", true,
            "Allow fluid containers on the cursor to fill and drain GT6 machine tanks through their GUI slots.");
        if (config.hasChanged()) config.save();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        if (allowObstructedInteraction) CS.OBSTRUCTION_CHECKS = false;
        NETWORK.registerMessage(MoldPackets.OpenHandler.class, MoldPackets.Open.class, 0, Side.CLIENT);
        NETWORK.registerMessage(MoldPackets.SelectHandler.class, MoldPackets.Select.class, 1, Side.SERVER);
        FMLCommonHandler.instance().bus().register(new MoldPackets());
        MoldInteraction.install();
        MoldTooltip.register();
        MinecraftForge.EVENT_BUS.register(new MoldTooltip());
        if (event.getSide().isClient() && gtmToolBars && !Loader.isModLoaded("duradisplay")) ToolBarRenderer.install();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        HopperHitbox.install();
        SmallCoverHitbox.install();
        GuiFluidInteraction.install();
    }
}
