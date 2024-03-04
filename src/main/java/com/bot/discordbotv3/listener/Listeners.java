package com.bot.discordbotv3.listener;


import com.bot.discordbotv3.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;


public class Listeners extends ListenerAdapter {

    private final long guildID = 0000000000L; //change to actual guild
    private User requestedUser = null;
    private Role requestedRole = null;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Guild guild = event.getJDA().getGuildById(guildID);



        guild.updateCommands()
                .addCommands(
                        Commands.slash("sum", "sums two numbers")
                                .addOption(OptionType.INTEGER, "num1", "num1", true)
                                .addOption(OptionType.INTEGER, "num2", "num2", true)
                )
                .addCommands(
                        Commands.slash("rolerequest", "Allows you to request a role")
                                .addOptions(new OptionData(OptionType.STRING, "rolerequest", "Allows you to request a role", true)
                                        .addChoice("Test Role1", "1106046269236981811")
                                        .addChoice("Test Role2", "1106449464983572533"))
                )
                .addCommands(
                        Commands.slash("play", "Will play any song")
                            .addOptions(new OptionData(OptionType.STRING, "name", "Name of the song to play", true))
                )
                .queue();
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

    //TODO
    /*
    Add slash command for roles that requests to my user with buttons
    Add slash command for ChatGPT (future)
    Add slash command for music (basics: play, pause and stop)
    More to add later....
     */

    //fix issues with play add sub classes for slash commands
    //check github for reference
    //https://github.com/relaxingleg/Tutorial-Bot/blob/master/src/main/java/com/relaxingleg/commands/music/NowPlaying.java
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event){
        switch(event.getName()){
            case "sum":
                event.reply("The sum is: " + String.valueOf(event.getOption("num1").getAsInt() + event.getOption("num2").getAsInt())).queue();
                break;
            case "rolerequest":
                for(OptionMapping option : event.getOptions()){
                    requestedRole = event.getJDA().getRoleById(option.getAsString());
                }
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Role Request by " + event.getUser().getName());
                eb.setDescription("User " + event.getUser().getName() + " has requested the " + requestedRole.getName() + " role");
                requestedUser = event.getUser();
                User botOwner = event.getJDA().getUserById(00000000000000L); //change to owner id
                botOwner.openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessageEmbeds(eb.build())
                            .setActionRow(
                                    Button.success("approve", "Approve"),
                                    Button.danger("decline", "Decline")
                            ).queue();
                });
                event.reply("Your role request has been submitted for review. Please wait for a response.").setEphemeral(true).queue();
                break;
            case "play":
                Member member = event.getMember();
                GuildVoiceState memberVoiceState = member.getVoiceState();

                if(!memberVoiceState.inAudioChannel()){
                    event.reply("You need to be in a voice channel!").setEphemeral(true).queue();
                    return;
                }

                Member self = event.getGuild().getSelfMember();
                GuildVoiceState selfVoiceState = self.getVoiceState();

                if(!selfVoiceState.inAudioChannel()){
                    //Join
                    event.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
                }else{
                    if(selfVoiceState.getChannel() != memberVoiceState.getChannel()){
                        event.reply("You need to be in the same channel as me").setEphemeral(true).queue();
                        return;
                    }
                }

                Document document = null;
                try {
                    document = Jsoup.connect(event.getOption("name").getAsString()).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String title = document.title().replaceAll(" - YouTube$", "");

                PlayerManager playerManager = PlayerManager.get();
                playerManager.play(event.getGuild(), event.getOption("name").getAsString());

                event.reply("Playing: " + title).setEphemeral(true).queue();
            default:
                event.reply("Invalid slash command!").setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        switch(event.getComponentId()){
            case "approve":
                // Approve button clicked
                eb.setTitle("Role Approved!");
                eb.setDescription("Your request for " + "**" +
                        requestedRole.getName() + "**" + " in " +  "*" + event.getJDA().getGuildById(guildID).getName() + "*"+ " has been approved!");
                requestedUser.openPrivateChannel().queue(privateChannel ->
                        privateChannel.sendMessageEmbeds(eb.build()).queue());
                // Add the role to the user
                event.getJDA().getGuildById(guildID).addRoleToMember(requestedUser, requestedRole).queue();
                event.reply("Role approved!").setEphemeral(true).queue();
                break;
            case "decline":
                // Decline button clicked
                eb.setTitle("Role Declined!");
                eb.setDescription("Your request for " + "**" +
                        requestedRole.getName() + "**" + " in " +  "*" + event.getJDA().getGuildById(guildID).getName() + "*"+ " has been declined!");
                requestedUser.openPrivateChannel().queue(privateChannel ->
                        privateChannel.sendMessageEmbeds(eb.build()).queue());
                event.reply("Role Declined!").setEphemeral(true).queue();
                break;
        }
    }


}
