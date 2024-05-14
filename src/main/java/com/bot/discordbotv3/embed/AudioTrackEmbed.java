package com.bot.discordbotv3.embed;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class AudioTrackEmbed {

    public static void audioTrackEmbedBuilder(AudioTrackInfo info, SlashCommandInteractionEvent event, boolean nowPlaying){
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if(nowPlaying){
            embedBuilder.setTitle("Now Playing");
        }else{
            embedBuilder.setTitle("Added to Queue");
        }
        embedBuilder.setDescription("**Name:** `" + info.title + "`");
        embedBuilder.setImage(info.artworkUrl);
        embedBuilder.addField("Author", info.author, true);
        embedBuilder.addField("URL", info.uri, true);
        embedBuilder.addField("Requested By", event.getUser().getEffectiveName(), true);
        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
