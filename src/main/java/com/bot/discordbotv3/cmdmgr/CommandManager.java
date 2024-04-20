package com.bot.discordbotv3.cmdmgr;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandManager {
    private static final Logger logger = LoggerFactory.getLogger(CommandManager.class);
    public static void registerCommands(Guild guild){
        try{
            guild.updateCommands()
                    .addCommands(
                            Commands.slash("sum", "sums two numbers")
                                    .addOption(OptionType.INTEGER, "num1", "num1", true)
                                    .addOption(OptionType.INTEGER, "num2", "num2", true)
                    )
                    .addCommands(
                            Commands.slash("rolerequest", "Allows you to request a role")
                                    .addOptions(new OptionData(OptionType.STRING, "rolerequest", "Allows you to request a role", true)
                                            .addChoice("ShreeSports", "912638897316589579")
                                            .addChoice("ShreeActiveRoster", "1137982033671495761")
                                            .addChoice("Shreeveloper", "1044861143514099712")
                                            .addChoice("Amigos", "695888137146335253")
                                            .addChoice("Geek Squad", "698031938790752286")
                                            .addChoice("Branded Payment", "572633970198446091")
                                            .addChoice("Peasant", "747662211744399442"))
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
                    .queue();
            logger.info("Commands registered successfully!");
        }catch (Exception ex){
            logger.error("There was an error with registering commands: " + ex);
        }

    }
}
