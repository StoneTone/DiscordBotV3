package com.bot.discordbotv3.cnfg;

import com.bot.discordbotv3.listener.Listeners;
import com.bot.discordbotv3.service.TwitchService;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;

@Configuration
public class BotConfig {

    private static final Logger log = LoggerFactory.getLogger(BotConfig.class);

    @Value("${DISCORD_TOKEN}")
    private String discordToken;

    @Value("${GUILD_ID}")
    private long guildId;

    @Value("${GPT_SECRET:#{null}}")
    private String gptSecret;

    @Bean
    public JDA jda(TwitchService twitchService) throws LoginException {
        JDABuilder builder = JDABuilder.createDefault(discordToken);
        builder.enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));
        builder.addEventListeners(new Listeners(guildId, gptSecret, twitchService));
        JDA jda =  builder.build();
        twitchService.setJda(jda);
        return jda;
    }

    @PostConstruct
    public void init() {
        if (gptSecret == null) {
            log.warn("GPT some features may not work!");
        }
    }
}
