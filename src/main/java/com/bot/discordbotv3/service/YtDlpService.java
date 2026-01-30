package com.bot.discordbotv3.service;

import com.bot.discordbotv3.vo.LofiTrack;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class YtDlpService {

    private static final Logger logger = LoggerFactory.getLogger(YtDlpService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;

    @Value("${lofi.ytdlp-service-url:http://yt-dlp:8080}")
    private String ytdlpServiceUrl;

    @Value("${lofi.channel-url:https://www.youtube.com/@LofiGirl/streams}")
    private String channelUrl;

    @Autowired
    public YtDlpService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        logVersion();
    }

    private void logVersion() {
        try {
            String versionUrl = ytdlpServiceUrl + "/version";
            String response = restTemplate.getForObject(versionUrl, String.class);
            JsonNode json = objectMapper.readTree(response);
            String version = json.has("version") ? json.get("version").asText() : "unknown";
            logger.info("yt-dlp service version: {}", version);
        } catch (Exception e) {
            logger.warn("Could not determine yt-dlp version: {}", e.getMessage());
        }
    }

    public List<LofiTrack> getLiveStreams() {
        List<LofiTrack> tracks = new ArrayList<>();

        try {
            String url = ytdlpServiceUrl + "/live-streams?url=" + channelUrl;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode json = objectMapper.readTree(response);

            if (json.has("error") && !json.get("error").isNull()) {
                logger.error("yt-dlp service error: {}", json.get("error").asText());
            }

            if (json.has("tracks")) {
                for (JsonNode trackNode : json.get("tracks")) {
                    LofiTrack track = new LofiTrack(
                            trackNode.get("id").asLong(),
                            trackNode.get("videoId").asText(),
                            trackNode.get("titleName").asText(),
                            trackNode.get("url").asText(),
                            LocalDateTime.now()
                    );
                    tracks.add(track);
                }
            }

            logger.info("Found {} live streams from LofiGirl channel", tracks.size());

        } catch (RestClientException e) {
            logger.error("Failed to connect to yt-dlp service: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to fetch live streams: {}", e.getMessage());
        }

        return tracks;
    }
}
