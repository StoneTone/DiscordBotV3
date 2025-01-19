package com.bot.discordbotv3.cnfg;

import com.bot.discordbotv3.listener.Listeners;
import com.bot.discordbotv3.service.TwitchService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;

@Configuration
public class BotConfig {

    @Value("${DISCORD_TOKEN}")
    private String discordToken;

    @Value("${GUILD_ID}")
    private long guildId;

    @Value("${YOUTUBE_SECRET}")
    private String youtubeSecret;

    @Value("${GPT_SECRET}")
    private String gptSecret;

    @Bean
    public JDA jda(TwitchService twitchService) throws LoginException {
        JDABuilder builder = JDABuilder.createDefault(discordToken);
        builder.enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));
        builder.addEventListeners(new Listeners(guildId, youtubeSecret, gptSecret, twitchService));
        JDA jda =  builder.build();
        twitchService.setJda(jda);
        return jda;
    }
}
