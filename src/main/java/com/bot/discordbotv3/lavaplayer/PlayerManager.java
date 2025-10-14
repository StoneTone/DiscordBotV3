package com.bot.discordbotv3.lavaplayer;

import com.bot.discordbotv3.embed.AudioPlaylistEmbed;
import com.bot.discordbotv3.embed.AudioTrackEmbed;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
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
import dev.lavalink.youtube.clients.AndroidVrWithThumbnail;
import dev.lavalink.youtube.clients.WebEmbeddedWithThumbnail;
import dev.lavalink.youtube.clients.WebWithThumbnail;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {
    private static final Logger log = LoggerFactory.getLogger(PlayerManager.class);
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
        playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        playerManager.getConfiguration().setOpusEncodingQuality(10);
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        return playerManager;
    }

    private AudioPlayerManager createYoutubePlayerManager() {
        String cipherURL = isApiAvailable();
        YoutubeSourceOptions sourceOptions = new YoutubeSourceOptions()
                .setAllowSearch(true)
                .setRemoteCipher(cipherURL, "","");
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        playerManager.getConfiguration().setOpusEncodingQuality(10);
        playerManager.registerSourceManager(new YoutubeAudioSourceManager(sourceOptions, new WebEmbeddedWithThumbnail(), new WebWithThumbnail(), new AndroidVrWithThumbnail()));
        AudioSourceManagers.registerLocalSource(playerManager);
        return playerManager;
    }

    private String isApiAvailable() {
        String localApiUrl = "http://yt-cipher:8001";
        String publicApiUrl = "https://cipher.kikkia.dev/api";

        try {
            URL url = new URL(localApiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(200);
            connection.setReadTimeout(200);

            connection.getResponseCode();
            connection.disconnect();

            return localApiUrl;
        } catch (Exception e) {
            // Only timeouts and connection refused will throw exceptions
            log.warn("API Unreachable: {}", e.getMessage());
            log.info("Defaulting to Public Cipher");
            return publicApiUrl;
        }
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
        return url.contains("youtube") || url.contains("youtu.be") || url.startsWith("ytsearch:") || url.startsWith("ytmsearch:");
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
                hook.editOriginal("🔍 No matches found. Try a different search term.").queue(
                    success -> {},
                    error -> hook.sendMessage("🔍 No matches found").setEphemeral(true).queue()
                );
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                String errorMessage = switch (exception.severity) {
                    case COMMON -> "Track unavailable. Please try a different song.";
                    case SUSPICIOUS -> "Unable to load track due to rate limiting. Try again later.";
                    case FAULT -> "YouTube service error. Please try again.";
                };
                
                hook.editOriginal("❌ " + errorMessage).queue(
                    success -> {},
                    error -> hook.sendMessage("❌ Failed to load track").setEphemeral(true).queue()
                );
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

        if (playlist.isSearchResult()) {
            boolean isQueueEmpty = musicManager.getTrackScheduler().getQueue().isEmpty();
            AudioTrackEmbed.audioTrackEmbedBuilder(info, hook, isQueueEmpty,
                    musicManager.getTrackScheduler().getQueue().size());
        } else {
            List<AudioTrack> tracks = new ArrayList<>(playlist.getTracks());
            AudioPlaylistEmbed.audioPlaylistEmbedBuilder(info, hook, tracks.size());
            tracks.remove(0);
            for(AudioTrack track : tracks){
                musicManager.getTrackScheduler().queue(track);
            }
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
