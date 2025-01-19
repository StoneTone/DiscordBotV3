package com.bot.discordbotv3.cmds;

import com.bot.discordbotv3.options.TwitchChannelOptions;
import com.bot.discordbotv3.service.TwitchService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class TwitchConfigCommand {
    private static final Logger logger = LoggerFactory.getLogger(TwitchConfigCommand.class);

    public static void handleTwitchConfigCommand(SlashCommandInteractionEvent event, TwitchService twitchService) {

        if(event.getInteraction().getMember().isOwner()){
            String channel = event.getOption("channel").getAsString();

            if(event.getOption("edit") != null && event.getOption("remove") != null){
                event.reply("You cannot edit and remove at the same time!").setEphemeral(true).queue();
            }
            else if(event.getOption("edit") != null){
                for(Map.Entry<String, Map<String, String>> entry : twitchService.channelMap.entrySet()){
                    for(Map.Entry<String, String> channelEntry : entry.getValue().entrySet()){
                        if(channelEntry.getKey().equals(channel)){
                            String msgEdit = event.getOption("edit").getAsString();
                            entry.getValue().put(channel, msgEdit);
                            event.reply("Modified notification message!").setEphemeral(true).queue();
                            break;
                        }
                    }
                }
            }
            else if(event.getOption("remove") != null && event.getOption("remove").getAsString().equals("yes")){
                twitchService.stopListeningToChannel(channel);
                event.reply("Unsubscribed from Twitch Channel: " + channel).setEphemeral(true).queue();

                event.getGuild().upsertCommand(
                        Commands.slash("twitchconfig", "Configure Twitch notifications")
                                .addOptions(TwitchChannelOptions.handleTwitchChannelOptions(twitchService))
                                .addOption(OptionType.STRING, "edit", "Edit notification message")
                                .addOptions(new OptionData(OptionType.STRING, "remove", "Remove notifications for channel")
                                        .addChoice("Yes", "yes")
                                        .addChoice("No", "no")
                                )
                ).queue();
            }else if(event.getOption("remove") != null && event.getOption("remove").getAsString().equals("no")){
                event.reply("Cancelled removal of Twitch Channel: " + channel).setEphemeral(true).queue();
            }
            else{
                event.reply("You must enter at least 1 option!").setEphemeral(true).queue();
            }
        }else{
            event.reply("You do not have permission to use this command!").setEphemeral(true).queue();
        }
    }
}
