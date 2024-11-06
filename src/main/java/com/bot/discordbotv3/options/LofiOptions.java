package com.bot.discordbotv3.options;

import com.bot.discordbotv3.service.LofiService;
import com.bot.discordbotv3.vo.LofiTrack;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LofiOptions {

    private static final Logger logger = LoggerFactory.getLogger(LofiOptions.class);

    public static OptionData handleLofiOptions() {
        try {
            List<LofiTrack> liveStreams = LofiService.getLofiTracks();
            OptionData options = new OptionData(OptionType.STRING, "type", "Pick what type of lofi sound", true);

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
