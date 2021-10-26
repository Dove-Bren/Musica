package com.smanzana.musica.music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;

import com.smanzana.musica.Musica;
import com.smanzana.musica.config.ModConfig;
import com.smanzana.musica.util.SoundUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * (Another) music player to centralize what music is playing.
 * Register music tracks that can say when they want to be played and if they want to be looped.
 * Tracks have a priority, and higher priority tracks can decide to play while another is playing
 * -- with a gentle fade in and out while doing so.
 * 
 * Tracks can be normal background music tracks, or 'overlay' tracks which play on top of (a quieted version)
 * of the normal track. Think battle music.
 *
 * This should be a different mod.
 * @author Skyler
 *
 */
@SideOnly(Side.CLIENT)
public class MusicPlayer implements ITickable {
	
	protected static final class TrackGroup {
		public final List<IMusicTrack> tracks;
		public final int priority; // remove?
		
		public TrackGroup(int priority) {
			this.tracks = new ArrayList<>();
			this.priority = priority;
		}
	}
	
	private static final class TrackSet {
		public @Nullable TrackGroup group;
		public @Nullable IMusicTrack track;
		public @Nullable MusicSound sound;
		
		public TrackSet() {
			group = null;
			track = null;
			sound = null;
		}
	}
	
	public static final void InitVanilla() {
		for (MusicTicker.MusicType type : MusicTicker.MusicType.values()) {
			final MusicTicker.MusicType myType = type;
			MusicPlayer.instance().registerTrack(new IMusicTrack() {

				private final MusicSound sound = IMusicTrack.SoundFromEvent(myType.getMusicLocation());
				private long delayTill = 0;
				
				{
					ModConfig.registerWatcher(() -> {
						if (ModConfig.shouldDisableVanilla()) {
							MusicPlayer.instance().stopTrack(this);
						}
					});
				}
				
				@Override
				public MusicSound getSound(EntityPlayerSP player) {
					return sound;
				}
				
				@Override
				public boolean shouldPlay(EntityPlayerSP player) {
					return !ModConfig.shouldDisableVanilla()
							&& delayTill < System.currentTimeMillis()
							&& Minecraft.getMinecraft().getAmbientMusicType() == myType;
				}
				
				@Override
				public void onStart(EntityPlayerSP player) {
					delayTill = System.currentTimeMillis()
							+ myType.getMinDelay()
							+ Musica.random.nextInt(myType.getMaxDelay() - myType.getMinDelay());
				}
				
			}, -1);
		}
	}
	
	@SubscribeEvent
	public void onDefaultSoundPlay(PlaySoundEvent event) {
		if (event.getResultSound() != null // not already cancelled
				&& event.getResultSound().getCategory() == SoundCategory.MUSIC
				&& !(event.getResultSound() instanceof MusicSound)
				) {
			// Cancel and make them go through us!
			//System.out.println("Preventing sound event " + event.getResultSound().getSoundLocation());
			event.setResultSound(null);
		}
			
	}
	
	protected static final double FADETO_MS = 1000 * 2; // Time to fade out existing music when changing tracks.
	protected static final double FADEOVERLAY_MS = 1000 * 1; // Time to dim current track when starting or stopping an overlay.

	private static final MusicPlayer INSTANCE = new MusicPlayer();
	
	public static final MusicPlayer instance() {
		return INSTANCE;
	}
	
	protected SortedMap<Integer, TrackGroup> trackGroups;
	protected SortedMap<Integer, TrackGroup> overlayGroups;
	
	private TrackSet currentTrack = new TrackSet(); // Currently playing background track info. May contain null.
	private TrackSet overlayTrack = new TrackSet(); // Track playing on top of current track. May contain null.
	private TrackSet fadeToTrack = new TrackSet(); // Track to fade to. May contain null.
	
	// Fade controls
	private long fadeStartTimeMS = -1; // background track fading. -1 if no fade is happening/
	private long overlayFadeStartTimeMS = -1; // overlay track fading. -1 means no fade. >= 0 means fade in. < -1 means fade out. 
	
