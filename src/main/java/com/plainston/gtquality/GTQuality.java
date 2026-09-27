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
    public static boolean allowSapBagHopperExtraction;
    public static float scaffoldClimbUpSpeed;
    public static float scaffoldClimbDownSpeed;

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
        allowSapBagHopperExtraction = config.getBoolean(
            "allowSapBagHopperExtraction", "general", true,
            "Allow hoppers and other sided inventory automation to extract resin items from GT6 Resin/Sap Bags.");
        scaffoldClimbUpSpeed = config.getFloat(
            "scaffoldClimbUpSpeed", "general", Loader.isModLoaded("gaiablossom") ? 0.0F : 0.14F, 0.0F, 2.0F,
            "Extra upward movement on GT6 scaffolds at maximum upward pitch, in blocks per tick. "
                + "Defaults to 0 when GaiaTweaks is installed. Set to 0 to disable.");
        scaffoldClimbDownSpeed = config.getFloat(
            "scaffoldClimbDownSpeed", "general", 0.15F, 0.0F, 2.0F,
            "Extra downward movement on GT6 scaffolds at maximum downward pitch, in blocks per tick. Set to 0 to disable.");
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
        MinecraftForge.EVENT_BUS.register(new ScaffoldClimb());
        if (event.getSide().isClient() && gtmToolBars && !Loader.isModLoaded("duradisplay")) ToolBarRenderer.install();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        HopperHitbox.install();
        SmallCoverHitbox.install();
        if (allowSapBagHopperExtraction) SapBagHopperExport.install();
        GuiFluidInteraction.install();
    }
}
