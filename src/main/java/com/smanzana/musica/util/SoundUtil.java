package com.smanzana.musica.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;
import com.smanzana.musica.Musica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class SoundUtil {

	public static final void stopSound(ISound sound) {
		Minecraft.getMinecraft().getSoundHandler().stopSound(sound);
		
		// Vanilla doesn't actually remove the sound from its playing list, so if you try and add it again
		// you crash. Remove from the internal playing map, too, and clear out category.
		try {
			// Get sound manager from the handler
			SoundManager handler_sndManager =
					ObfuscationReflectionHelper.getPrivateValue(SoundHandler.class, Minecraft.getMinecraft().getSoundHandler(), "sndManager");
			
			// Get the 'playingSounds' (reg name -> sound handle) map
			Map<String, ISound> manager_playingSounds = 
				ObfuscationReflectionHelper.getPrivateValue(SoundManager.class, handler_sndManager, "playingSounds");
			// And the 'categorySounds" (category -> reg name) map
			HashMultimap<SoundCategory, String> manager_categorySounds = 
					ObfuscationReflectionHelper.getPrivateValue(SoundManager.class, handler_sndManager, "categorySounds");
			
			// Iterate running sound map and remove every match, storing names for category removal
			List<String> removedNames = new ArrayList<>();
			Iterator<Entry<String, ISound>> playingIterator = manager_playingSounds.entrySet().iterator();
			while (playingIterator.hasNext()) {
				Entry<String, ISound> entry = playingIterator.next();
				if (entry.getValue().equals(sound)) {
					removedNames.add(entry.getKey());
					playingIterator.remove();
				}
			}
			
			// Repeat for category map
			for (SoundCategory category : SoundCategory.values()) {
				for (String removedName : removedNames) {
					manager_categorySounds.remove(category, removedName);
				}
			}
		} catch (Exception e) {
			Musica.logger.error("Failed to fully cancel running sound: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
}
