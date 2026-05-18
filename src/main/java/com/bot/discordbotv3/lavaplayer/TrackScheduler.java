package com.bot.discordbotv3.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class TrackScheduler extends AudioEventAdapter {

    private static final Logger log = LoggerFactory.getLogger(TrackScheduler.class);
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue = new LinkedBlockingQueue<>();
    private boolean isRepeat = false;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        log.info("Track started | Title: {} | Source: {}",
                track.getInfo().title, track.getSourceManager().getSourceName());
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        log.info("Track ended | Title: {} | Reason: {}", track.getInfo().title, endReason);
        if (endReason.mayStartNext) {
            if (isRepeat) {
                player.startTrack(track.makeClone(), false);
            } else {
                AudioTrack next = queue.poll();
                if (next != null) {
                    log.info("Next track | Title: {}", next.getInfo().title);
                } else {
                    log.info("Queue empty, no more tracks");
                }
                player.startTrack(next, false);
            }
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        log.error("Track exception | Title: {} | Error: {}",
                track.getInfo().title, exception.getMessage(), exception);
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        log.warn("Track stuck | Title: {} | Threshold: {}ms — skipping",
                track.getInfo().title, thresholdMs);
        player.startTrack(queue.poll(), false);
    }

    public void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
            log.info("Queued | Title: {} | Queue size: {}", track.getInfo().title, queue.size());
        }
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean repeat) {
        isRepeat = repeat;
    }
}
