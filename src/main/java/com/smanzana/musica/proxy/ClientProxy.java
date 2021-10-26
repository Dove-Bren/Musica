package com.smanzana.musica.proxy;

import com.smanzana.musica.config.ModConfig;
import com.smanzana.musica.gui.GuiMusicaPopup;
import com.smanzana.musica.music.MusicPlayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {
	
	public ClientProxy() {
		super();
	}
	
	@Override
	public void preinit() {
		super.preinit();
	}
	
	@Override
	public void init() {
		super.init();
		MusicPlayer.InitVanilla();
	}
	
	@Override
	public void postinit() {
		super.postinit();
		
	}
	
	@Override
	public EntityPlayer getPlayer() {
		return Minecraft.getMinecraft().player;
	}
	
	@Override
	public boolean isServer() {
		return false;
	}
	
//	@Override
//	public String getTranslation(String key) {
//		return I18n.format(key, new Object[0]).trim();
//	}
	
//	private static boolean shownText = false;
//	@SubscribeEvent
//	public void onClientConnect(EntityJoinWorldEvent event) {
//		if (ClientProxy.shownText == false && ModConfig.config.displayLoginText()
//				&& event.getEntity() == Minecraft.getMinecraft().player) {
//			Minecraft.getMinecraft().player.sendMessage(
//					new TextComponentTranslation("info.nostrumwelcome.text", new Object[]{
//							this.bindingInfo.getDisplayName()
//					}));
//			ClientProxy.shownText = true;
//		}
//	}
	
	private GuiButton toggleVanillaButton = null;
	
	protected String getVanillaButtonText() {
		return I18n.format("menu.musica.disable_vanilla", ModConfig.shouldDisableVanilla() ? "DISABLED" : "ALLOWED");
	}
	
	@SubscribeEvent
	public void onGuiInit(InitGuiEvent.Pre event) {
		final GuiScreen gui = event.getGui();
		if (ModConfig.shouldShowPopup() && gui instanceof GuiMainMenu) {
			ModConfig.setShouldShowPopup(false);
			event.setCanceled(true);
			Minecraft.getMinecraft().displayGuiScreen(new GuiMusicaPopup(gui));
		}
	}
	
	@SubscribeEvent
	public void onGuiInit(InitGuiEvent.Post event) {
		final GuiScreen gui = event.getGui();
		if (gui instanceof GuiScreenOptionsSounds) {
			if (toggleVanillaButton == null) {
				toggleVanillaButton = new GuiButton(Integer.MIN_VALUE, 0, 0, 1, 1, "");
			}
			
			// Reset button and position it correctly
			
			// Find subtitle button and move it to the side, and position next to it
			GuiButton subtitleButton = null;
			for (GuiButton button : event.getButtonList()) {
				if (button.id == 201) {
					subtitleButton = button;
					break;
				}
			}
			
			int x = gui.width / 2 - 75;
			int y = (gui.height / 6 - 12) + (24 * 7);
			int w = 200;
			int h = 20;
			
			if (subtitleButton != null) {
				subtitleButton.x = gui.width / 2 - 155;
				x = gui.width / 2 - 155 + 160;
				y = subtitleButton.y;
				w = subtitleButton.width;
				h = subtitleButton.height;
			}
			
			toggleVanillaButton.x = x;
			toggleVanillaButton.y = y;
			toggleVanillaButton.width = w;
			toggleVanillaButton.height = h;
			toggleVanillaButton.displayString = getVanillaButtonText();
			event.getButtonList().add(toggleVanillaButton);
		}
	}
	
	@SubscribeEvent
	public void onGuiButtonClick(ActionPerformedEvent.Post event) {
		final GuiScreen gui = event.getGui();
		if (gui instanceof GuiScreenOptionsSounds) {
			if (event.getButton() == toggleVanillaButton) {
				ModConfig.setDisableVanilla(!ModConfig.shouldDisableVanilla());
				
				// Update the button text
				toggleVanillaButton.displayString = getVanillaButtonText();
			}
		}
	}
}
