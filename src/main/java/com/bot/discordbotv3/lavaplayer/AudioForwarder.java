package com.bot.discordbotv3.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;


import java.nio.ByteBuffer;

public class AudioForwarder implements AudioSendHandler {

    private final AudioPlayer player;
    private final Guild guild;
    private final ByteBuffer buffer = ByteBuffer.allocate(8192);
    private final MutableAudioFrame frame = new MutableAudioFrame();
    private int time;
    private boolean isBuffering = true;
    private int bufferFrames = 0;

    public AudioForwarder(AudioPlayer player, Guild guild) {
        this.player = player;
        this.guild = guild;
        frame.setBuffer(buffer);
    }

    @Override
    public boolean canProvide() {
        boolean canProvide = player.provide(frame);
        // Buffering logic to prevent initial audio cuts
        if (isBuffering) {
            if (canProvide) {
                bufferFrames++;
                if (bufferFrames >= 10) {  // Buffer 10 frames (200ms) before starting
                    isBuffering = false;
                    time = 0;
                    return true;
                }
            }
            return false;  // Don't provide audio while buffering
        }
        // Normal operation
        if (!canProvide) {
            time += 20;
            if (time >= 300000) {  // 5 minutes of silence
                time = 0;
                guild.getAudioManager().closeAudioConnection();
            }
        } else {
            time = 0;
        }

        return canProvide;

    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return buffer.flip();
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
