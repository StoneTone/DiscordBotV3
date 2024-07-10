package com.bot.discordbotv3.lavaplayer;

import com.bot.discordbotv3.embed.AudioPlaylistEmbed;
import com.bot.discordbotv3.embed.AudioTrackEmbed;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {

    private static PlayerManager INSTANCE;
    private Map<Long, GuildMusicManager> guildMusicManagers = new HashMap<>();
    private AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();

    private PlayerManager() {
//        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
//        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        audioPlayerManager.registerSourceManager(new dev.lavalink.youtube.YoutubeAudioSourceManager());
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }

    public static PlayerManager get() {
        if(INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return guildMusicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            GuildMusicManager musicManager = new GuildMusicManager(audioPlayerManager, guild);

            guild.getAudioManager().setSendingHandler(musicManager.getAudioForwarder());

            return musicManager;
        });
    }

    public void play(Guild guild, String trackURL, InteractionHook hook) {
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        VoiceChannelManager voiceChannelManager = new VoiceChannelManager();
        voiceChannelManager.startDisconnectTimer(guild);
        audioPlayerManager.loadItemOrdered(guildMusicManager, trackURL, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                guildMusicManager.getTrackScheduler().queue(track);
                AudioTrackInfo info = track.getInfo();
                if(guildMusicManager.getTrackScheduler().getQueue().isEmpty()){
                    AudioTrackEmbed.audioTrackEmbedBuilder(info, hook, true, guildMusicManager.getTrackScheduler().getQueue().size());
                }else{
                    AudioTrackEmbed.audioTrackEmbedBuilder(info, hook, false, guildMusicManager.getTrackScheduler().getQueue().size());
                }

            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getTracks().get(0);
                guildMusicManager.getTrackScheduler().queue(firstTrack);
                AudioTrackInfo info = firstTrack.getInfo();

                // Queue the remaining tracks
                List<AudioTrack> tracks = new ArrayList<>(playlist.getTracks());
                AudioPlaylistEmbed.audioPlaylistEmbedBuilder(info, hook, tracks.size());
                tracks.remove(0);
                for(AudioTrack track : tracks){
                    guildMusicManager.getTrackScheduler().queue(track);
                }
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

    public boolean isPaused(Guild guild){
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        AudioPlayer audioPlayer = guildMusicManager.getTrackScheduler().getPlayer();
        return audioPlayer.isPaused();
    }

    public void pause(Guild guild){
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        AudioPlayer audioPlayer = guildMusicManager.getTrackScheduler().getPlayer();
        audioPlayer.setPaused(true);
    }

    public void unpause(Guild guild){
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        AudioPlayer audioPlayer = guildMusicManager.getTrackScheduler().getPlayer();
        audioPlayer.setPaused(false);
    }
}
