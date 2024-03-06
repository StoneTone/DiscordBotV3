package com.bot.discordbotv3.config;

import com.bot.discordbotv3.listener.Listeners;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;

@Configuration
public class BotConfig {

    @Value("${discord.token}")
    private String token;

    private final long guildId;
    private final long ownerId;
    private String ytSecret;

    public BotConfig(@Value("${guild.id}") long guildId, @Value("${owner.id}") long ownerId, @Value("${youtube.secret}") String ytSecret) {
        this.guildId = guildId;
        this.ownerId = ownerId;
        this.ytSecret = ytSecret;
    }


    @Bean
    public JDA jda() throws LoginException {
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));
        builder.addEventListeners(new Listeners(guildId, ownerId, ytSecret));
        builder.setActivity(Activity.customStatus("Not taking over the planet"));
        return builder.build();
    }
}
