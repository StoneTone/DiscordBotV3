package com.bot.discordbotv3.service;

import com.bot.discordbotv3.vo.LofiTrack;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

@Service
public class YtDlpService {

    private static final Logger logger = LoggerFactory.getLogger(YtDlpService.class);
    private static final Pattern TRAILING_TIMESTAMP = Pattern.compile("\\s+\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}[:\\-]\\d{2}$");
    private static final String YOUTUBE_WATCH_URL = "https://www.youtube.com/watch?v=";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Value("${lofi.channel-url:https://www.youtube.com/@LofiGirl/streams}")
    private String channelUrl;

    @Value("${lofi.ytdlp-path:yt-dlp}")
    private String ytdlpPath;

    @PostConstruct
    public void init() {
        logVersion();
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
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
        try {
            List<String> videoIds = getVideoIds();
            logger.info("Found {} videos on channel, checking for live streams...", videoIds.size());

            AtomicLong idCounter = new AtomicLong(1);

            List<LofiTrack> tracks = videoIds.stream()
                    .map(videoId -> CompletableFuture.supplyAsync(
                            () -> checkVideoLiveStatus(videoId, idCounter.getAndIncrement()),
                            executor
                    ))
                    .toList()
                    .stream()
                    .map(f -> {
                        try {
                            return f.get(30, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            logger.warn("Failed to check video: {}", e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingLong(LofiTrack::getId))
                    .toList();

            logger.info("Found {} live streams from LofiGirl channel", tracks.size());
            return tracks;

        } catch (Exception e) {
            logger.error("Failed to fetch live streams: {}", e.getMessage());
            return List.of();
        }
    }

    private List<String> getVideoIds() {
        List<String> videoIds = new ArrayList<>();

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    ytdlpPath, "-j", "--flat-playlist",
                    "--extractor-args", "youtube:player_client=web",
                    channelUrl
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        JsonNode json = objectMapper.readTree(line);
                        if (json.has("id")) {
                            videoIds.add(json.get("id").asText());
                        }
                    } catch (Exception ignored) {}
                }
            }

            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                logger.warn("yt-dlp playlist process timed out");
                process.destroyForcibly();
            }

        } catch (Exception e) {
            logger.error("Failed to fetch video IDs: {}", e.getMessage());
        }

        return videoIds;
    }

    private LofiTrack checkVideoLiveStatus(String videoId, long id) {
        String videoUrl = YOUTUBE_WATCH_URL + videoId;

        try {
            ProcessBuilder pb = new ProcessBuilder(ytdlpPath, "-j", videoUrl);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try {
                        JsonNode json = objectMapper.readTree(line);
                        if ("is_live".equals(json.path("live_status").asText(null))) {
                            String title = TRAILING_TIMESTAMP
                                    .matcher(json.path("title").asText("Unknown"))
                                    .replaceAll("");
                            return new LofiTrack(id, videoId, title, videoUrl);
                        }
                    } catch (Exception ignored) {}
                }
            }

            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                logger.warn("yt-dlp timed out for video: {}", videoId);
                process.destroyForcibly();
            }

        } catch (Exception e) {
            logger.warn("Failed to check video {}: {}", videoId, e.getMessage());
        }

        return null;
    }
}