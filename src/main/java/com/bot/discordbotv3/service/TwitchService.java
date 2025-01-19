package com.bot.discordbotv3.service;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TwitchService {
    private final Logger logger = LoggerFactory.getLogger(TwitchService.class);
    private final TwitchClient twitchClient;
    //Map<TextChannelId, Map<ChannelName, Msg>>
    public HashMap<String, Map<String, String>> channelMap = new HashMap<>();

    private JDA jda;

    public TwitchService(TwitchClient twitchClient) {
        this.twitchClient = twitchClient;
        setupEventListeners();
    }

    public HashMap<String, Map<String, String>> getChannelMap() {
        return channelMap;
    }

    private void setupEventListeners() {
        twitchClient.getEventManager().onEvent(ChannelGoLiveEvent.class, this::handleStreamUp);
    }

    public void setJda(JDA jda) {
        this.jda = jda;
    }

    private void handleStreamUp(ChannelGoLiveEvent event) {

        for(Map.Entry<String, Map<String, String>> textEntry : channelMap.entrySet()){
            if(textEntry.getValue().containsKey(event.getChannel().getName())){
                TextChannel channel = jda.getTextChannelById(textEntry.getKey());
                if (channel != null) {
                    for(Map.Entry<String, String> channelEntry : textEntry.getValue().entrySet()){
                        if(channelEntry.getKey().equals(event.getChannel().getName())) {
                            channel.sendMessage("@here, " + channelEntry.getValue()).addEmbeds(createEmbed(event)).queue();
                        }
                        logger.info("Sent live notification for streamer: {}", event.getChannel().getName());
                    }

                } else {
                    logger.error("Could not find Text Channel with ID: {}", textEntry.getKey());
                }
            }
        }
    }

    private MessageEmbed createEmbed(ChannelGoLiveEvent event){
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle(event.getStream().getTitle());
        embed.setUrl("https://twitch.tv/" + event.getChannel().getName());
        embed.setImage(event.getStream().getThumbnailUrl(700, 400));
        embed.addField("Game", event.getStream().getGameName(), true);
        embed.addField("Viewers", String.valueOf(event.getStream().getViewerCount()), true);
        embed.setColor(-7256321);

        return embed.build();
    }

    public void listenToChannel(String channelName, String textChannelId, String message) {
        twitchClient.getClientHelper().enableStreamEventListener(channelName);
        if(!channelMap.containsKey(textChannelId)){
            HashMap<String,String> innerChannel = new HashMap<>();
            innerChannel.put(channelName, message);
            channelMap.put(textChannelId, innerChannel);
        }else{
            Map<String, String> twitchMap = channelMap.get(textChannelId);
            if(twitchMap.containsKey(channelName) && !twitchMap.get(channelName).equals(message)){
                twitchMap.put(channelName, message);
            }
        }
        logger.info("Started listening to Twitch channel: {}", channelName);
    }

    public void stopListeningToChannel(String channelName) {
        twitchClient.getClientHelper().disableStreamEventListener(channelName);
        for(Map.Entry<String, Map<String, String>> textEntry : channelMap.entrySet()){
            for(Map.Entry<String, String> channelEntry : textEntry.getValue().entrySet()){
                if(channelEntry.getKey().equals(channelName)){
                    textEntry.getValue().remove(channelName);
                    break;
                }
            }
        }
        logger.info("Stopped listening to Twitch channel: {}", channelName);
    }

}
