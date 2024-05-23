package com.bot.discordbotv4.cmds;

import com.bot.discordbotv4.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PauseCommand {
    public static void handlePauseCommand(SlashCommandInteractionEvent event){
        //write more logic then add unpause function
        PlayerManager playerManager = PlayerManager.get();
        if(playerManager.isPaused(event.getGuild())){
            event.reply("I'm already paused!").setEphemeral(true).queue();
        }else{
            playerManager.pause(event.getGuild());
            event.reply("Paused").setEphemeral(true).queue();
        }
    }
}
