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
import dev.lavalink.youtube.clients.skeleton.Client;
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
    private final AudioPlayerManager playerManager;

    private final String ytdlpPath;

    private PlayerManager() {
        this.ytdlpPath = System.getenv().getOrDefault("YTDLP_PATH", "yt-dlp");
        this.playerManager = createPlayerManager();
    }

    private AudioPlayerManager createPlayerManager() {
        String cipherURL = isApiAvailable();
        String potURL = isPotApiAvailable();

        YoutubeSourceOptions sourceOptions = new YoutubeSourceOptions()
                .setAllowSearch(true)
                .setRemoteCipher(cipherURL, "", "");

        if (potURL != null) {
            sourceOptions.setRemotePoToken(potURL, null);
            log.info("poToken generation enabled via: {}", potURL);
        }

        AudioPlayerManager manager = new DefaultAudioPlayerManager();

        Client[] ytClients = new Client[]{
                new MusicWithThumbnail(),
                new WebWithThumbnail(),
                new WebEmbeddedWithThumbnail()
        };

        manager.registerSourceManager(new YtDlpLiveSourceManager(ytdlpPath));
        manager.registerSourceManager(new YoutubeAudioSourceManager(sourceOptions, ytClients));
        AudioSourceManagers.registerRemoteSources(manager);
        AudioSourceManagers.registerLocalSource(manager);

        log.info("Player manager initialized | cipher: {} | yt-dlp: {}", cipherURL, ytdlpPath);
        return manager;
    }

    private String isApiAvailable() {
        String localApiUrl = System.getenv().getOrDefault("CIPHER_URL", "http://yt-cipher:8001");
        String publicApiUrl = "https://cipher.kikkia.dev/api";

        try {
            URL url = new URL(localApiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(200);
            connection.setReadTimeout(200);
            connection.getResponseCode();
            connection.disconnect();

            log.info("Local cipher API available at {}", localApiUrl);
            return localApiUrl;
        } catch (Exception e) {
            log.warn("Local cipher unreachable: {}. Falling back to public cipher", e.getMessage());
            return publicApiUrl;
        }
    }
    //Temporary until official fix (poToken generation running for videos only)
    private String isPotApiAvailable() {
        String localApiUrl = System.getenv().getOrDefault("POT_URL", "http://webpo-generator:8090");

        try {
            URL url = new URL(localApiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(200);
            connection.setReadTimeout(200);
            connection.getResponseCode();
            connection.disconnect();

            log.info("WebPO generator available at {}", localApiUrl);
            return localApiUrl;
        } catch (Exception e) {
            log.warn("WebPO generator unreachable: {}. poToken generation disabled", e.getMessage());
            return null;
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
            GuildMusicManager musicManager = new GuildMusicManager(playerManager, guild);
            guild.getAudioManager().setSendingHandler(musicManager.getAudioForwarder());
            log.info("Created new music manager for guild: {} ({})", guild.getName(), guild.getId());
            return musicManager;
        });
    }

    public void play(Guild guild, String trackURL, InteractionHook hook) {
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        log.info("Play requested in guild {} | URL: {}", guild.getName(), trackURL);
        new VoiceChannelManager().startDisconnectTimer(guild);
        loadAndPlay(guildMusicManager, trackURL, hook);
    }

    private void loadAndPlay(GuildMusicManager musicManager, String trackURL, InteractionHook hook) {
        playerManager.loadItemOrdered(musicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                AudioTrackInfo info = track.getInfo();
                log.info("Track loaded | Title: {} | Author: {} | Source: {} | Duration: {}ms | Stream: {}",
                        info.title, info.author, track.getSourceManager().getSourceName(), info.length, info.isStream);
                handleSingleTrack(track, musicManager, hook);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                log.info("Playlist loaded | Name: {} | Tracks: {} | Search: {}",
                        playlist.getName(), playlist.getTracks().size(), playlist.isSearchResult());
                handlePlaylist(playlist, musicManager, hook);
            }

            @Override
            public void noMatches() {
                log.warn("No matches found for URL: {}", trackURL);
                hook.editOriginal("🔍 No matches found. Try a different search term.").queue(
                        success -> {},
                        error -> hook.sendMessage("🔍 No matches found").setEphemeral(true).queue()
                );
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                log.error("Track load failed | URL: {} | Severity: {} | Message: {}",
                        trackURL, exception.severity, exception.getMessage(), exception);

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
        AudioTrackInfo info = track.getInfo();

        if (info.isStream && !(track instanceof YtDlpLiveAudioTrack)) {
            log.info("Live stream detected, re-routing to yt-dlp pipeline | Title: {}", info.title);
            String ytdlpId = YtDlpLiveSourceManager.buildIdentifier(info);
            playerManager.loadItemOrdered(musicManager, ytdlpId, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack ytdlpTrack) {
                    musicManager.getTrackScheduler().queue(ytdlpTrack);
                    boolean isQueueEmpty = musicManager.getTrackScheduler().getQueue().isEmpty();
                    AudioTrackEmbed.audioTrackEmbedBuilder(info, hook, isQueueEmpty,
                            musicManager.getTrackScheduler().getQueue().size());
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {}

                @Override
                public void noMatches() {
                    log.warn("yt-dlp failed to load live stream, falling back to default");
                    musicManager.getTrackScheduler().queue(track);
                    boolean isQueueEmpty = musicManager.getTrackScheduler().getQueue().isEmpty();
                    AudioTrackEmbed.audioTrackEmbedBuilder(info, hook, isQueueEmpty,
                            musicManager.getTrackScheduler().getQueue().size());
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    log.warn("yt-dlp load failed, falling back to default: {}", exception.getMessage());
                    musicManager.getTrackScheduler().queue(track);
                    boolean isQueueEmpty = musicManager.getTrackScheduler().getQueue().isEmpty();
                    AudioTrackEmbed.audioTrackEmbedBuilder(info, hook, isQueueEmpty,
                            musicManager.getTrackScheduler().getQueue().size());
                }
            });
            return;
        }

        musicManager.getTrackScheduler().queue(track);
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
            for (AudioTrack track : tracks) {
                musicManager.getTrackScheduler().queue(track);
            }
            log.info("Queued {} tracks from playlist", tracks.size());
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
