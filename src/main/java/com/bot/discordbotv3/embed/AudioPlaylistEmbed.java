package com.bot.discordbotv3.embed;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.time.Instant;

public class AudioPlaylistEmbed {

    private static final int COLOR_PLAYLIST = 0xE91E63; // pink accent to distinguish from single tracks

    public static void audioPlaylistEmbedBuilder(AudioTrackInfo info, InteractionHook hook, int size) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        Member member = hook.getInteraction().getMember();

        embedBuilder.setAuthor(
                member.getEffectiveName(),
                null,
                member.getEffectiveAvatarUrl()
        );

        embedBuilder.setTitle("📋 Playlist Added");
        embedBuilder.setColor(COLOR_PLAYLIST);

        String description = "**[" + info.title + "](" + info.uri + ")**"
                + "\nby " + info.author;
        embedBuilder.setDescription(description);

        embedBuilder.addField("Tracks", "`" + size + "`", true);

        String artworkUrl = info.artworkUrl;
        if (artworkUrl != null && artworkUrl.contains("ytimg.com")) {
            artworkUrl = artworkUrl.replaceAll(
                    "(hqdefault|mqdefault|sddefault|default)",
                    "maxresdefault"
            );
        }
        embedBuilder.setThumbnail(artworkUrl);

        embedBuilder.setTimestamp(Instant.now());

        hook.editOriginalEmbeds(embedBuilder.build()).queue();
    }
}
