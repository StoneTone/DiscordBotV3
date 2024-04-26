package com.bot.discordbotv3.cmds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CaseCommand {
    private static final Logger logger = LoggerFactory.getLogger(CaseCommand.class);
    private static final Random random = new Random();
    private static final double MIL_SPEC_GRADE_PROB = 0.7992;
    private static final double RESTRICTED_PROB = 0.1598;
    private static final double CLASSIFIED_PROB = 0.032;
    private static final double COVERT_PROB = 0.0064;
    private static final double EXCEEDINGLY_RARE_PROB = 0.0026;
    private static double randomWear = 0.0;

    public static void handleCaseCommand(SlashCommandInteractionEvent event){
        String caseId = event.getOption("case").getAsString();
        JSONObject caseData = fetchCaseData(caseId);
        String rarity = getRandomRarity();

        if (caseData != null) {
            String caseName = caseData.getString("name");
            JSONObject randomItem = getRandomItemWithRarity(caseData, rarity);
            String image = randomItem.getString("image");
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(event.getUser().getEffectiveName() + " opened a " + caseName + " and found");
            if(isStatTrack()){
                eb.addField("Item", "Stat-Track " + randomItem.getString("name"),true);
            }else{
                eb.addField("Item", randomItem.getString("name"), true);
            }
            eb.addField("Wear", getWearText(randomWear), true).setColor(getRarityColor(rarity));
            eb.addField("Wear Value", String.valueOf(randomWear), true);
            eb.addField("Pattern", String.valueOf(patternIndex()), true);
            eb.setImage(image);

            event.replyEmbeds(eb.build()).queue();

        } else {
            event.reply("I'm having issues fetching case data. Please try again").setEphemeral(true).queue();
        }

    }

    private static String getWearText(double randomWear){
        String wear = "";
        if(randomWear >= 0.00 && randomWear <= 0.07){
            wear = "Factory New";
        }else if(randomWear >= 0.08 && randomWear <= 0.15){
            wear = "Minimal Wear";
        }else if(randomWear >= 0.16 && randomWear <= 0.36){
            wear = "Field Tested";
        }else if(randomWear >= 0.37 && randomWear <= 0.44){
            wear = "Well-Worn";
        }else{
            wear = "Battle-Scarred";
        }

        return wear;
    }

    private static double getWear(JSONObject item){
        String skinId = item.getString("id");
        // Call API to fetch min_float and max_float for the skin
        JSONObject skinData = fetchSkinData(skinId);
        try{
            if (skinData != null) {
                double minFloat = skinData.getDouble("min_float");
                double maxFloat = skinData.getDouble("max_float");
                double randomWear = random.nextDouble() * (maxFloat - minFloat) + minFloat;
                double scaledWear = randomWear * 100000000;
                long roundedScaledWear = Math.round(scaledWear);
                return (double) roundedScaledWear / 100000000;
            }
        }catch (NumberFormatException numFor){
            logger.error("Format Exception: " + numFor);
            logger.error("SkinID: " + skinId);
            logger.error("Skin Data: " + skinData);
        }
        return 0.0;
    }

    private static boolean isStatTrack(){
        double randomVal = random.nextDouble();
        double statTrackProbability = 0.1;

        return randomVal < statTrackProbability;
    }

    private static int patternIndex(){
        return random.nextInt(1000) + 1;
    }

    private static String getRandomRarity() {
        double randomNumber = random.nextDouble();
        double cumulativeProbability = 0.0;

        if (randomNumber < MIL_SPEC_GRADE_PROB) {
            return "Mil-Spec Grade";
        } else {
            cumulativeProbability += MIL_SPEC_GRADE_PROB;
            if (randomNumber < cumulativeProbability + RESTRICTED_PROB) {
                return "Restricted";
            } else {
                cumulativeProbability += RESTRICTED_PROB;
                if (randomNumber < cumulativeProbability + CLASSIFIED_PROB) {
                    return "Classified";
                } else {
                    cumulativeProbability += CLASSIFIED_PROB;
                    if (randomNumber < cumulativeProbability + COVERT_PROB) {
                        return "Covert";
                    } else {
                        cumulativeProbability += COVERT_PROB;
                        if(randomNumber < cumulativeProbability + EXCEEDINGLY_RARE_PROB){
                            return "Rare Special Item";
                        }
                    }
                }
            }
        }
        return "Common";
    }

    private static int getRarityColor(String rarity){
        return switch (rarity) {
            case "Mil-Spec Grade" -> 19865;
            case "Restricted" -> 6697881;
            case "Classified" -> 16711935;
            case "Covert" -> 16711680;
            case "Rare Special Item" -> 16776960;
            default -> 0;
        };
    }

    private static JSONObject fetchCaseData(String caseId) {
        try {
            URL url = new URL("https://bymykel.github.io/CSGO-API/api/en/crates.json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                reader.close();

                JSONArray cratesArray = new JSONArray(jsonBuilder.toString());
                for (int i = 0; i < cratesArray.length(); i++) {
                    JSONObject crate = cratesArray.getJSONObject(i);
                    if (crate.getString("id").equals(caseId)) {
                        return crate;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static JSONObject fetchSkinData(String skinId) {
        try {
            URL url = new URL("https://bymykel.github.io/CSGO-API/api/en/skins.json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                reader.close();

                JSONArray skinsArray = new JSONArray(jsonBuilder.toString());
                for (int i = 0; i < skinsArray.length(); i++) {
                    JSONObject skin = skinsArray.getJSONObject(i);
                    if (skin.getString("id").equals(skinId)) {
                        return skin;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static JSONObject getRandomItemWithRarity(JSONObject caseData, String rarity) {
        JSONArray items;
        if (rarity.equals("Rare Special Item")) {
            items = caseData.getJSONArray("contains_rare");
        } else {
            items = caseData.getJSONArray("contains");
        }

        List<JSONObject> itemsWithRarity = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            JSONObject rarityObject = item.getJSONObject("rarity");
            if (rarityObject.getString("name").equals(rarity)) {
                itemsWithRarity.add(item);
            }
            if(rarity.equals("Rare Special Item")){
                itemsWithRarity.add(item);
            }
        }

        if (itemsWithRarity.isEmpty()) {
            return null;
        }

        JSONObject randomItem = itemsWithRarity.get(random.nextInt(itemsWithRarity.size()));
        randomWear = getWear(randomItem);
        return randomItem;
    }

}
