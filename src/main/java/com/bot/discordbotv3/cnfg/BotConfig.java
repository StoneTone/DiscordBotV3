package com.bot.discordbotv3.cnfg;

import com.bot.discordbotv3.cnst.CmnCnst;
import com.bot.discordbotv3.listener.Listeners;
import com.bot.discordbotv3.service.TwitchService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.login.LoginException;

@Configuration
public class BotConfig {

    private static final Logger log = LoggerFactory.getLogger(BotConfig.class);

    @Autowired
    private ConfigReader cnfgRdr;

    @Bean
    public JDA jda(TwitchService twitchService) throws LoginException {
        JDABuilder builder = JDABuilder.createDefault(cnfgRdr.getPropValue(CmnCnst.TOKEN));
        builder.enableIntents(GatewayIntent.getIntents(GatewayIntent.ALL_INTENTS));
        builder.addEventListeners(new Listeners(Long.parseLong(cnfgRdr.getPropValue(CmnCnst.GUILDID)), twitchService, cnfgRdr.getPropValue(CmnCnst.GPTSECRET),
                cnfgRdr.getPropValue(CmnCnst.GPTMODEL)));
        if(cnfgRdr.getPropValue(CmnCnst.GPTSECRET).isEmpty()){
            log.warn("GPT token is null. Some features may not work!");
        }
        JDA jda =  builder.build();
        twitchService.setJda(jda);
        return jda;
    }
}
