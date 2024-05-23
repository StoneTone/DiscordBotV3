package com.bot.discordbotv3.cmds;

import com.bot.discordbotv3.gpt.ChatRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class GptCommand {
    private static final Logger logger = LoggerFactory.getLogger(GptCommand.class);
    public static void handleGptCommand(SlashCommandInteractionEvent event, String gptSecret){
        String prompt = event.getOption("prompt").getAsString();

        event.deferReply().queue(hook -> {
            // Process the response asynchronously
            CompletableFuture.runAsync(() -> {
                try {
                    ChatRequest request = new ChatRequest("gpt-3.5-turbo", prompt);
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

                    // Get Response
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                    String content = jsonResponse.getAsJsonArray("choices")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("message")
                            .get("content").getAsString();

                    // Build and send the embed
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("\"" + prompt + "\"");
                    eb.setDescription(content);

                    hook.editOriginalEmbeds(eb.build()).queue(
                            success -> {
                                logger.info("Success sending message to channel");
                            },
                            failure -> {
                                logger.error("Failed to send reply: " + failure.getMessage());
                                failure.printStackTrace();
                            }
                    );
                } catch (IOException e) {
                    logger.error("Error with URL Request for GPT: " + e);
                    hook.deleteOriginal().and(hook.sendMessage("An error occurred while processing your request.").setEphemeral(true)).queue();
                }
            });
        }, failure -> {
            logger.error("Failed to defer reply: " + failure.getMessage());
            failure.printStackTrace();
        });
    }
}
