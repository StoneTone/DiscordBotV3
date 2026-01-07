package com.bot.discordbotv3.cmds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;

public class CaseCommand {
    private static final Logger logger = LoggerFactory.getLogger(CaseCommand.class);
    private static final double MIL_SPEC_GRADE_PROB = 0.7992;
    private static final double RESTRICTED_PROB = 0.1598;
    private static final double CLASSIFIED_PROB = 0.032;
    private static final double COVERT_PROB = 0.0064;
    private static final double EXCEEDINGLY_RARE_PROB = 0.0026;
    private static final double STAT_TRACK_PROB = 0.1;

    // Caches
    private static volatile Map<String, JSONObject> cratesCache;
    private static volatile Map<String, JSONObject> skinsCache;
    private static volatile Map<String, Map<String, List<JSONObject>>> caseRarityCache;
    private static volatile Map<String, String> caseNameToIdCache;
    private static volatile boolean cacheInitialized = false;
    private static final Object initLock = new Object();

    private static void ensureCacheInitialized() {
        if (cacheInitialized) return;

        synchronized (initLock) {
            if (cacheInitialized) return;

            long startTime = System.currentTimeMillis();
            logger.info("Starting cache initialization...");

            try {
                Map<String, JSONObject> tempCratesCache = new HashMap<>();
                Map<String, JSONObject> tempSkinsCache = new HashMap<>();
                Map<String, Map<String, List<JSONObject>>> tempRarityCache = new HashMap<>();
                Map<String, String> tempNameToIdCache = new HashMap<>();

                // Load crates
                JSONArray cratesArray = loadJSONArrayFromResource("/crates.json");
                for (int i = 0; i < cratesArray.length(); i++) {
                    JSONObject crate = cratesArray.getJSONObject(i);
                    String crateId = crate.getString("id");
                    String crateName = crate.getString("name");

                    tempCratesCache.put(crateId, crate);
                    tempNameToIdCache.put(crateName.toLowerCase(), crateId);

                    // Pre-compute rarity distributions
                    Map<String, List<JSONObject>> rarityMap = new HashMap<>();

                    // Regular items
                    if (crate.has("contains")) {
                        JSONArray contains = crate.getJSONArray("contains");
                        for (int j = 0; j < contains.length(); j++) {
                            JSONObject item = contains.getJSONObject(j);
                            String rarityName = item.getJSONObject("rarity").getString("name");
                            rarityMap.computeIfAbsent(rarityName, k -> new ArrayList<>()).add(item);
                        }
                    }

                    // Rare items
                    if (crate.has("contains_rare")) {
                        JSONArray containsRare = crate.getJSONArray("contains_rare");
                        List<JSONObject> rareItems = new ArrayList<>();
                        for (int j = 0; j < containsRare.length(); j++) {
                            rareItems.add(containsRare.getJSONObject(j));
                        }
                        rarityMap.put("Rare Special Item", rareItems);
                    }

                    tempRarityCache.put(crateId, rarityMap);
                }

                // Load skins
                JSONArray skinsArray = loadJSONArrayFromResource("/skins.json");
                for (int i = 0; i < skinsArray.length(); i++) {
                    JSONObject skin = skinsArray.getJSONObject(i);
                    tempSkinsCache.put(skin.getString("id"), skin);
                }

                // Assign to volatile fields
                cratesCache = tempCratesCache;
                skinsCache = tempSkinsCache;
                caseRarityCache = tempRarityCache;
                caseNameToIdCache = tempNameToIdCache;

                cacheInitialized = true;

                long endTime = System.currentTimeMillis();
                logger.info("Cache initialized with {} crates and {} skins in {}ms",
                        cratesCache.size(), skinsCache.size(), (endTime - startTime));
            } catch (IOException e) {
                logger.error("Failed to initialize cache", e);
                throw new RuntimeException("Failed to initialize cache", e);
            }
        }
    }

