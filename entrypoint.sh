#!/bin/sh
echo "Downloading latest yt-dlp..."
curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp
chmod +x /usr/local/bin/yt-dlp
echo "yt-dlp version: $(yt-dlp --version)"
echo "Starting Discord Bot..."
exec java -jar app.jar
