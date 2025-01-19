package com.bot.discordbotv3.options;

import com.bot.discordbotv3.service.TwitchService;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Map;

public class TwitchChannelOptions {

    public static OptionData handleTwitchChannelOptions(TwitchService twitchService){
        OptionData data = new OptionData(OptionType.STRING, "channel", "Select which channel to modify", true);
        if(twitchService.channelMap.isEmpty()){
            data.addChoice("No Active Channels", "n/a");
        }else{
            for(Map.Entry<String, Map<String, String>> entry: twitchService.channelMap.entrySet()){
                for(Map.Entry<String, String> channel: entry.getValue().entrySet()){
                    data.addChoice(channel.getKey(), channel.getKey());
                }
            }
        }
        return data;
    }
}
