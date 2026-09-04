package com.bot.discordbotv3.lavaplayer;

import com.sedmelluq.discord.lavaplayer.filter.AudioPipeline;
import com.sedmelluq.discord.lavaplayer.filter.AudioPipelineFactory;
import com.sedmelluq.discord.lavaplayer.filter.PcmFormat;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BaseAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioProcessingContext;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class YtDlpLiveAudioTrack extends BaseAudioTrack {

    private static final Logger log = LoggerFactory.getLogger(YtDlpLiveAudioTrack.class);
    private static final int SAMPLE_RATE = 48000;
    private static final int CHANNELS = 2;
    // 20ms of audio at 48kHz stereo 16-bit = 960 samples * 2 channels * 2 bytes
    private static final int PCM_FRAME_SIZE = 960 * CHANNELS * 2;

    private final String ytdlpPath;
    private final YtDlpLiveSourceManager sourceManager;
    private volatile List<Process> processes;

    public YtDlpLiveAudioTrack(AudioTrackInfo trackInfo, String ytdlpPath, YtDlpLiveSourceManager sourceManager) {
        super(trackInfo);
        this.ytdlpPath = ytdlpPath;
        this.sourceManager = sourceManager;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        String url = trackInfo.uri;
        log.info("Starting yt-dlp live stream | Title: {} | URL: {} | yt-dlp path: {}", trackInfo.title, url, ytdlpPath);

        ProcessBuilder ytdlpPb = new ProcessBuilder(ytdlpPath,
                "-f", "bestaudio", "--no-part", "--js-runtimes", "nodejs", "-o", "-", url);
        ProcessBuilder ffmpegPb = new ProcessBuilder("ffmpeg", "-i", "pipe:0", "-f", "s16le", "-ar",
                String.valueOf(SAMPLE_RATE), "-ac", String.valueOf(CHANNELS), "pipe:1", "-loglevel", "warning");

        log.info("yt-dlp command: {} -f bestaudio --no-part --js-runtimes nodejs -o - {}", ytdlpPath, url);

        processes = ProcessBuilder.startPipeline(List.of(ytdlpPb, ffmpegPb));
        Process ytdlpProcess = processes.get(0);
        Process ffmpegProcess = processes.get(1);

        // Log stderr from both processes
        logProcessStderr(ytdlpProcess, "yt-dlp");
        logProcessStderr(ffmpegProcess, "ffmpeg");

        // Monitor process exit codes
        monitorProcess(ytdlpProcess, "yt-dlp");
        monitorProcess(ffmpegProcess, "ffmpeg");

        InputStream pcmStream = ffmpegProcess.getInputStream();

        try {
            executor.executeProcessingLoop(() -> {
                AudioProcessingContext context = executor.getProcessingContext();
                AudioPipeline audioPipeline = AudioPipelineFactory.create(context, new PcmFormat(CHANNELS, SAMPLE_RATE));

                try {
                    byte[] buffer = new byte[PCM_FRAME_SIZE];
                    short[] samples = new short[960 * CHANNELS];
                    long framesProcessed = 0;

                    log.info("yt-dlp pipeline ready, waiting for PCM audio...");

                    while (true) {
                        int totalRead = readFully(pcmStream, buffer);
                        if (totalRead < PCM_FRAME_SIZE) {
                            log.info("yt-dlp stream ended | frames processed: {} | last read: {} bytes",
                                    framesProcessed, totalRead);
                            break;
                        }

                        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                        audioPipeline.process(samples, 0, samples.length);
                        framesProcessed++;

                        if (framesProcessed == 1) {
                            log.info("First audio frame received from yt-dlp pipeline");
                        } else if (framesProcessed % 3000 == 0) {
                            // Log every 60 seconds (3000 frames * 20ms = 60s)
                            log.debug("yt-dlp stream alive | frames: {} | ~{}s played", framesProcessed, framesProcessed / 50);
                        }
                    }
                } finally {
                    audioPipeline.close();
                }
            }, null);
        } finally {
            destroyProcesses();
        }
    }

    private void logProcessStderr(Process process, String name) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String upper = line.toUpperCase();
                    if (upper.contains("WARNING") || upper.contains("ERROR") || upper.contains("FATAL")) {
                        log.warn("[{}] {}", name, line);
                    } else {
                        log.debug("[{}] {}", name, line);
                    }
                }
            } catch (IOException ignored) {
            }
        }, name + "-stderr");
        thread.setDaemon(true);
        thread.start();
    }

    private void monitorProcess(Process process, String name) {
        Thread thread = new Thread(() -> {
            try {
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    log.warn("{} process exited with code {}", name, exitCode);
                } else {
                    log.info("{} process exited normally", name);
                }
            } catch (InterruptedException ignored) {
            }
        }, name + "-monitor");
        thread.setDaemon(true);
        thread.start();
    }

    private int readFully(InputStream stream, byte[] buffer) throws InterruptedException {
        int totalRead = 0;
        try {
            while (totalRead < buffer.length) {
                int read = stream.read(buffer, totalRead, buffer.length - totalRead);
                if (read == -1) break;
                totalRead += read;
            }
        } catch (IOException e) {
            log.debug("Read interrupted: {}", e.getMessage());
        }
        return totalRead;
    }

    private void destroyProcesses() {
        List<Process> procs = this.processes;
        if (procs != null) {
            for (Process p : procs) {
                if (p.isAlive()) {
                    p.destroyForcibly();
                }
            }
            log.info("Destroyed yt-dlp/ffmpeg processes for: {}", trackInfo.title);
        }
    }

    @Override
    public void stop() {
        super.stop();
        destroyProcesses();
    }

    @Override
    public AudioTrack makeClone() {
        return new YtDlpLiveAudioTrack(trackInfo, ytdlpPath, sourceManager);
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return sourceManager;
    }

    @Override
    public boolean isSeekable() {
        return false;
    }
}
