package com.bot.discordbotv3.config;

import com.bot.discordbotv3.listener.Listeners;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;

@Configuration
public class BotConfig {

    @Value("${discord.token}")
    private String token;

    @Bean
    public JDA jda() throws LoginException {
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));
        builder.addEventListeners(new Listeners());
        return builder.build();
    }
}
