package com.bot.discordbotv3.options;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class LofiOptions {

    private static final String BASE_YOUTUBE_URI = "https://www.youtube.com/watch?v=";
    private static final String CHANNEL_ID = "UCSJ4gkVC6NrvII8umztf0Ow";

    public static OptionData handleLofiOptions() {
        try {
            YouTube youtube = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    null)
                    .setApplicationName("LofiOptions")
                    .build();

            List<SearchResult> liveStreams = searchStream(youtube, System.getenv("YOUTUBE_SECRET"));
            OptionData searchOptions = new OptionData(OptionType.STRING, "type", "Pick what type of lofi sound", true);

            if (liveStreams != null && !liveStreams.isEmpty()) {
                for (SearchResult result : liveStreams) {
                    searchOptions.addChoice(result.getSnippet().getTitle(), BASE_YOUTUBE_URI + result.getId().getVideoId());
                }
            } else {
                searchOptions.addChoice("No live streams available", "none");
            }

            return searchOptions;
        } catch (Exception e) {
            e.printStackTrace();
            return new OptionData(OptionType.STRING, "type", "Error occurred while fetching lofi options", true)
                    .addChoice("Error", "error");
        }
    }

    private static List<SearchResult> searchStream(YouTube youtube, String ytSecret) throws Exception {
        YouTube.Search.List search = youtube.search().list("id,snippet");
        search.setKey(ytSecret);
        search.setChannelId(CHANNEL_ID);
        search.setEventType("live");
        search.setType("video");
        search.setFields("items(id/videoId,snippet/title)");

        SearchListResponse searchListResponse = search.execute();

        return searchListResponse.getItems() != null ? searchListResponse.getItems() : new ArrayList<>();
    }
}
