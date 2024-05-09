package com.bot.discordbotv3.cnfg;

import com.bot.discordbotv3.listener.Listeners;
import io.github.cdimascio.dotenv.Dotenv;
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

    @Value("${DISCORD_TOKEN}")
    private String discordToken;

    @Value("${GUILD_ID}")
    private long guildId;

    @Value("${OWNER_ID}")
    private long ownerId;

    @Value("${YOUTUBE_SECRET}")
    private String youtubeSecret;

    @Value("${GPT_SECRET}")
    private String gptSecret;

    @Bean
    public JDA jda() throws LoginException {
        JDABuilder builder = JDABuilder.createDefault(discordToken);
        builder.enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));
        builder.addEventListeners(new Listeners(guildId, ownerId, youtubeSecret, gptSecret));
        builder.setActivity(Activity.customStatus("Not taking over the planet"));
        return builder.build();
    }
}
