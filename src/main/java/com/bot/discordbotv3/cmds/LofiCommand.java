package com.bot.discordbotv3.cmds;

import com.bot.discordbotv3.lavaplayer.GuildMusicManager;
import com.bot.discordbotv3.lavaplayer.PlayerManager;
import com.bot.discordbotv3.lavaplayer.TrackScheduler;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class LofiCommand {
    private static final Logger logger = LoggerFactory.getLogger(LofiCommand.class);
    public static void handleLofiCommand(SlashCommandInteractionEvent event, String ytSecret, String option){
        Member member = event.getMember();
        GuildVoiceState memberVoiceState = member.getVoiceState();

        if (!memberVoiceState.inAudioChannel()) {
            event.reply("You need to be in a voice channel!").setEphemeral(true).queue();
            return;
        }

        Member self = event.getGuild().getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if (!selfVoiceState.inAudioChannel()) {
            //Join VC
            event.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
        } else {
            if (selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
                event.reply("You need to be in the same channel as me").setEphemeral(true).queue();
                return;
            }
        }
        //clear the queue and change the song
        GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
        TrackScheduler trackScheduler = guildMusicManager.getTrackScheduler();
        trackScheduler.getQueue().clear();
        trackScheduler.getPlayer().stopTrack();

        Document document = null;

        try{
            YouTube youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), null)
                    .setApplicationName("Discord Bot")
                    .build();

            YouTube.Search.List search = youTube.search().list("id, snippet");
            search.setKey(ytSecret);
            search.setQ("Lofi girl " + option);
            search.setType("video");
            search.setMaxResults(1L);

            SearchListResponse response = search.execute();
            List<SearchResult> results = response.getItems();

            if(results != null && !results.isEmpty()){
                String videoId = results.get(0).getId().getVideoId();
                String videoUrl = "https://youtu.be/" + videoId;

                document = Jsoup.connect(videoUrl).get();
                String title = document.title().replaceAll(" - YouTube$", "");

                PlayerManager playerManager = PlayerManager.get();
                playerManager.play(event.getGuild(), videoUrl);

                event.reply("Playing: " + title).setEphemeral(true).queue();
            }else{
                event.reply("No search results found for Lofi girl " + option).setEphemeral(true).queue();
            }
        }catch(IOException e){
            logger.error("Error with search on YouTube: " + e);
        }
    }
}