    private static JSONArray loadJSONArrayFromResource(String resourcePath) throws IOException {
        try (InputStream inputStream = CaseCommand.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            StringBuilder jsonBuilder = new StringBuilder(8192);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), 8192)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
            }

            return new JSONArray(jsonBuilder.toString());
        }
    }

    public static void handleAutocomplete(CommandAutoCompleteInteractionEvent event) {
        if (!event.getFocusedOption().getName().equals("case")) return;

        ensureCacheInitialized();

        String userInput = event.getFocusedOption().getValue().toLowerCase();

        // Pre-allocate list size
        List<Command.Choice> choices = new ArrayList<>(25);

        for (Map.Entry<String, JSONObject> entry : cratesCache.entrySet()) {
            if (choices.size() >= 25) break;

            String caseName = entry.getValue().getString("name").toLowerCase();
            if (caseName.contains("case") && (userInput.isEmpty() || caseName.contains(userInput))) {
                choices.add(new Command.Choice(entry.getValue().getString("name"), entry.getKey()));
            }
        }

        event.replyChoices(choices).queue();
    }

    public static void handleCaseCommand(SlashCommandInteractionEvent event) {
        ensureCacheInitialized();

        String input = event.getOption("case").getAsString();

        String caseId = cratesCache.containsKey(input)
                ? input
                : caseNameToIdCache.get(input.toLowerCase());

        if (caseId == null) {
            event.reply("Case not found!").setEphemeral(true).queue();
            return;
        }

        JSONObject caseData = cratesCache.get(caseId);
        if (caseData == null) {
            event.reply("Case data not found!").setEphemeral(true).queue();
            return;
        }

        String rarity = getRandomRarity();
        ItemResult result = getRandomItemWithRarity(caseId, rarity);

        if (result == null) {
            event.reply("Error getting item!").setEphemeral(true).queue();
            return;
        }

        String caseName = caseData.getString("name");
        JSONObject randomItem = result.item;
        double wear = result.wear;
        String itemName = randomItem.getString("name");
        String image = randomItem.getString("image");
        boolean isStatTrack = isStatTrack();
        int pattern = patternIndex();
        String wearText = getWearText(wear);
        int color = getRarityColor(rarity);

        boolean isRareSpecialItem = rarity.equals("Rare Special Item");

        EmbedBuilder eb = new EmbedBuilder();
        if (isRareSpecialItem) {
            eb.setTitle(event.getUser().getEffectiveName() + " opened a " + caseName + " and found a RARE SPECIAL ITEM!");
        } else {
            eb.setTitle(event.getUser().getEffectiveName() + " opened a " + caseName + " and found");
        }

        eb.addField("Item", (isStatTrack ? "StatTrak™ " : "") + itemName, true);
        eb.addField("Wear", wearText, true);
        eb.setColor(color);
        eb.addField("Wear Value", String.format("%.8f", wear), true);
        eb.addField("Pattern", String.valueOf(pattern), true);
        eb.setImage(image);

        event.replyEmbeds(eb.build()).queue(
                success -> {
                    if (isRareSpecialItem) {
                        success.retrieveOriginal().queue(
                                message -> message.pin().queue(
                                        pinSuccess -> logger.info("Pinned rare item: {}", itemName),
                                        pinFailure -> logger.error("Failed to pin", pinFailure)
                                )
                        );
                    }
                }
        );
    }

    private static String getWearText(double randomWear) {
        if (randomWear < 0.07) return "Factory New";
        if (randomWear < 0.15) return "Minimal Wear";
        if (randomWear < 0.38) return "Field-Tested";
        if (randomWear < 0.45) return "Well-Worn";
        return "Battle-Scarred";
    }

    private static double getWear(JSONObject item) {
        String skinId = item.getString("id");
        JSONObject skinData = skinsCache.get(skinId);

        if (skinData != null && skinData.has("min_float") && skinData.has("max_float")) {
            try {
                double minFloat = skinData.getDouble("min_float");
                double maxFloat = skinData.getDouble("max_float");
                double randomWear = ThreadLocalRandom.current().nextDouble(minFloat, maxFloat);
                return Math.round(randomWear * 1e8) / 1e8;
            } catch (Exception e) {
                logger.error("Error calculating wear for skinID: {}", skinId, e);
            }
        }

        return ThreadLocalRandom.current().nextDouble(0.0, 1.0);
    }

    private static boolean isStatTrack() {
        return ThreadLocalRandom.current().nextDouble() < STAT_TRACK_PROB;
    }

    private static int patternIndex() {
        return ThreadLocalRandom.current().nextInt(1000) + 1;
    }

    private static String getRandomRarity() {
        double randomNumber = ThreadLocalRandom.current().nextDouble();

        if (randomNumber < MIL_SPEC_GRADE_PROB) return "Mil-Spec Grade";
        randomNumber -= MIL_SPEC_GRADE_PROB;

        if (randomNumber < RESTRICTED_PROB) return "Restricted";
        randomNumber -= RESTRICTED_PROB;

        if (randomNumber < CLASSIFIED_PROB) return "Classified";
        randomNumber -= CLASSIFIED_PROB;

        if (randomNumber < COVERT_PROB) return "Covert";
        randomNumber -= COVERT_PROB;

        if (randomNumber < EXCEEDINGLY_RARE_PROB) return "Rare Special Item";

        return "Common";
    }

    private static int getRarityColor(String rarity) {
        return switch (rarity) {
            case "Mil-Spec Grade" -> 0x4D69FF;
            case "Restricted" -> 0x8847FF;
            case "Classified" -> 0xD32CE6;
            case "Covert" -> 0xEB4B4B;
            case "Rare Special Item" -> 0xFFD700;
            default -> 0x000000;
        };
    }

    private static ItemResult getRandomItemWithRarity(String caseId, String rarity) {
        Map<String, List<JSONObject>> rarityMap = caseRarityCache.get(caseId);
        if (rarityMap == null) {
            logger.error("No rarity map found for caseId: {}", caseId);
            return null;
        }

        List<JSONObject> items = rarityMap.get(rarity);
        if (items == null || items.isEmpty()) {
            logger.warn("No items found for rarity {} in case {}", rarity, caseId);
            return null;
        }

        JSONObject randomItem = items.get(ThreadLocalRandom.current().nextInt(items.size()));
        double wear = getWear(randomItem);

        return new ItemResult(randomItem, wear);
    }

    static class ItemResult {
        final JSONObject item;
        final double wear;

        ItemResult(JSONObject item, double wear) {
            this.item = item;
            this.wear = wear;
        }
    }
}