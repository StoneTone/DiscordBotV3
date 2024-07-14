package com.bot.discordbotv3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DiscordBotV3Application implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DiscordBotV3Application.class);

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(DiscordBotV3Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception{
        log.info("Joining thread, press Ctrl+C to shutdown application");
        Thread.currentThread().join();
    }

}
