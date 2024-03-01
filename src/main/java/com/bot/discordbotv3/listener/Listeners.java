package com.bot.discordbotv3.listener;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

public class Listeners extends ListenerAdapter {

    private final long guildID = 0000000L;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Guild guild = event.getJDA().getGuildById(guildID);
        guild.updateCommands()
                .addCommands(Commands.slash("sum","sums two numbers")
                .addOption(OptionType.INTEGER,"num1", "num1", true)
                .addOption(OptionType.INTEGER, "num2","num2", true)).queue();
        System.out.println(event.getJDA().getSelfUser().getName() + " has logged in!");
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event){
        MessageChannel channel = event.getChannel();
        if(event.getAuthor().isBot()) return;
        if(event.getMessage().getContentRaw().equals("!ping")){
            channel.sendMessage("pong!").queue();

        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event){
        if(event.getName().equals("sum"))
            event.reply("The sum is: " + String.valueOf(event.getOption("num1").getAsInt() + event.getOption("num2").getAsInt())).queue();
    }

}
