FROM eclipse-temurin:25-jre-noble

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    python3 \
    libstdc++6 \
    ffmpeg \
    unzip \
    && ARCH=$(uname -m) \
    && curl -fsSL "https://github.com/denoland/deno/releases/latest/download/deno-${ARCH}-unknown-linux-gnu.zip" -o /tmp/deno.zip \
    && unzip /tmp/deno.zip -d /usr/local/bin/ \
    && chmod +x /usr/local/bin/deno \
    && rm /tmp/deno.zip \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

RUN mkdir -p /app/tmp

COPY build/libs/*.jar app.jar
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -s -o /dev/null http://localhost:8081/ || exit 1

ENTRYPOINT ["./entrypoint.sh"]
