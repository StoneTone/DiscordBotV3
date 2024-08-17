package com.bot.discordbotv3.embed;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.time.Duration;

public class AudioTrackEmbed {
    public static void audioTrackEmbedBuilder(AudioTrackInfo info, InteractionHook hook, boolean nowPlaying, int queueSize){
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if(nowPlaying){
            embedBuilder.setTitle("Now Playing");
        }else{
            embedBuilder.setTitle("Added to Queue");
        }
        embedBuilder.setDescription("**Name:** `" + info.title + "`");
        embedBuilder.setThumbnail(info.artworkUrl);
        embedBuilder.addField("Author", info.author, true);
        if(info.isStream){
            embedBuilder.addField("Duration","`" + "Live" + "`", true);
        }else{
            embedBuilder.addField("Duration","`" + formatDuration(info.length) + "`", true);
        }
        embedBuilder.addField("Queue", "`" + queueSize + "`", true);
        embedBuilder.addField("Requested By", hook.getInteraction().getUser().getEffectiveName(), true);
        embedBuilder.setUrl(info.uri);
        hook.editOriginalEmbeds(embedBuilder.build()).queue();
    }

    private static String formatDuration(long milliseconds){
        Duration duration = Duration.ofMillis(milliseconds);
        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds %= 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
