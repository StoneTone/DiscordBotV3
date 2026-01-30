package com.bot.discordbotv3.service;

import com.bot.discordbotv3.vo.LofiTrack;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class YtDlpService {

    private static final Logger logger = LoggerFactory.getLogger(YtDlpService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${lofi.channel-url:https://www.youtube.com/@LofiGirl/streams}")
    private String channelUrl;

    @Value("${lofi.ytdlp-path:yt-dlp}")
    private String ytdlpPath;

    @PostConstruct
    public void init() {
        logVersion();
    }

    private void logVersion() {
        try {
            ProcessBuilder pb = new ProcessBuilder(ytdlpPath, "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String version = reader.readLine();
                if (version != null) {
                    logger.info("yt-dlp version: {}", version);
                }
            }

            process.waitFor(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Could not determine yt-dlp version: {}", e.getMessage());
        }
    }

    public List<LofiTrack> getLiveStreams() {
        List<LofiTrack> tracks = new ArrayList<>();

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    ytdlpPath,
                    "-j",
                    "--flat-playlist",
                    "--extractor-args", "youtube:player_client=web",
                    channelUrl
            );
            pb.redirectErrorStream(false);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                String line;
                long id = 1;
                while ((line = reader.readLine()) != null) {
                    try {
                        JsonNode json = objectMapper.readTree(line);
                        String liveStatus = json.has("live_status") ? json.get("live_status").asText() : null;

                        if ("is_live".equals(liveStatus)) {
                            String videoId = json.has("id") ? json.get("id").asText() : null;
                            String title = json.has("title") ? json.get("title").asText() : "Unknown";
                            String url = json.has("url") ? json.get("url").asText() :
                                    (videoId != null ? "https://www.youtube.com/watch?v=" + videoId : null);

                            if (videoId != null && url != null) {
                                LofiTrack track = new LofiTrack(id++, videoId, title, url, LocalDateTime.now());
                                tracks.add(track);
                                logger.debug("Found live stream: {} - {}", videoId, title);
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to parse yt-dlp output line: {}", e.getMessage());
                    }
                }

                StringBuilder errorOutput = new StringBuilder();
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
                if (!errorOutput.isEmpty()) {
                    logger.debug("yt-dlp stderr: {}", errorOutput.toString().trim());
                }
            }

            boolean finished = process.waitFor(120, TimeUnit.SECONDS);
            if (!finished) {
                logger.warn("yt-dlp process timed out");
                process.destroyForcibly();
            } else if (process.exitValue() != 0) {
                logger.warn("yt-dlp exited with code: {}", process.exitValue());
            }

            logger.info("Found {} live streams from LofiGirl channel", tracks.size());

        } catch (Exception e) {
            logger.error("Failed to fetch live streams: {}", e.getMessage());
        }

        return tracks;
    }
}
