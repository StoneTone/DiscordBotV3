package com.bot.discordbotv3.listener;


import com.bot.discordbotv3.btn.RoleRequestEmbed;
import com.bot.discordbotv3.cmdmgr.CommandManager;
import com.bot.discordbotv3.cmds.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;


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

        //region Schedule for updates (doesn't work need to refactor)
//        AtomicBoolean messageSent = new AtomicBoolean(false);
//        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
//        executorService.scheduleAtFixedRate(() -> {
//            try {
//                LocalDate updateDate = updateNotify();
//                LocalDate now = LocalDate.now(ZoneOffset.UTC);
//                if(updateDate.isEqual(now) && !messageSent.get()) {
//                    //write logic for if there is an update
//                    MessageChannel updateChannel = event.getJDA().getTextChannelById(1161467402089930763L);
//                    EmbedBuilder eb = new EmbedBuilder();
//                    eb.setTitle("New CS2 Update!");
//                    eb.setDescription("Check out the latest update here: \n " +
//                            "https://www.counter-strike.net/news/updates");
//                    updateChannel.sendMessageEmbeds(eb.build()).queue();
//                    messageSent.set(true);
//                    logger.info("Update message sent");
//
//                }else if(!updateDate.isEqual(now)){
//                    messageSent.set(false);
//                    logger.info("No update available for today");
//                }
//                logger.info("Update check completed.");
//            } catch (Exception e) {
//                logger.error("Error during update check: " + e.getMessage());
//            }
//        }, 0, 1, TimeUnit.HOURS);
        //endregion

        //Logging bot has logged in and is ready
        logger.info(event.getJDA().getSelfUser().getName() + " has logged in!");
    }

    /*
    Note:
    EC2 instance: "scp -i /path/to/your/key.pem /path/to/your/app.jar ec2-user@your-instance-ip:/home/ec2-user
    Run NoHup: nohup "java -jar /path/to/your/app.jar > /path/to/your/app.log (or nohup.out) 2>&1 &" (Keeps java instance up)
    for nohup logs: "tail -f nohup.out
     */

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
            default -> event.reply("Invalid slash command!").setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        //region Button Interaction with Requests
        RoleRequestEmbed.handleRoleRequestEmbed(event, requestedUser, requestedRole, guildID);
        //endregion
    }

    public LocalDate updateNotify() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.get("https://www.counter-strike.net/news/updates");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10L));

        WebElement dateElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.updatecapsule_Date_gvPzK")));

        String updateDateString = dateElement.getText();

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMMM d, yyyy")
                .toFormatter(Locale.ENGLISH);
        LocalDate updateDate = LocalDate.parse(updateDateString, formatter.withZone(ZoneId.of("UTC")));

        ZoneId desiredZone = ZoneId.of("UTC");
        ZonedDateTime convertedDateTime = updateDate.atStartOfDay(desiredZone);
        LocalDate convertedDate = convertedDateTime.toLocalDate();

        driver.quit();

        return convertedDate;
    }

}
