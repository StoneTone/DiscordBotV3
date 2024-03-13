package com.bot.discordbotv3.listener;


import com.bot.discordbotv3.gpt.ChatRequest;
import com.bot.discordbotv3.lavaplayer.GuildMusicManager;
import com.bot.discordbotv3.lavaplayer.PlayerManager;
import com.bot.discordbotv3.lavaplayer.TrackScheduler;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import io.github.bonigarcia.wdm.WebDriverManager;
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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Listeners extends ListenerAdapter {

    private final long guildID;
    private final long ownerId;
    private User requestedUser = null;
    private Role requestedRole = null;
    private final String ytSecret;
    private final String gptSecret;
    private final Logger logger = LoggerFactory.getLogger(Listeners.class);

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
//                            .addOptions(new OptionData(OptionType.STRING, "type", "Name of the song to play (Any HTTP URL is supported)", true))
                        .addOption(OptionType.STRING, "url", "Name of the song to play (Any HTTP URL is supported)" )
                        .addOption(OptionType.STRING, "search", "Search YouTube for a song")

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
                        Commands.slash("gpt", "Ask GPT a question")
                        .addOption(OptionType.STRING, "prompt", "Allows you to talk to Howard")
                )
                .queue();
        //endregion

        //region Schedule for updates
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {

            try {
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern("MMMM d, yyyy")
                        .toFormatter(Locale.ENGLISH);
                LocalDate updateDate = LocalDate.parse(updateNotify(), formatter);
                LocalDate now = LocalDate.now();               //.of(2024,3,8);
                if(updateDate.isEqual(now)){
                    //write logic for if there is an update
                    MessageChannel updateChannel = event.getJDA().getTextChannelById(1161467402089930763L);
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("New CS2 Update!");
                    eb.setDescription("Check out the latest update here: \n " +
                            "https://www.counter-strike.net/news/updates");
                    updateChannel.sendMessageEmbeds(eb.build()).queue();
                }
                logger.info("Update check completed.");
            } catch (Exception e) {
                logger.error("Error during update check: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.HOURS);
        //endregion

        //Logging bot has logged in and is ready
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

    /*
    Note:
    EC2 instance: "scp -i /path/to/your/key.pem /path/to/your/app.jar ec2-user@your-instance-ip:/path/on/ec2/
    Run NoHup: nohup "java -jar /path/to/your/app.jar > /path/to/your/app.log (or nohup.out) 2>&1 &" (Keeps java instance up)
    for nohup logs: "tail -f nohup.out
     */

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event){
            //region Test Command with Sum
            if(event.getName().equals("sum")) {
                    event.reply("The sum is: " + String.valueOf(event.getOption("num1").getAsInt() + event.getOption("num2").getAsInt())).queue();
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
                for(OptionMapping options : event.getOptions()){
                    if(options.getName().equals("search")){
                        try{
                            YouTube youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), null)
                                    .setApplicationName("Discord Bot")
                                    .build();

                            YouTube.Search.List search = youTube.search().list("id, snippet");
                            search.setKey(ytSecret);
                            search.setQ(options.getAsString());
                            search.setType("video");
                            search.setMaxResults(1L);

                            SearchListResponse response = search.execute();
                            List<SearchResult> results = response.getItems();

                            if(results != null && !results.isEmpty()){
                                String videoId = results.get(0).getId().getVideoId();
                                String videoUrl = "https://youtu.be/" + videoId;

                                document = Jsoup.connect(videoUrl).get();
                                String title = document.title().replaceAll(" - YouTube$", "");

                                PlayerManager playerManager = PlayerManager.get();
                                playerManager.play(event.getGuild(), videoUrl);

                                event.reply("Playing: " + title).setEphemeral(true).queue();
                            }else{
                                event.reply("No search results found for: " + options.getName()).setEphemeral(true).queue();
                            }
                        }catch(IOException e){
                            logger.error("Error with search on YouTube: " + e);
                        }
                        break;
                    }
                    if(options.getName().equals("url")) {
                        try {
                            document = Jsoup.connect(event.getOption("url").getAsString()).get();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String title = document.title().replaceAll(" - YouTube$", "");

                        PlayerManager playerManager = PlayerManager.get();
                        playerManager.play(event.getGuild(), event.getOption("url").getAsString());

                        event.reply("Playing: " + title).setEphemeral(true).queue();
                        break;
                    }
                }


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

            //region GPT
            else if(event.getName().equals("gpt")){
                ChatRequest request = new ChatRequest("gpt-3.5-turbo", event.getOption("prompt").getAsString());
                try{
                    URL url = new URL("https://api.openai.com/v1/chat/completions");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization", "Bearer " + gptSecret);
                    conn.setDoOutput(true);

                    Gson gson = new Gson();
                    String json = gson.toJson(request);
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                    writer.write(json);
                    writer.flush();
                    writer.close();

                    //Get Response
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while((inputLine = in.readLine()) != null){
                        response.append(inputLine);
                    }
                    in.close();

                    JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                    String content = "";
                    if (jsonResponse.has("choices")) {
                        JsonArray choices = jsonResponse.getAsJsonArray("choices");
                        if (choices.size() > 0) {
                            JsonObject firstChoice = choices.get(0).getAsJsonObject();
                            if (firstChoice.has("message")) {
                                JsonObject message = firstChoice.getAsJsonObject("message");
                                if (message.has("content")) {
                                    content = message.get("content").getAsString();
                                }
                            }
                        }
                    }

                    event.reply(content).queue();

                }catch (IOException io){
                    System.out.println("Error with URL Request for GPT: " + io);
                }
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

    public String updateNotify() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        WebDriver driver = new ChromeDriver(options);
        driver.get("https://www.counter-strike.net/news/updates");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10L));

        WebElement dateElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.updatecapsule_Date_gvPzK")));

        String updateDate = dateElement.getText();

        driver.quit();

        return updateDate;
    }

}
