package com.bot.discordbotv3.cnfg;

import com.bot.discordbotv3.service.TwitchService;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwitchConfig {

    private static final Logger log = LoggerFactory.getLogger(TwitchConfig.class);

    @Value("${TWITCH_CLIENT_ID:#{null}}")
    private String twitchClientId;

    @Value("${TWITCH_CLIENT_SECRET:#{null}}")
    private String twitchClientSecret;

    @PostConstruct
    public void init() {
        if (twitchClientId == null || twitchClientSecret == null) {
            log.warn("Twitch integration is disabled due to missing credentials");
        }
    }

    @Bean
    public TwitchClient twitchClient() {
        return TwitchClientBuilder.builder()
                .withClientId(twitchClientId)
                .withClientSecret(twitchClientSecret)
                .withEnableHelix(true)
                .build();
    }

    @Bean
    public TwitchService twitchService(TwitchClient twitchClient) {
        return new TwitchService(twitchClient);
    }
}
