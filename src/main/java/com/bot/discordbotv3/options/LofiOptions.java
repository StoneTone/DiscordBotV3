package com.bot.discordbotv3.options;

import com.bot.discordbotv3.service.LofiService;
import com.bot.discordbotv3.vo.LofiTrack;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LofiOptions {

    private static final Logger logger = LoggerFactory.getLogger(LofiOptions.class);

    public static OptionData handleLofiOptions() {
        try {
            List<LofiTrack> liveStreams = LofiService.getLofiTracks();
            String timestamp = LocalDateTime.now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss"));

            OptionData options = new OptionData(OptionType.STRING, "type", "Pick what type of lofi sound (Last Refreshed: " + timestamp + " UTC)", true);

            if (liveStreams != null && !liveStreams.isEmpty()) {
                for (LofiTrack lives : liveStreams) {
                    options.addChoice(lives.getTitleName(), lives.getUrl());
                }
            } else {
                options.addChoice("No live streams available", "none");
            }

            return options;
        } catch (Exception e) {
            logger.error("Failed to fetch lofi options: {}", e.getMessage(), e);
            return new OptionData(OptionType.STRING, "type", "Error occurred while fetching lofi options", true)
                    .addChoice("Error", "error");
        }
    }

}
