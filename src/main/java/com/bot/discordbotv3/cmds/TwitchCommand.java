package com.bot.discordbotv3.cmds;

import com.bot.discordbotv3.options.TwitchChannelOptions;
import com.bot.discordbotv3.service.TwitchService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class TwitchCommand {
    public static void handleTwitchCommand(SlashCommandInteractionEvent event, TwitchService twitchService) {
        if(event.getInteraction().getMember().isOwner()){
            String channelName = event.getOption("channel").getAsString();
            String textChannelId = event.getOption("textchannel").getAsString();
            String message = event.getOption("message").getAsString();
            twitchService.listenToChannel(channelName, textChannelId, message);

            event.reply("Now listening to Twitch channel: " + channelName).setEphemeral(true).queue();

            event.getGuild().upsertCommand(
                    Commands.slash("twitchconfig", "Configure Twitch notifications")
                            .addOptions(TwitchChannelOptions.handleTwitchChannelOptions(twitchService))
                            .addOption(OptionType.STRING, "edit", "Edit notification message")
                            .addOptions(new OptionData(OptionType.STRING, "remove", "Remove notifications for channel")
                                    .addChoice("Yes", "yes")
                                    .addChoice("No", "no")
                            )
            ).queue();

        }else{
            event.reply("You do not have permission to use this command!").setEphemeral(true).queue();
        }
    }
}
