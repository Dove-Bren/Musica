package com.smanzana.musica.music;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A music track that may sometimes be played.
 * Register it with a music player and at times return true that you want to be played to do so.
 * 
 * Some methods are only called when a previous track is ending or is about to loop. Others are called
 * every tick. This is noted on each method.
 * 
 * This is a client-side effect. There's no synchronization between the client and server.
 * @author Skyler
 *
 */
@SideOnly(Side.CLIENT)
public interface IMusicTrack {

	/**
	 * Check whether this track should begin playing now.
	 * This method is only called when the previous playing track has ended.
	 * @return
	 */
	public boolean shouldPlay(EntityPlayerSP player);
	
	/**
	 * Check whether this track should begin playing now.
	 * This method is called every tick for sounds that have higher priority than what's playing.
	 * Returning true will stop the playing track and play this one instead.
	 * @return
	 */
	default public boolean shouldPlayOverride(EntityPlayerSP player) { return false; }
	
	/**
	 * Return whether this track should loop.
	 * This method is only called when the previous playing track has ended.
	 * Note returning true will mean getSoundEvent is called again, and a different event can
	 * be returned.
	 * @return
	 */
	default public boolean shouldLoop(EntityPlayerSP player) { return false; };
	
	/**
	 * Return the actual sound event to begin playing.
	 * This method is only called when the previous playing track has ended.
	 * This can return different sound events each time.
	 * @return
	 */
	public MusicSound getSound(EntityPlayerSP player);
	
	default public void onStart(EntityPlayerSP player) { ; };
	default public void onStop(EntityPlayerSP player) { ; };
	
	public static MusicSound SoundFromEvent(SoundEvent event) {
		return new MusicSound(event);
	}
	
}
