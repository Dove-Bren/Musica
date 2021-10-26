package com.smanzana.musica.music;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class MusicSound extends MovingSound {

	protected MusicSound(SoundEvent soundIn) {
		super(soundIn, SoundCategory.MUSIC);
		
		this.repeat = false;
		this.repeatDelay = 0;
	}

	@Override
	public void update() {
		final EntityPlayerSP player = Minecraft.getMinecraft().player;
		if (player == null) {
			this.xPosF = 0;
			this.yPosF = 0;
			this.zPosF = 0;
		} else {
			this.xPosF = (float) player.posX;
			this.yPosF = (float) (player.posY + 5);
			this.zPosF = (float) player.posZ;
		}
	}
	
	public void setVolume(float volume) {
		this.volume = volume;
	}
	
	public void setPitch(float pitch) {
		this.pitch = pitch;
	}
	
	public void setRepeat(boolean repeat, int delayTicks) {
		this.repeat = repeat;
		this.repeatDelay = delayTicks;
	}

}
