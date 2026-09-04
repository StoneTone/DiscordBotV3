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

        // Try single-ffmpeg mode first (less CPU — important for Pi/ARM)
        // yt-dlp -g returns the direct stream URL, then one ffmpeg handles HLS + PCM conversion
        String streamUrl = getStreamUrl(url);

        InputStream pcmStream;
        if (streamUrl != null) {
            log.info("Using direct stream URL with single ffmpeg process");
            ProcessBuilder ffmpegPb = new ProcessBuilder("ffmpeg",
                    "-reconnect", "1",
                    "-reconnect_streamed", "1",
                    "-reconnect_delay_max", "5",
                    "-i", streamUrl,
                    "-f", "s16le", "-ar", String.valueOf(SAMPLE_RATE),
                    "-ac", String.valueOf(CHANNELS),
                    "-loglevel", "warning",
                    "pipe:1");
            Process ffmpegProcess = ffmpegPb.start();
            this.processes = List.of(ffmpegProcess);
            logProcessStderr(ffmpegProcess, "ffmpeg");
            monitorProcess(ffmpegProcess, "ffmpeg");
            pcmStream = ffmpegProcess.getInputStream();
        } else {
            log.warn("Could not get direct URL, falling back to yt-dlp | ffmpeg pipeline");
            ProcessBuilder ytdlpPb = new ProcessBuilder(ytdlpPath, "-f", "bestaudio", "--no-part", "-o", "-", url);
            ProcessBuilder ffmpegPb = new ProcessBuilder("ffmpeg", "-i", "pipe:0", "-f", "s16le", "-ar",
                    String.valueOf(SAMPLE_RATE), "-ac", String.valueOf(CHANNELS), "pipe:1", "-loglevel", "warning");
            processes = ProcessBuilder.startPipeline(List.of(ytdlpPb, ffmpegPb));
            logProcessStderr(processes.get(0), "yt-dlp");
            logProcessStderr(processes.get(1), "ffmpeg");
            monitorProcess(processes.get(0), "yt-dlp");
            monitorProcess(processes.get(1), "ffmpeg");
            pcmStream = processes.get(1).getInputStream();
        }

        try {
            executor.executeProcessingLoop(() -> {
                AudioProcessingContext context = executor.getProcessingContext();
                AudioPipeline audioPipeline = AudioPipelineFactory.create(context, new PcmFormat(CHANNELS, SAMPLE_RATE));

                try {
                    byte[] buffer = new byte[PCM_FRAME_SIZE];
                    short[] samples = new short[960 * CHANNELS];
                    long framesProcessed = 0;

                    log.info("Pipeline ready, waiting for PCM audio...");

                    while (true) {
                        int totalRead = readFully(pcmStream, buffer);
                        if (totalRead < PCM_FRAME_SIZE) {
                            log.info("Stream ended | frames processed: {} | last read: {} bytes",
                                    framesProcessed, totalRead);
                            break;
                        }

                        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                        audioPipeline.process(samples, 0, samples.length);
                        framesProcessed++;

                        if (framesProcessed == 1) {
                            log.info("First audio frame received from pipeline");
                        } else if (framesProcessed % 3000 == 0) {
                            // Log every 60 seconds (3000 frames * 20ms = 60s)
                            log.debug("Stream alive | frames: {} | ~{}s played", framesProcessed, framesProcessed / 50);
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

    private String getStreamUrl(String url) {
        try {
            ProcessBuilder pb = new ProcessBuilder(ytdlpPath, "-g", "-f", "bestaudio", url);
            Process process = pb.start();
            logProcessStderr(process, "yt-dlp");
            String output = new String(process.getInputStream().readAllBytes()).trim();
            int exitCode = process.waitFor();
            if (exitCode != 0 || output.isEmpty()) {
                log.warn("yt-dlp -g failed with exit code {}", exitCode);
                return null;
            }
            // yt-dlp may return multiple lines (video + audio), take the last one
            String[] lines = output.split("\n");
            String streamUrl = lines[lines.length - 1].trim();
            log.info("Got direct stream URL ({} chars)", streamUrl.length());
            return streamUrl;
        } catch (Exception e) {
            log.warn("Failed to get stream URL: {}", e.getMessage());
            return null;
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
            log.info("Destroyed processes for: {}", trackInfo.title);
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
