package com.bot.discordbotv3.options;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class LofiOptions {
    public static OptionData handleLofiOptions(){
        return new OptionData(OptionType.STRING, "type", "Pick what type of lofi sound", true)
                .addChoice("Relax/Study", "beats to relax/study to")
                .addChoice("Sleep/Chill", "beats to sleep/chill to")
                .addChoice("Chill/Game", "beats to chill/game to")
                .addChoice("Focus/Study", "music to focus/study to")
                .addChoice("Escape/Dream", "music to escape/dream to");
    }
}
