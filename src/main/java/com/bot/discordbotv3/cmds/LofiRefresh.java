package com.bot.discordbotv3.cmds;

import com.bot.discordbotv3.options.LofiOptions;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LofiRefresh {
    private static final Logger logger = LoggerFactory.getLogger(LofiRefresh.class);

    public static void handleRefreshLofi(Guild guild) {
        try {
            OptionData refreshedOptions = LofiOptions.handleLofiOptions();

            CommandListUpdateAction commands = guild.updateCommands();
            commands.addCommands(
                    Commands.slash("lofi", "Plays lofi radio")
                            .addOptions(refreshedOptions)
            ).queue();
            logger.info("Successfully updated lofi");
        } catch (Exception e) {
            logger.error("Failed to refresh lofi options: " + e.getMessage(), e);
        }
    }
}
