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
            event.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
        } else {
            if (selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
                event.reply("You need to be in the same channel as me").setEphemeral(true).queue();
                return;
            }
        }

        OptionMapping searchOption = event.getOption("search");
        OptionMapping urlOption = event.getOption("url");

        if (searchOption == null && urlOption == null) {
            event.reply("You need to provide a search term or URL!").setEphemeral(true).queue();
            return;
        }

        String query;
        if (urlOption != null) {
            String url = urlOption.getAsString().trim();
            if (url.isEmpty()) {
                event.reply("URL cannot be empty!").setEphemeral(true).queue();
                return;
            }
            query = url;
        } else {
            String search = searchOption.getAsString().trim();
            if (search.isEmpty()) {
                event.reply("Search term cannot be empty!").setEphemeral(true).queue();
                return;
            }
            query = "ytsearch:" + search;
        }

        event.deferReply().queue(hook -> {
            try {
                PlayerManager.get().play(event.getGuild(), query, hook);
            } catch (Exception e) {
                logger.error("Error playing track | Query: {}", query, e);
                hook.editOriginal("❌ Something went wrong trying to play that track.").queue();
            }
        });
    }
}