	private MusicPlayer() {
		trackGroups = new TreeMap<>();
		overlayGroups = new TreeMap<>();
		
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	/**
	 * Register a new track with the given priority.
	 * A rough, uninforced guide to priority:
	 *   <-1 for only if default ambient music is not playing
	 *   -1 for the equivalent of vanilla's ambient music
	 *   0 for less sporadic but still universal ambient music
	 *   1+ for specialized ambient music (like custom dimension music)
	 * 
	 * If multiple tracks are registered with the same priority, the track picked is randomized
	 * every time a new track is being selected.
	 * 
	 * Note overlays can play on top of these tracks (after they're quickly faded out) but the
	 * tracks will continue and resume afterwards.
	 * 
	 * For overlay tracks, use {@link #registerOverlay}.
	 * @param track
	 * @param priority
	 */
	public void registerTrack(IMusicTrack track, int priority) {
		TrackGroup group = trackGroups.get(-priority);
		if (group == null) {
			group = new TrackGroup(priority);
			trackGroups.put(-priority, group); // Negative to make forward sorting go from high to low
		}
		group.tracks.add(track);
	}
	
	/**
	 * Register a new track with the given priority as an overlay.
	 * Overlays can play while a regular track is still playing. This works well for
	 * tracks that want to play quickly and go away quickly given a situation -- like battle
	 * music.
	 * 
	 * If multiple tracks are registered with the same priority, the track picked is randomized
	 * every time a new track is being selected.
	 * @param track
	 * @param priority
	 */
	public void registerOverlay(IMusicTrack track, int priority) {
		TrackGroup group = overlayGroups.get(-priority);
		if (group == null) {
			group = new TrackGroup(priority);
			overlayGroups.put(-priority, group); // Negative to make forward sorting go from high to low
		}
		group.tracks.add(track);
	}
	
	/**
	 * Removes all entries for the given track.
	 * @param track
	 */
	public void unregisterTrack(IMusicTrack track) {
		Iterator<Entry<Integer, TrackGroup>> it = trackGroups.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<Integer, TrackGroup> entry = it.next();
			entry.getValue().tracks.removeIf((r) -> {return r == null || r.equals(track);});
			
			// Remove now-entry groups
			if (entry.getValue().tracks.isEmpty()) {
				it.remove();
			}
		}
		
		stopTrack(track);
	}
	
	public void unregisterOverlay(IMusicTrack track) {
		Iterator<Entry<Integer, TrackGroup>> it = overlayGroups.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<Integer, TrackGroup> entry = it.next();
			entry.getValue().tracks.removeIf((r) -> {return r == null || r.equals(track);});
			
			// Remove now-entry groups
			if (entry.getValue().tracks.isEmpty()) {
				it.remove();
			}
		}

