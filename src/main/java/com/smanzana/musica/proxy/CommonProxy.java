package com.smanzana.musica.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {
	
	public CommonProxy() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void preinit() {
		//NetworkHandler.getInstance();
	}
	
	public void init() {
    	;
	}
	
	public void postinit() {
		;
	}
    
	public EntityPlayer getPlayer() {
		return null; // Doesn't mean anything on the server
	}
	
	public boolean isServer() {
		return true;
	}
}
