package com.builtbroken.netherhelm;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod.EventBusSubscriber(modid = NetherHelmMod.MODID)
@Mod(modid = NetherHelmMod.MODID, name = NetherHelmMod.NAME, version = NetherHelmMod.VERSION)
public class NetherHelmMod {
    
    public static final String MODID = "netherhelm";
    public static final String NAME = "Nether Helm";
    public static final String VERSION = "@VERSION@";

	@EventHandler
    public void preInit(FMLPreInitializationEvent event) {

    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

	}

}