		stopTrack(track);
	}
	
	/**
	 * Stops a track, if it's playing.
	 * @param track
	 */
	public void stopTrack(IMusicTrack track) {
		final EntityPlayerSP player = (EntityPlayerSP) Musica.proxy.getPlayer();
		if (track == currentTrack.track) {
			clearTrackInternal(player);
		} else if (track == overlayTrack.track) {
			fadeOutOverlay(player);
		}
	}
	
	public @Nullable IMusicTrack getCurrentTrack() {
		return currentTrack.track;
	}
	
	protected @Nullable TrackGroup getCurrentGroup() {
		return currentTrack.group;
	}
	
	public @Nullable IMusicTrack getCurrentOverlayTrack() {
		return overlayTrack.track;
	}
	
	protected @Nullable TrackGroup getCurrentOverlayGroup() {
		return overlayTrack.group;
	}
	
	public boolean isPlaying(@Nullable IMusicTrack track) {
		if (track == null) {
			return getCurrentTrack() == null && getCurrentOverlayTrack() == null;
		} else {
			return track.equals(getCurrentTrack()) || track.equals(getCurrentOverlayTrack());
		}
	}
	
	/**
	 * Just sets variables, and does no fading or stopping/starting.
	 * @param group
	 * @param track
	 */
	protected void setTrackInternal(EntityPlayerSP player, @Nullable TrackGroup group, @Nullable IMusicTrack track, @Nullable MusicSound sound) {
		Validate.isTrue((group == null) == (track == null));
		currentTrack.track = track;
		currentTrack.group = group;
		currentTrack.sound = sound;
	}
	
	protected void clearTrackInternal(EntityPlayerSP player) {
		stopTrack(player, currentTrack.track, currentTrack.sound);
		setTrackInternal(player, null, null, null);
	}
	
	/**
	 * Just sets variables, and does no fading or stopping/starting.
	 * @param group
	 * @param track
	 */
	protected void setOverlayTrackInternal(@Nullable TrackGroup group, @Nullable IMusicTrack track, @Nullable MusicSound sound) {
		Validate.isTrue((group == null) == (track == null));
		overlayTrack.track = track;
		overlayTrack.group = group;
		overlayTrack.sound = sound;
	}
	
	protected void clearOverlayTrackInternal(EntityPlayerSP player) {
		stopTrack(player, overlayTrack.track, overlayTrack.sound);
		setOverlayTrackInternal(null, null, null);
	}
	
	protected @Nullable MusicSound startTrack(EntityPlayerSP player, @Nullable IMusicTrack track) {
		final @Nullable MusicSound sound;
		if (track == null) {
			sound = null;;
		} else {
			sound = track.getSound(player);
			Minecraft.getMinecraft().getSoundHandler().playSound(sound);
			track.onStart(player);
		}
		return sound;
	}
	
	protected void stopTrack(EntityPlayerSP player, @Nullable IMusicTrack track, @Nullable MusicSound sound) {
		if (track != null) {
			track.onStop(player);
		}
		if (sound != null) {
			SoundUtil.stopSound(sound);
			sound.setVolume(1f);
		}
	}
	
	/**
	 * Start fading out the old track (if present) and fade in the new track (if present).
	 * Fades old track out before fading new one in.
	 * @param group
	 * @param track
	 */
	protected void fadeInTrack(EntityPlayerSP player, @Nullable TrackGroup group, @Nullable IMusicTrack track) {
		Validate.isTrue((group == null) == (track == null));
		
		// If we would fade in but nothing's playing, just start track
		if (this.currentTrack.sound == null) {
			stopTrack(player, currentTrack.track, currentTrack.sound);
			setAndStartTrack(player, group, track);
			return;
		}
		
		this.fadeToTrack.group = group;
		this.fadeToTrack.track = track;
		if (fadeStartTimeMS == -1 && track != null) {
			fadeStartTimeMS = System.currentTimeMillis(); // Start fading
			// TODO this could check how much time is left and adjust fadeTime if track will be over
			// before fade would stop it. Maybe just set an 'up next' if that's the case.
		}
	}
	
	protected void fadeInOverlay(EntityPlayerSP player, @Nullable TrackGroup group, @Nullable IMusicTrack track) {
		Validate.isTrue((group == null) == (track == null));
		
		if (track == null && overlayTrack.track != null) {
			// Start fading out!
			if (overlayFadeStartTimeMS == -1) {
				overlayFadeStartTimeMS = -System.currentTimeMillis();
			}
		} else if (track != null) {
			// Edge case 1: Currenetly fading to an overlay. Don't reset fade progress.
			if (overlayFadeStartTimeMS == -1) {
				overlayFadeStartTimeMS = System.currentTimeMillis();
			}
			
			// Edge case 2: new overlay while existing overlay is playing! Just cut to new.
			if (overlayTrack.sound != null) {
				stopTrack(player, overlayTrack.track, overlayTrack.sound);
			}
			
			setOverlayTrackInternal(group, track, startTrack(player, track));
		}
	}
	
	/**
	 * Returns if fading is still occuring
	 * @param player
	 * @return
	 */
	protected void fadeOutOverlay(EntityPlayerSP player) {
		fadeInOverlay(player, null, null);
	}
	
	protected void setAndStartTrack(EntityPlayerSP player, @Nullable TrackGroup group, @Nullable IMusicTrack track) {
		Validate.isTrue((group == null) == (track == null));
		
		this.setTrackInternal(player, group, track, startTrack(player, track));
	}
	
	/**
	 * Tick fading logic for background track. If no fade is present (or it just completed), returns false.
	 * Else fading is still happening, and regular track selection shouldn't happen.
	 * @return
	 */
	private boolean tickBackgroundFade(EntityPlayerSP player) {
		if (fadeStartTimeMS != -1) {
			final long now = System.currentTimeMillis();
			final double fadeProgress;
			if (currentTrack.sound == null) {
				fadeProgress = 1.0;
			} else {
				fadeProgress = (double) (now - fadeStartTimeMS) / FADETO_MS;
			}
			
			if (fadeProgress >= 1.0) {
				// Start fade track and clear fade data
				clearTrackInternal(player);
				setAndStartTrack(player, overlayTrack.group, overlayTrack.track);
				overlayTrack.group = null;
				overlayTrack.track = null;
				fadeStartTimeMS = -1;
			} else {
				// Fade out current track until fade is complete. Just linear fade.
				final float volume = (float) (1 - fadeProgress);
				currentTrack.sound.setVolume(volume);
			}
		}
		
		// Return if, after above, fade is still happening or not
		return fadeStartTimeMS != -1;
	}
	
	/**
	 * Tick fading logic for overlay fade in and out. Should always happen.
	 * @param player
	 */
	private boolean tickOverlayFade(EntityPlayerSP player) {
		final float volume;
		if (overlayFadeStartTimeMS != -1) {
			final long now = System.currentTimeMillis();
			final double fadeProgress = (double) (now - Math.abs(overlayFadeStartTimeMS)) / FADEOVERLAY_MS;
			final boolean fadeIn = overlayFadeStartTimeMS > -1; // use negatives to encode fade out
			
			// Overlay sound effect set up when fade is started, so just adjust volume or kill
			volume = (float) (fadeIn ? fadeProgress : 1.0 - fadeProgress);
			overlayTrack.sound.setVolume(volume);
			
			if (fadeProgress >= 1.0) {
				// If fading out, remove sound.
				// Regardless, reset time.
				overlayFadeStartTimeMS = -1;
				if (!fadeIn) {
					stopTrack(player, overlayTrack.track, overlayTrack.sound);
					clearOverlayTrackInternal(player);
				}
			}
		} else if (overlayTrack.sound != null) {
			volume = 1f; // Fully faded
		} else {
			volume = 0f; // Not playing
		}
		if (currentTrack.sound != null) {
			currentTrack.sound.setVolume(1f - (volume * .98f));
		}
		return false; // Always look at other tracks and allow overriding even while fading
	}
	
	private void updateSubtrack(EntityPlayerSP player, boolean overlay) {
		final TrackSet currentSet = overlay ? overlayTrack : currentTrack;
		final SortedMap<Integer, TrackGroup> groupMap = overlay ? overlayGroups : trackGroups;
		
		
		// Check if we're fading still and do that before anything else.
		if (overlay ? tickOverlayFade(player) : tickBackgroundFade(player)) {
			// Only do fade for now.
		} else {
			// Give things at a higher priority than what's playing a chance to jump in
			int currentIntPriority = (currentSet.group == null ? Integer.MAX_VALUE : -currentSet.group.priority);
			for (TrackGroup group : groupMap.headMap(currentIntPriority).values()) {
				Collections.shuffle(group.tracks);
				for (IMusicTrack track : group.tracks) {
					if (track.shouldPlayOverride(player)) {
						// Track has indicated it wants to hop in right now. Fade it in,
						// and do so fast.
						if (overlay) {
							fadeInOverlay(player, group, track);
						} else {
							fadeInTrack(player, group, track);
						}
						return;
					}
				}
			}
			
			// Overlays want to fade out as soon as they stop being enabled.
			if (overlay && currentSet.sound != null) {
				if (!currentSet.track.shouldPlay(player)) {
					fadeOutOverlay(player);
					return;
				}
			}
			
			// No higher priority thing tried to preempt. Check if current track is done.
			if (currentSet.sound == null || !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(currentSet.sound)) {
				
				// If a sound just finished, check if it wants to loop
				if (currentSet.track != null && currentSet.track.shouldLoop(player)) {
					// Restart sound
					if (overlay) {
						//fadeInOverlay(player, currentSet.group, currentSet.track);
						setOverlayTrackInternal(currentSet.group, currentSet.track, startTrack(player, currentSet.track));
					} else {
						setAndStartTrack(player, currentSet.group, currentSet.track);
					}
					return;
				}
				
				if (currentSet.track != null) {
					if (overlay) {
						fadeOutOverlay(player); // overlay fade clears out track already
					} else {
						clearTrackInternal(player);
					}
				}
				
				// Find a new sound to play!
				for (TrackGroup group : groupMap.values()) {
					Collections.shuffle(group.tracks);
					for (IMusicTrack track : group.tracks) {
						if (track.shouldPlay(player)) {
							// Track can play, so start it playing.
							if (overlay) {
								fadeInOverlay(player, group, track);
							} else {
								setAndStartTrack(player, group, track);
							}
							return;
						}
					}
				}
			}
		}
	}
	
	public void update() {
		EntityPlayerSP player = (EntityPlayerSP) Musica.proxy.getPlayer();
		
		// Disable everything if music volume is 0%
		if (Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MUSIC) <= 0f) {
			if (this.currentTrack.sound != null) {
				this.clearTrackInternal(player);
			}
			if (this.overlayTrack.sound != null) {
				this.clearOverlayTrackInternal(player);
			}
			return;
		}

		updateSubtrack(player, false);
		updateSubtrack(player, true);
		
