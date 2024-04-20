package com.bot.discordbotv3.cnfg;

import com.bot.discordbotv3.listener.Listeners;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;

@Configuration
public class BotConfig {

    private final Dotenv dotenv;

    public BotConfig() {
        this.dotenv = Dotenv.configure().load();
    }

    private String getEnv(String name) {
        return dotenv.get(name);
    }

    private long getEnvAsLong(String name) {
        String value = getEnv(name);
        if (value == null) {
            throw new IllegalArgumentException("Environment variable " + name + " is not set");
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Environment variable " + name + " is not a valid long value");
        }
    }

    @Bean
    public JDA jda() throws LoginException {
        String token = getEnv("DISCORD_TOKEN");
        long guildId = getEnvAsLong("GUILD_ID");
        long ownerId = getEnvAsLong("OWNER_ID");
        String ytSecret = getEnv("YOUTUBE_SECRET");
        String gptSecret = getEnv("GPT_SECRET");
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));
        builder.addEventListeners(new Listeners(guildId, ownerId, ytSecret, gptSecret));
        builder.setActivity(Activity.customStatus("Not taking over the planet"));
        return builder.build();
    }
}
