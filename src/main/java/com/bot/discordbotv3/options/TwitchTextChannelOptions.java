package com.bot.discordbotv3.options;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwitchTextChannelOptions {
    private static final Logger logger = LoggerFactory.getLogger(TwitchTextChannelOptions.class);
    public static OptionData handleTwitchTextChannelOptions(Guild guild){
        OptionData data = new OptionData(OptionType.STRING, "textchannel", "Select the text channel to send notifications to", true);
        try{
            for(TextChannel channel: guild.getTextChannels()){
                data.addChoice(channel.getName(), channel.getId());
            }
        }catch (Exception e){
            logger.error("Failed to fetch text channels: {}", e.getMessage(), e);
        }
        return data;
    }
}
