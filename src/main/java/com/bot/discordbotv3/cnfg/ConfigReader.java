package com.bot.discordbotv3.cnfg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class ConfigReader {
    @Autowired
    private Environment env;

    public String getPropValue(String prop){ return env.getProperty(prop); }
}
