package com.bot.discordbotv3.cnfg;

import com.bot.discordbotv3.cnst.CmnCnst;
import com.bot.discordbotv3.service.TwitchService;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TwitchConfig {

    private static final Logger log = LoggerFactory.getLogger(TwitchConfig.class);

    @Autowired
    private ConfigReader cnfgRdr;

    @PostConstruct
    public void init() {
        if (cnfgRdr.getPropValue(CmnCnst.TWITCHID).isEmpty() || cnfgRdr.getPropValue(CmnCnst.TWITCHSECRET).isEmpty()) {
            log.warn("Twitch integration is disabled due to missing credentials");
        }
    }

    @Bean
    public TwitchClient twitchClient() {
        return TwitchClientBuilder.builder()
                .withClientId(cnfgRdr.getPropValue(CmnCnst.TWITCHID))
                .withClientSecret(cnfgRdr.getPropValue(CmnCnst.TWITCHSECRET))
                .withEnableHelix(true)
                .build();
    }

    @Bean
    public TwitchService twitchService(TwitchClient twitchClient) {
        return new TwitchService(twitchClient);
    }
}
