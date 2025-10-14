package com.bot.discordbotv3.cmds;

import com.bot.discordbotv3.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PlayCommand {

    private static final Logger logger = LoggerFactory.getLogger(PlayCommand.class);

    public static void handlePlayCommand(SlashCommandInteractionEvent event) {
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
                    String searchQuery = "ytsearch:" + options.getAsString();
                    PlayerManager playerManager = PlayerManager.get();
                    playerManager.play(event.getGuild(), searchQuery, hook);
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
