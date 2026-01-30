package com.bot.discordbotv3.listener;


import com.bot.discordbotv3.embed.RoleRequestEmbed;
import com.bot.discordbotv3.cmdmgr.CommandManager;
import com.bot.discordbotv3.cmds.*;
import com.bot.discordbotv3.service.TwitchService;
import jakarta.annotation.PreDestroy;
import net.dv8tion.jda.api.entities.*;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Listeners extends ListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(Listeners.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final long guildID;
    private Role requestedRole = null;
    private final TwitchService twitchService;
    private final String gptSecret;
    private final String gptModel;


    public Listeners(long guildID, TwitchService twitchService, String gptSecret, String gptModel) {
        this.guildID = guildID;
        this.twitchService = twitchService;
        this.gptSecret = gptSecret;
        this.gptModel = gptModel;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        //region Updating Commands for Start-Up
        Guild guild = event.getJDA().getGuildById(guildID);
        CommandManager.registerCommands(guild);
        //endregion
        //Logging bot has logged in and is ready
        logger.info(event.getJDA().getSelfUser().getName() + " has logged in!");
        //Scheduler for updating Lofi command options
        runLofiScheduler(guild);
    }

    // NEW: Add autocomplete handler
    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("open")) {
            CaseCommand.handleAutocomplete(event);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event){
        String commandName = event.getName();

        switch (commandName) {
            case "sum" -> SumCommand.handleSumCommand(event);
            case "rolerequest" -> {
                for (OptionMapping option : event.getOptions()) {
                    requestedRole = event.getJDA().getRoleById(option.getAsString());
                }
                RoleRequestCommand.handleRoleRequestCommand(event, requestedRole);
            }
            case "play" -> PlayCommand.handlePlayCommand(event);
            case "pause" -> PauseCommand.handlePauseCommand(event);
            case "unpause" -> UnPauseCommand.handleUnPauseCommand(event);
            case "leave" -> LeaveCommand.handleLeaveCommand(event);
            case "stop" -> StopCommand.handleStopCommand(event);
            case "skip" -> SkipCommand.handleSkipCommand(event);
            case "queue" -> QueueCommand.handleQueueCommand(event);
            case "nowplaying" -> NowPlayingCommand.handleNowPlayingCommand(event);
            case "gpt" -> GptCommand.handleGptCommand(event, gptSecret, gptModel);
            case "lofi" -> {
                String videoURL = "";
                for(OptionMapping option : event.getOptions()){
                    videoURL = option.getAsString();
                }
                LofiCommand.handleLofiCommand(event, videoURL);
            }
            case "activity" -> ActivityCommand.handleActivityCommand(event);
            case "twitch" -> TwitchCommand.handleTwitchCommand(event, twitchService);
            case "twitchconfig" -> TwitchConfigCommand.handleTwitchConfigCommand(event, twitchService);
            case "open" -> CaseCommand.handleCaseCommand(event);
            case "embed" -> EmbedCommand.handleEmbedCommand(event);
            default -> event.reply("Invalid slash command!").setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        //region Button Interaction with Requests
        RoleRequestEmbed.handleRoleRequestEmbed(event, guildID);
        //endregion
    }

    private void runLofiScheduler(Guild guild) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(1).withMinute(0).withSecond(0);

        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }

        long initialDelay = ChronoUnit.MILLIS.between(now, nextRun);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                LofiRefresh.handleRefreshLofi(guild);
            } catch (Exception e) {
                logger.error("Error occurred while refreshing lofi command options: {}", e.getMessage());
            }
        }, initialDelay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void shutdown() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
