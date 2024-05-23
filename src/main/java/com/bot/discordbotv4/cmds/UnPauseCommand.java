package com.bot.discordbotv4.cmds;

import com.bot.discordbotv4.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class UnPauseCommand {
    public static void handleUnPauseCommand(SlashCommandInteractionEvent event){
        PlayerManager playerManager = PlayerManager.get();
        if(!playerManager.isPaused(event.getGuild())){
            event.reply("I'm not paused!").setEphemeral(true).queue();
        }else{
            playerManager.unpause(event.getGuild());
            event.reply("Unpaused").setEphemeral(true).queue();
        }
    }
}
