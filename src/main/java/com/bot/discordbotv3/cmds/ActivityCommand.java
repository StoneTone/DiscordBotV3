package com.bot.discordbotv3.cmds;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;


public class ActivityCommand {
    public static void handleActivityCommand(SlashCommandInteractionEvent event) {
        if (!event.getInteraction().getMember().isOwner()) {
            event.reply("Only the owner can set the status!").setEphemeral(true).queue();
            return;
        }

        OptionMapping statusOption = event.getOption("status");
        OptionMapping textOption = event.getOption("text");

        if (statusOption == null || textOption == null) {
            event.reply("Activity cannot be null!").setEphemeral(true).queue();
            return;
        }

        String status = statusOption.getAsString();
        String text = textOption.getAsString().trim();

        if (text.isEmpty()) {
            event.reply("Activity text cannot be empty!").setEphemeral(true).queue();
            return;
        }

        Activity activity = switch (status) {
            case "listen" -> Activity.listening(text);
            case "competing" -> Activity.competing(text);
            case "playing" -> Activity.playing(text);
            case "watching" -> Activity.watching(text);
            case "custom" -> Activity.customStatus(text);
            default -> Activity.streaming("Huh?", "www.google.com");
        };


        event.getJDA().getPresence().setActivity(activity);
        event.reply("Activity has been set!").setEphemeral(true).queue();
    }
}
