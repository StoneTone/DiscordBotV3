package com.bot.discordbotv3.options;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class ActivityOptions {

    public static OptionData handleActivityOptions(){
        OptionData options = new OptionData(OptionType.STRING, "status", "What do you want the bot doing", true);
        options.addChoice("Listening", "listen");
        options.addChoice("Competing", "competing");
        options.addChoice("Playing", "playing");
        options.addChoice("Watching", "watching");
        options.addChoice("Custom", "custom");
        return options;
    }
}
