package com.bot.discordbotv3.cmds;

import com.bot.discordbotv3.gpt.ChatRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class GptCommand {
    private static final Logger logger = LoggerFactory.getLogger(GptCommand.class);
    public static void handleGptCommand(SlashCommandInteractionEvent event, String gptSecret){
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
            logger.error("Error with URL Request for GPT: " + io);
        }
    }
}
