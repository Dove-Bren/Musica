package com.smanzana.musica;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Musica.MODID, version = Musica.VERSION)
public class Musica
{
    public static final String MODID = "musica";
    public static final String VERSION = "1.0";
    public static Musica instance;
    @SidedProxy(clientSide="com.smanzana.musica.proxy.ClientProxy", serverSide="com.smanzana.musica.proxy.CommonProxy")
    public static com.smanzana.musica.proxy.CommonProxy proxy;
    public static Logger logger = LogManager.getLogger(MODID);
    public static Random random = new Random();
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }
    
    @EventHandler
    public void preinit(FMLPreInitializationEvent event) {
    	instance = this;
    	
    	//new ModConfig(new Configuration(event.getSuggestedConfigurationFile()));
    	
    	proxy.preinit();
    }
    
    @EventHandler
    public void postinit(FMLPostInitializationEvent event) {
    	proxy.postinit();
    	MinecraftForge.EVENT_BUS.register(this);
    }
}
