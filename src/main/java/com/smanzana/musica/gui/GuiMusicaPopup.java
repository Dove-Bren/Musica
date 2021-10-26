package com.smanzana.musica.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.smanzana.musica.config.ModConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.SoundCategory;

public class GuiMusicaPopup extends GuiScreen {

	private final GuiScreen parent;
	private String title = "";
	private List<String> info;
	private String footnote = "";
	private boolean isMuted;
	
	public GuiMusicaPopup(GuiScreen parent) {
		this.parent = parent;
	}
	
	@Override
	public void initGui() {
		// First, figure out if we're already muted, as we'll display different things if we are
		isMuted = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MUSIC) <= 0f;
		
		title = I18n.format(isMuted ? "popup.title.muted" : "popup.title.unmuted", ChatFormatting.RESET, ChatFormatting.BOLD);
		info = formatString(isMuted ? "popup.info.muted" : "popup.info.unmuted");
		footnote = I18n.format("popup.footnote", ChatFormatting.RESET, ChatFormatting.BOLD);
		
		final int y = this.height - 50;
		final int h = 20;
		if (isMuted) {
			final int numButtons = 3;
			final int margin = 10;
			final int w = (this.width - ((numButtons+1) * margin)) / numButtons;
			this.addButton(new GuiButton(1,
					margin + (margin + w) * 0, y, w, h, I18n.format("popup.button.muted.leave_muted")
				));
			this.addButton(new GuiButton(2,
					margin + (margin + w) * 1, y, w, h, I18n.format("popup.button.muted.mute_vanilla")
				));
			this.addButton(new GuiButton(3,
					margin + (margin + w) * 2, y, w, h, I18n.format("popup.button.muted.unmute_all")
				));
		} else {
			final int numButtons = 3;
			final int margin = 10;
			final int w = (this.width - ((numButtons+1) * margin)) / numButtons;
			this.addButton(new GuiButton(101,
					margin + (margin + w) * 0, y, w, h, I18n.format("popup.button.unmuted.leave_unmuted")
				));
			this.addButton(new GuiButton(102,
					margin + (margin + w) * 1, y, w, h, I18n.format("popup.button.unmuted.mute_vanilla")
				));
			this.addButton(new GuiButton(103,
					margin + (margin + w) * 2, y, w, h, I18n.format("popup.button.unmuted.mute_all")
				));
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case 1:
			; // Do nothing
			break;
		case 2:
			ModConfig.setDisableVanilla(true);
			Minecraft.getMinecraft().gameSettings.setSoundLevel(SoundCategory.MUSIC, 1f);
			break;
		case 3:
			Minecraft.getMinecraft().gameSettings.setSoundLevel(SoundCategory.MUSIC, 1f);
			ModConfig.setDisableVanilla(false);
			break;
		case 101:
			ModConfig.setDisableVanilla(false);
			break;
		case 102:
			ModConfig.setDisableVanilla(true);
			break;
		case 103:
			ModConfig.setDisableVanilla(true);
			Minecraft.getMinecraft().gameSettings.setSoundLevel(SoundCategory.MUSIC, 0f);
			break;
		}
		
		this.mc.displayGuiScreen(parent);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 15, 16777215);
		
		int i = 0;
		for (String line : info) {
			final int margin = 10;
			final int y = 45 + (i * (fontRenderer.FONT_HEIGHT + 2));
			fontRenderer.drawSplitString(line, margin, y, this.width - 2 * margin, 0xFFDDDDDD);
			i += 1 + fontRenderer.getStringWidth(line) / (this.width - 2 * margin);
		}
		
		this.drawCenteredString(fontRenderer, footnote, this.width / 2, this.height - 20, 0xFFDDDDDD);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	protected static List<String> formatString(String unloc) {
		final String raw = I18n.format(unloc, ChatFormatting.RESET, ChatFormatting.BOLD);
		List<String> ret = new ArrayList<>();
		int begin = 0;
		int cursor = raw.indexOf('|');
		
		while (cursor != -1) {
			ret.add(raw.substring(begin, cursor));
			begin = cursor + 1;
			
			cursor = raw.indexOf('|', begin);
		}
		
		// Add last
		ret.add(raw.substring(begin));
		
		return ret;
	}
}
