package com.bot.discordbotv3.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Source manager that handles live streams via yt-dlp + ffmpeg pipeline.
 * Identifiers use the format: "ytdlp-live:TITLE\tAUTHOR\tARTWORK_URL\tURL"
 */
public class YtDlpLiveSourceManager implements AudioSourceManager {

    private static final Logger log = LoggerFactory.getLogger(YtDlpLiveSourceManager.class);
    public static final String PREFIX = "ytdlp-live:";
    private static final String SEPARATOR = "\t";

    private final String ytdlpPath;

    public YtDlpLiveSourceManager(String ytdlpPath) {
        this.ytdlpPath = ytdlpPath;
    }

    /**
     * Build an identifier string from track metadata.
     */
    public static String buildIdentifier(AudioTrackInfo originalInfo) {
        return PREFIX + originalInfo.title + SEPARATOR +
                originalInfo.author + SEPARATOR +
                (originalInfo.artworkUrl != null ? originalInfo.artworkUrl : "") + SEPARATOR +
                originalInfo.uri;
    }

    @Override
    public String getSourceName() {
        return "ytdlp-live";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        if (!reference.identifier.startsWith(PREFIX)) {
            return null;
        }

        String data = reference.identifier.substring(PREFIX.length());
        String[] parts = data.split(SEPARATOR, 4);

        if (parts.length < 4) {
            log.warn("Invalid ytdlp-live identifier: {}", reference.identifier);
            return null;
        }

        String title = parts[0];
        String author = parts[1];
        String artworkUrl = parts[2].isEmpty() ? null : parts[2];
        String url = parts[3];

        AudioTrackInfo trackInfo = new AudioTrackInfo(
                title, author, Long.MAX_VALUE, reference.identifier, true, url, artworkUrl, null
        );

        log.info("Loading yt-dlp live track | Title: {} | URL: {}", title, url);
        return new YtDlpLiveAudioTrack(trackInfo, ytdlpPath, this);
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return false;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        // Not encodable
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return null;
    }

    @Override
    public void shutdown() {
        // Nothing to clean up
    }
}
