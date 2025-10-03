package com.bot.discordbotv3.lavaplayer;

import com.bot.discordbotv3.embed.AudioPlaylistEmbed;
import com.bot.discordbotv3.embed.AudioTrackEmbed;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.YoutubeSourceOptions;
import dev.lavalink.youtube.clients.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> guildMusicManagers = new HashMap<>();
    private final AudioPlayerManager defaultPlayerManager;
    private final AudioPlayerManager youtubePlayerManager;

    private PlayerManager() {
        this.defaultPlayerManager = createDefaultPlayerManager();
        this.youtubePlayerManager = createYoutubePlayerManager();
    }

    private AudioPlayerManager createDefaultPlayerManager() {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        return playerManager;
    }

    private AudioPlayerManager createYoutubePlayerManager() {
        YoutubeSourceOptions sourceOptions = new YoutubeSourceOptions()
                .setRemoteCipherUrl("https://cipher.kikkia.dev/api", "");
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new YoutubeAudioSourceManager(sourceOptions, new WebEmbedded(), new Web()));
        AudioSourceManagers.registerLocalSource(playerManager);
        return playerManager;
    }

    public static PlayerManager get() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return guildMusicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            GuildMusicManager musicManager = new GuildMusicManager(defaultPlayerManager, guild);
            guild.getAudioManager().setSendingHandler(musicManager.getAudioForwarder());
            return musicManager;
        });
    }

    public void play(Guild guild, String trackURL, InteractionHook hook) {
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        AudioPlayerManager selectedManager = isYoutubeURL(trackURL) ? youtubePlayerManager : defaultPlayerManager;

        new VoiceChannelManager().startDisconnectTimer(guild);
        loadAndPlay(selectedManager, guildMusicManager, trackURL, hook);
    }

    private boolean isYoutubeURL(String url) {
        return url.contains("youtube") || url.contains("youtu.be");
    }

    private void loadAndPlay(AudioPlayerManager playerManager, GuildMusicManager musicManager, String trackURL, InteractionHook hook) {
        playerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                handleSingleTrack(track, musicManager, hook);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                handlePlaylist(playlist, musicManager, hook);
            }

            @Override
            public void noMatches() {
                hook.deleteOriginal().and(hook.sendMessage("No matches found for track, Please try again").setEphemeral(true)).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                hook.deleteOriginal().and(hook.sendMessage("Failed to load track, Please try again").setEphemeral(true)).queue();
            }
        });
    }

    private void handleSingleTrack(AudioTrack track, GuildMusicManager musicManager, InteractionHook hook) {
        musicManager.getTrackScheduler().queue(track);
        AudioTrackInfo info = track.getInfo();
        boolean isQueueEmpty = musicManager.getTrackScheduler().getQueue().isEmpty();
        AudioTrackEmbed.audioTrackEmbedBuilder(info, hook, isQueueEmpty,
                musicManager.getTrackScheduler().getQueue().size());
    }

    private void handlePlaylist(AudioPlaylist playlist, GuildMusicManager musicManager, InteractionHook hook) {
        AudioTrack firstTrack = playlist.getTracks().get(0);
        musicManager.getTrackScheduler().queue(firstTrack);
        AudioTrackInfo info = firstTrack.getInfo();

        // Queue the remaining tracks
        List<AudioTrack> tracks = new ArrayList<>(playlist.getTracks());
        AudioPlaylistEmbed.audioPlaylistEmbedBuilder(info, hook, tracks.size());
        tracks.remove(0);
        for(AudioTrack track : tracks){
            musicManager.getTrackScheduler().queue(track);
        }
    }

    public boolean isPaused(Guild guild) {
        return getGuildMusicManager(guild).getTrackScheduler().getPlayer().isPaused();
    }

    public void pause(Guild guild) {
        getGuildMusicManager(guild).getTrackScheduler().getPlayer().setPaused(true);
    }

    public void unpause(Guild guild) {
        getGuildMusicManager(guild).getTrackScheduler().getPlayer().setPaused(false);
    }
}
