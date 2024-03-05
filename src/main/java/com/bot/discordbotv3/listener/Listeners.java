package com.bot.discordbotv3.listener;


import com.bot.discordbotv3.lavaplayer.GuildMusicManager;
import com.bot.discordbotv3.lavaplayer.PlayerManager;
import com.bot.discordbotv3.lavaplayer.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Listeners extends ListenerAdapter {

    private long guildID;
    private long ownerId;
    private User requestedUser = null;
    private Role requestedRole = null;
    private final Logger logger = LoggerFactory.getLogger(Listeners.class);

    public Listeners(@Value("${guild.id}") long guildID, @Value("${owner.id}") long ownerId) {
        this.guildID = guildID;
        this.ownerId = ownerId;
    }

    public Listeners() {

    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        //region Updating Commands for Start-Up
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
                            .addOptions(new OptionData(OptionType.STRING, "url", "Name of the song to play (Any HTTP URL is supported)", true))
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
                        Commands.slash("greet", "Greets you")
                )
                .queue();
        //endregion

        logger.info(event.getJDA().getSelfUser().getName() + " has logged in!");
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event){
        //region MessageRecievedEvent for !Ping
        MessageChannel channel = event.getChannel();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("\t\tWelcome to Area 52!");
        eb.setDescription("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n" +
                "===== where dreams become memes =====\n" +
                "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n" +
                "===== Roles =====\n\n" +
                " \t:rocket:Geek Squid\n" +
                " \t:computer:Best Buy\n" +
                " \t<:cryingthumbsup:976319319229206548>Amigos\n" +
                "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n" +
                "=       Requestable Roles  =\n" +
                "\t<:nobitches:976317738467332156>Shree Sports\n" +
                "\t<:depression:976317842007945266>Shreevelopers\n" +
                "\t<:sayitagain:976318270602887228>Peasant\n\n" +
                "Request the role in the \"Request\" Channel \n using Howard with **/rolerequest**\n " +
                "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n" +
                "=========== Rules ===========\n" +
                "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n" +
                "▬▬▬   No mic spam      ▬▬▬\n" +
                "▬▬▬    No toxicity   ▬▬▬\n" +
                "▬▬▬    Have fun!     ▬▬▬\n" +
                "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

        if(event.getAuthor().isBot()) return;
        if(event.getMessage().getContentRaw().equals("!title")){
            channel.sendMessageEmbeds(eb.build()).queue();

        }
        if(event.getMessage().getContentRaw().equals("!request")){
            EmbedBuilder ee = new EmbedBuilder();
            ee.setTitle("Welcome!");
            ee.setDescription("Hello! Please use my **/rolerequest** in order to request a role!\n" +
                    "Once your request comes through it will be under review and will be accepted or denied by an admin");
            channel.sendMessageEmbeds(ee.build()).queue();
        }
        //endregion
    }

    //TODO
    /*
    Add slash command for roles that requests to my user with buttons (Check)
    Add slash command for ChatGPT (future)
    Add slash command for music (basics: play, pause and stop) (Check)
    More to add later....
     */

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event){
            //region Test Command with Sum
            if(event.getName().equals("sum")) {
                    event.reply("The sum is: " + String.valueOf(event.getOption("num1").getAsInt() + event.getOption("num2").getAsInt())).queue();
            }
            else if(event.getName().equals("greet")){
                event.reply("Hello! " + event.getUser().getName()).setEphemeral(true).queue();
            }
            //endregion

            //region Roles
            else if(event.getName().equals("rolerequest")) {
                for (OptionMapping option : event.getOptions()) {
                    requestedRole = event.getJDA().getRoleById(option.getAsString());
                }
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Role Request by " + event.getUser().getName());
                eb.setDescription("User " + event.getUser().getName() + " has requested the " + requestedRole.getName() + " role");
                requestedUser = event.getUser();
                User botOwner = event.getJDA().getUserById(ownerId);
                botOwner.openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessageEmbeds(eb.build())
                            .setActionRow(
                                    Button.success("approve", "Approve"),
                                    Button.danger("decline", "Decline")
                            ).queue();
                });
                event.reply("Your role request has been submitted for review. Please wait for a response.").setEphemeral(true).queue();
            }
            //endregion

            //region AudioPlayer
            else if(event.getName().equals("play")) {
                Member member = event.getMember();
                GuildVoiceState memberVoiceState = member.getVoiceState();

                if (!memberVoiceState.inAudioChannel()) {
                    event.reply("You need to be in a voice channel!").setEphemeral(true).queue();
                    return;
                }

                Member self = event.getGuild().getSelfMember();
                GuildVoiceState selfVoiceState = self.getVoiceState();

                if (!selfVoiceState.inAudioChannel()) {
                    //Join
                    event.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
                } else {
                    if (selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
                        event.reply("You need to be in the same channel as me").setEphemeral(true).queue();
                        return;
                    }
                }

                Document document = null;
                try {
                    document = Jsoup.connect(event.getOption("url").getAsString()).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String title = document.title().replaceAll(" - YouTube$", "");

                PlayerManager playerManager = PlayerManager.get();
                playerManager.play(event.getGuild(), event.getOption("url").getAsString());

                event.reply("Playing: " + title).setEphemeral(true).queue();
            }

            else if(event.getName().equals("stop")){
                Member member = event.getMember();
                GuildVoiceState memberVoiceState = member.getVoiceState();

                if(!memberVoiceState.inAudioChannel()) {
                    event.reply("You need to be in a voice channel").queue();
                    return;
                }

                Member self = event.getGuild().getSelfMember();
                GuildVoiceState selfVoiceState = self.getVoiceState();

                if(!selfVoiceState.inAudioChannel()) {
                    event.reply("I am not in an audio channel").queue();
                    return;
                }

                if(selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
                    event.reply("You are not in the same channel as me").queue();
                    return;
                }

                GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
                TrackScheduler trackScheduler = guildMusicManager.getTrackScheduler();
                trackScheduler.getQueue().clear();
                trackScheduler.getPlayer().stopTrack();
                event.reply("Stopped").queue();
            }

            else if(event.getName().equals("skip")){
                Member member = event.getMember();
                GuildVoiceState memberVoiceState = member.getVoiceState();

                if(!memberVoiceState.inAudioChannel()) {
                    event.reply("You need to be in a voice channel").queue();
                    return;
                }

                Member self = event.getGuild().getSelfMember();
                GuildVoiceState selfVoiceState = self.getVoiceState();

                if(!selfVoiceState.inAudioChannel()) {
                    event.reply("I am not in an audio channel").queue();
                    return;
                }

                if(selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
                    event.reply("You are not in the same channel as me").queue();
                    return;
                }

                GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
                guildMusicManager.getTrackScheduler().getPlayer().stopTrack();
                event.reply("Skipped current song").queue();
            }

            else if(event.getName().equals("queue")){
                Member member = event.getMember();
                GuildVoiceState memberVoiceState = member.getVoiceState();

                if(!memberVoiceState.inAudioChannel()) {
                    event.reply("You need to be in a voice channel").queue();
                    return;
                }

                Member self = event.getGuild().getSelfMember();
                GuildVoiceState selfVoiceState = self.getVoiceState();

                if(!selfVoiceState.inAudioChannel()) {
                    event.reply("I am not in an audio channel").queue();
                    return;
                }

                if(selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
                    event.reply("You are not in the same channel as me").queue();
                    return;
                }

                GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
                List<AudioTrack> queue = new ArrayList<>(guildMusicManager.getTrackScheduler().getQueue());
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Current Queue");
                if(queue.isEmpty()) {
                    embedBuilder.setDescription("Queue is empty");
                }
                for(int i = 0; i < queue.size(); i++) {
                    AudioTrackInfo info = queue.get(i).getInfo();
                    embedBuilder.addField(i+1 + ":", info.title, false);
                }
                event.replyEmbeds(embedBuilder.build()).queue();
            }

            else if(event.getName().equals("nowplaying")){
                Member member = event.getMember();
                GuildVoiceState memberVoiceState = member.getVoiceState();

                if(!memberVoiceState.inAudioChannel()) {
                    event.reply("You need to be in a voice channel").queue();
                    return;
                }

                Member self = event.getGuild().getSelfMember();
                GuildVoiceState selfVoiceState = self.getVoiceState();

                if(!selfVoiceState.inAudioChannel()) {
                    event.reply("I am not in an audio channel").queue();
                    return;
                }

                if(selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
                    event.reply("You are not in the same channel as me").queue();
                    return;
                }

                GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
                if(guildMusicManager.getTrackScheduler().getPlayer().getPlayingTrack() == null) {
                    event.reply("I am not playing anything").queue();
                    return;
                }
                AudioTrackInfo info = guildMusicManager.getTrackScheduler().getPlayer().getPlayingTrack().getInfo();
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Currently Playing");
                embedBuilder.setDescription("**Name:** `" + info.title + "`");
                embedBuilder.appendDescription("\n**Author:** `" + info.author + "`");
                embedBuilder.appendDescription("\n**URL:** `" + info.uri + "`");
                event.replyEmbeds(embedBuilder.build()).queue();
            }
            //endregion

            else{
                event.reply("Invalid slash command!").setEphemeral(true).queue();
            }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        //region Button Interaction with Requests
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

        //endregion
    }


}
