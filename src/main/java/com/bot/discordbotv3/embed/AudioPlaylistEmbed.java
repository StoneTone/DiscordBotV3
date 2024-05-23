package com.bot.discordbotv3.embed;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class AudioPlaylistEmbed {
    public static void audioPlaylistEmbedBuilder(AudioTrackInfo info, InteractionHook hook, int size){
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Playlist Added");
        embedBuilder.setDescription("**Name:** `" + info.title + "`");
        embedBuilder.setThumbnail(info.artworkUrl);
        embedBuilder.addField("Author", info.author, true);
        embedBuilder.setUrl(info.uri);
        embedBuilder.addField("Queue", String.valueOf(size), true);
        embedBuilder.addField("Requested By", hook.getInteraction().getUser().getEffectiveName(), true);
        hook.editOriginalEmbeds(embedBuilder.build()).queue();
    }
}
