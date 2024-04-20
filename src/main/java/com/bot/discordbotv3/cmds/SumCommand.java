package com.bot.discordbotv3.cmds;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SumCommand {
    public static void handleSumCommand(SlashCommandInteractionEvent event) {
        event.reply("The sum is: " + String.valueOf(event.getOption("num1").getAsInt() + event.getOption("num2").getAsInt())).queue();
    }
}
