package com.bot.discordbotv3.lavaplayer;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VoiceChannelManager {
    private static final Logger logger = LoggerFactory.getLogger(VoiceChannelManager.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final long DISCONNECT_DELAY = 10;

    public void startDisconnectTimer(Guild guild) {
        scheduler.scheduleAtFixedRate(() -> {
            checkAndDisconnect(guild);
        }, 1, 1, TimeUnit.MINUTES);
    }

    private void checkAndDisconnect(Guild guild) {
        Member selfMember = guild.getSelfMember();
        GuildVoiceState voiceState = selfMember.getVoiceState();

        if (voiceState != null && voiceState.inAudioChannel()) {
            AudioChannel voiceChannel = voiceState.getChannel();
            List<Member> members = voiceChannel.getMembers();

            if (members.size() == 1 && members.get(0).equals(selfMember)) {
                // Bot is alone in the voice channel
                disconnectAfterDelay(guild);
            }
        }
    }

    private void disconnectAfterDelay(Guild guild) {
        scheduler.schedule(() -> {
            AudioManager audioManager = guild.getAudioManager();
            if (audioManager.isConnected()) {
                audioManager.closeAudioConnection();
                logger.info("Bot disconnected from voice due to inactivity");
            }
        }, DISCONNECT_DELAY, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
