package com.bot.discordbotv3.cmdmgr;

import com.bot.discordbotv3.options.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandManager {
    private static final Logger logger = LoggerFactory.getLogger(CommandManager.class);
    public static void registerCommands(Guild guild){
        try{
            guild.updateCommands()
                    .addCommands(
                            Commands.slash("rolerequest", "Allows you to request a role")
                                    .addOptions(RoleOptions.handleRoleOptions(guild))
                    )
                    .addCommands(
                            Commands.slash("play", "Will play any song")
                                    .addOption(OptionType.STRING, "url", "Name of the song to play (Any HTTP URL is supported)" )
                                    .addOption(OptionType.STRING, "search", "Search YouTube for a song")

                    )
                    .addCommands(
                            Commands.slash("pause", "Will pause current song")
                    )
                    .addCommands(
                            Commands.slash("unpause", "Will unpause current song")
                    )
                    .addCommands(
                            Commands.slash("leave", "Will disconnect from channel")
                    )
                    .addCommands(
                            Commands.slash("nowplaying", "Will display the current song playing")
                    )
                    .addCommands(
                            Commands.slash("stop", "Will stop the bot playing")
                    )
                    .addCommands(
                            Commands.slash("skip", "Will skip the current song")
                    )
                    .addCommands(
                            Commands.slash("queue", "Will display the current queue")
                    )
                    .addCommands(
                            Commands.slash("gpt", "Ask GPT a question")
                                    .addOption(OptionType.STRING, "prompt", "Allows you to talk to Howard", true)
                    )
                    .addCommands(
                            Commands.slash("lofi", "Plays lofi radio")
                            .addOptions(LofiOptions.handleLofiOptions())
                    )
                    .addCommands(
                            Commands.slash("open", "Open any CS2 Case for free!")
                            .addOptions(CaseOptions.handleOptions())
                    )
                    .addCommands(
                            Commands.slash("embed", "Creates an embed that the bot replies with")
                            .addOptions(EmbedBuilderOptions.TITLE, EmbedBuilderOptions.DESCRIPTION, EmbedBuilderOptions.URL,
                                    EmbedBuilderOptions.AUTHOR, EmbedBuilderOptions.THUMBNAIL, EmbedBuilderOptions.IMAGE)
                    )
                    .addCommands(
                            Commands.slash("activity", "Set the bot's activity")
                                    .addOption(OptionType.STRING, "activity", "The activity you want the bot to display", true)
                    )
                    .addCommands(
                            Commands.slash("twitch", "Sends a message to a channel when a Twitch streamer goes live")
                                    .addOption(OptionType.STRING, "channel", "Name of the Twitch Channel you want notifications for", true)
                                    .addOptions(TwitchTextChannelOptions.handleTwitchTextChannelOptions(guild))
                                    .addOption(OptionType.STRING, "message", "Set the notification message", true)
                    )
                    .queue();
            logger.info("Commands registered successfully!");
        }catch (Exception ex){
            logger.error("There was an error with registering commands: " + ex);
        }

    }
}
