package com.bot.discordbotv3.listener;


import com.bot.discordbotv3.btn.RoleRequestEmbed;
import com.bot.discordbotv3.cmdmgr.CommandManager;
import com.bot.discordbotv3.cmds.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Listeners extends ListenerAdapter {

    private final Logger logger = LoggerFactory.getLogger(Listeners.class);

    private final long guildID;
    private final long ownerId;
    private User requestedUser = null;
    private Role requestedRole = null;
    private final String ytSecret;
    private final String gptSecret;


    public Listeners(long guildID, long ownerId, String ytSecret, String gptSecret) {
        this.guildID = guildID;
        this.ownerId = ownerId;
        this.ytSecret = ytSecret;
        this.gptSecret = gptSecret;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        //region Updating Commands for Start-Up
        Guild guild = event.getJDA().getGuildById(guildID);
        CommandManager.registerCommands(guild);
        //endregion

        //Logging bot has logged in and is ready
        logger.info(event.getJDA().getSelfUser().getName() + " has logged in!");
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event){
        String commandName = event.getName();

        switch (commandName) {
            case "sum" -> SumCommand.handleSumCommand(event);
            case "rolerequest" -> {
                requestedUser = event.getUser();
                for (OptionMapping option : event.getOptions()) {
                    requestedRole = event.getJDA().getRoleById(option.getAsString());
                }
                RoleRequestCommand.handleRoleRequestCommand(event, requestedRole, requestedUser, ownerId);
            }
            case "play" -> PlayCommand.handlePlayCommand(event, ytSecret);
            case "pause" -> PauseCommand.handlePauseCommand(event);
            case "unpause" -> UnPauseCommand.handleUnPauseCommand(event);
            case "leave" -> LeaveCommand.handleLeaveCommand(event);
            case "stop" -> StopCommand.handleStopCommand(event);
            case "skip" -> SkipCommand.handleSkipCommand(event);
            case "queue" -> QueueCommand.handleQueueCommand(event);
            case "nowplaying" -> NowPlayingCommand.handleNowPlayingCommand(event);
            case "gpt" -> GptCommand.handleGptCommand(event, gptSecret);
            case "lofi" -> {
                String optionStr = "";
                for(OptionMapping option : event.getOptions()){
                    optionStr = option.getAsString();
                }
                LofiCommand.handleLofiCommand(event, ytSecret, optionStr);
            }
            case "open" -> CaseCommand.handleCaseCommand(event);
            default -> event.reply("Invalid slash command!").setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        //region Button Interaction with Requests
        RoleRequestEmbed.handleRoleRequestEmbed(event, requestedUser, requestedRole, guildID);
        //endregion
    }

}
