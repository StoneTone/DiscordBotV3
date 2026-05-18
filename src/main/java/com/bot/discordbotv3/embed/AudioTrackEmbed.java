package com.bot.discordbotv3.embed;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.time.Duration;
import java.time.Instant;

public class AudioTrackEmbed {

    private static final int COLOR_NOW_PLAYING = 0x1DB954;  // spotify-green accent
    private static final int COLOR_QUEUED = 0x5865F2;       // discord blurple

    public static void audioTrackEmbedBuilder(AudioTrackInfo info, InteractionHook hook, boolean nowPlaying, int queueSize) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        Member member = hook.getInteraction().getMember();

        // Author line with user avatar — puts the requester info at the top cleanly
        embedBuilder.setAuthor(
                member.getEffectiveName(),
                null,
                member.getEffectiveAvatarUrl()
        );

        if (nowPlaying) {
            embedBuilder.setTitle("🎶 Now Playing");
            embedBuilder.setColor(COLOR_NOW_PLAYING);
        } else {
            embedBuilder.setTitle("📥 Added to Queue");
            embedBuilder.setColor(COLOR_QUEUED);
        }

        // Title as a clickable hyperlink in the description
        String description = "**[" + info.title + "](" + info.uri + ")**"
                + "\nby " + info.author;
        embedBuilder.setDescription(description);

        // Duration + Queue as inline fields
        if (info.isStream) {
            embedBuilder.addField("Duration", "🔴 Live", true);
        } else {
            embedBuilder.addField("Duration", "`" + formatDuration(info.length) + "`", true);
        }
        embedBuilder.addField("Queue", "`" + queueSize + "`", true);

        // Artwork — try to get the highest res version for YouTube thumbnails
        String artworkUrl = info.artworkUrl;
        if (artworkUrl != null && artworkUrl.contains("ytimg.com")) {
            artworkUrl = artworkUrl.replaceAll(
                    "(hqdefault|mqdefault|sddefault|default)",
                    "maxresdefault"
            );
        }
        embedBuilder.setImage(artworkUrl);

        // Timestamp in the footer — shows when the track was requested
        embedBuilder.setTimestamp(Instant.now());

        hook.editOriginalEmbeds(embedBuilder.build()).queue();
    }

    private static String formatDuration(long milliseconds) {
        Duration duration = Duration.ofMillis(milliseconds);
        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds %= 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }
}
