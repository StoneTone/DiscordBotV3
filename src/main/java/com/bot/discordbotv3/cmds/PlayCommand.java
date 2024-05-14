package com.bot.discordbotv3.cmds;

import com.bot.discordbotv3.lavaplayer.GuildMusicManager;
import com.bot.discordbotv3.lavaplayer.PlayerManager;
import com.bot.discordbotv3.lavaplayer.TrackScheduler;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayCommand {

    private static final Logger logger = LoggerFactory.getLogger(PlayCommand.class);

    public static void handlePlayCommand(SlashCommandInteractionEvent event, String ytSecret) {
        Member member = event.getMember();
        GuildVoiceState memberVoiceState = member.getVoiceState();

        if (!memberVoiceState.inAudioChannel()) {
            event.reply("You need to be in a voice channel!").setEphemeral(true).queue();
            return;
        }

        Member self = event.getGuild().getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if (!selfVoiceState.inAudioChannel()) {
            //Join
            event.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
        } else {
            if (selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
                event.reply("You need to be in the same channel as me").setEphemeral(true).queue();
                return;
            }
        }

        Document document = null;
        for(OptionMapping options : event.getOptions()){
            if(options.getName().equals("search")){
                try{
                    YouTube youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), null)
                            .setApplicationName("Discord Bot")
                            .build();

                    YouTube.Search.List search = youTube.search().list("id, snippet");
                    search.setKey(ytSecret);
                    search.setQ(options.getAsString());
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
                        playerManager.play(event.getGuild(), videoUrl, event);

                    }else{
                        event.reply("No search results found for: " + options.getName()).setEphemeral(true).queue();
                    }
                }catch(IOException e){
                    logger.error("Error with search on YouTube: " + e);
                }
                break;
            }
            if(options.getName().equals("url")) {
                try {
                    document = Jsoup.connect(event.getOption("url").getAsString()).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String title = document.title().replaceAll(" - YouTube$", "");

                PlayerManager playerManager = PlayerManager.get();
                playerManager.play(event.getGuild(), event.getOption("url").getAsString(), event);
                event.reply("Playing: " + title).setEphemeral(true).queue();
                break;
            }
        }
    }
}
