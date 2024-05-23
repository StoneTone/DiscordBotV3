package com.bot.discordbotv3.cmds;

import com.bot.discordbotv3.lavaplayer.PlayerManager;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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


        for(OptionMapping options : event.getOptions()){
            if(options.getName().equals("search")){
                event.deferReply().queue(hook -> {
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
                            PlayerManager playerManager = PlayerManager.get();
                            playerManager.play(event.getGuild(), videoUrl, hook);

                        }else{
                            event.reply("No search results found for: " + options.getName()).setEphemeral(true).queue();
                        }
                    }catch(IOException e){
                        logger.error("Error with search on YouTube: " + e);
                    }
                });
            }
            if(options.getName().equals("url")) {
                event.deferReply().queue(hook -> {
                    PlayerManager playerManager = PlayerManager.get();
                    playerManager.play(event.getGuild(), event.getOption("url").getAsString(), hook);
                });
            }
        }
    }
}
