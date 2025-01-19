package com.bot.discordbotv3.cmds;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Objects;

public class ActivityCommand {
    public static void handleActivityCommand(SlashCommandInteractionEvent event) {
        if(event.getInteraction().getMember().isOwner()){
            if(event.getOption("activity") == null){
                event.reply("Activity cannot be null!").setEphemeral(true).queue();
            }else{
                event.getJDA().getPresence().setActivity(Activity.customStatus(Objects.requireNonNull(event.getOption("activity")).getAsString()));
                event.reply("Activity has been set!").setEphemeral(true).queue();
            }
        }else {
            event.reply("Only the owner can set the status!").setEphemeral(true).queue();
        }
    }
}
