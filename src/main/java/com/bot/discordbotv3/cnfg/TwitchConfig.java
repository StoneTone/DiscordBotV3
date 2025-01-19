package com.bot.discordbotv3.cnfg;

import com.bot.discordbotv3.service.TwitchService;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwitchConfig {

    @Value("${TWITCH_CLIENT_ID}")
    private String twitchClientId;

    @Value("${TWITCH_CLIENT_SECRET}")
    private String twitchClientSecret;


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
