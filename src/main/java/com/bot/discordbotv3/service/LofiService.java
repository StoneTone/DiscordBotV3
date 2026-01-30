package com.bot.discordbotv3.service;

import com.bot.discordbotv3.vo.LofiTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LofiService {

    private static final Logger logger = LoggerFactory.getLogger(LofiService.class);
    private static YtDlpService ytDlpService;

    @Autowired
    public LofiService(YtDlpService ytDlpService) {
        LofiService.ytDlpService = ytDlpService;
    }

    public static List<LofiTrack> getLofiTracks() {
        logger.info("Fetching Lofi tracks via yt-dlp");
        return ytDlpService.getLiveStreams();
    }
}