//		// Always unconditionally tick overlay fading
//		tickOverlayFade(player);
//		
//		// Check if we're fading still and do that before anything else.
//		if (tickBackgroundFade(player)) {
//			// Only do fade for now.
//		} else {
//			// Give things at a higher priority than what's playing a chance to jump in
//			int currentIntPriority = (currentTrack.group == null ? Integer.MAX_VALUE : -currentTrack.group.priority);
//			for (TrackGroup group : trackGroups.headMap(currentIntPriority).values()) {
//				Collections.shuffle(group.tracks);
//				for (IMusicTrack track : group.tracks) {
//					if (track.shouldPlayOverride(player)) {
//						// Track has indicated it wants to hop in right now. Fade it in,
//						// and do so fast.
//						fadeInTrack(player, group, track);
//						return;
//					}
//				}
//			}
//			
//			// No higher priority thing tried to preempt. Check if current track is done.
//			if (currentTrack.sound == null || !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(currentTrack.sound)) {
//				
//				// If a sound just finished, check if it wants to loop
//				if (currentTrack.track != null && currentTrack.track.shouldLoop(player)) {
//					// Restart sound
//					setAndStartTrack(player, currentTrack.group, currentTrack.track);
//					return;
//				}
//				
//				if (currentTrack.track != null) {
//					clearTrackInternal(player);
//				}
//				
//				// Find a new sound to play!
//				for (TrackGroup group : trackGroups.values()) {
//					Collections.shuffle(group.tracks);
//					for (IMusicTrack track : group.tracks) {
//						if (track.shouldPlay(player)) {
//							// Track can play, so start it playing.
//							setAndStartTrack(player, group, track);
//							return;
//						}
//					}
//				}
//			}
//		}
	}
	
}
