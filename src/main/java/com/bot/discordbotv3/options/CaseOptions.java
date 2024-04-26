package com.bot.discordbotv3.options;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CaseOptions {
    public static OptionData handleOptions(){
        OptionData caseOptions = new OptionData(OptionType.STRING, "case", "Pick the case you want to open!", true);

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://bymykel.github.io/CSGO-API/api/en/crates.json").openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONArray jsonArray = new JSONArray(response.toString());
            int discordLimit = 0;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("name");
                String id = jsonObject.getString("id");
                if(name.contains("Case") && discordLimit <= 24){
                    caseOptions.addChoice(name, id);
                    discordLimit++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return caseOptions;
    }

}
