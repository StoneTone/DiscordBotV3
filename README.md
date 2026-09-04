# Java Discord Bot with JDA and Lavaplayer

## Introduction
A feature-rich Discord bot built with JDA (Java Discord API) and Lavaplayer. Supports YouTube music playback (including live streams), playlists, Twitch notifications, ChatGPT integration, and more.

## Setup
### Discord Portal
1. Go to the [Discord Developer](https://discord.com/developers) portal
2. Click `New Application`
3. Customize however you'd like
4. Click `Bot` Tab then `Reset Secret`
5. Copy your token (Keep this secure and do not share with anyone!)
6. Scroll down to `Privileged Gateway Intents` and enable all intents (This is required for the bot to function properly)
7. Click `OAuth2` Tab
8. In the OAuth2 URL Generator, make sure to select (bot, application.commands)
9. Select administrator permissions
10. Copy the generated url and paste into your browser and invite it to your server
11. Move your bot role to the highest priority in your server `Server Settings/Roles`

### OpenAI (ChatGPT) - Optional
*Only required for the `/gpt` command*
1. Go to [OpenAI](https://platform.openai.com/) portal
2. Create an account (if you don't have one)
3. Click API Keys
4. Create a new secret key

### Twitch Notifications - Optional
*Only required for the `/twitch` command*
1. Go to [Twitch Developer](https://dev.twitch.tv/) portal
2. Create an account (if you don't have one)
3. Click `Create Application`
4. Fill out the required fields and click `Create`
5. Copy your client id and client secret

---

## Quick Start with Docker (Recommended)

The easiest way to run this bot is using Docker with pre-built images. All dependencies (yt-dlp, ffmpeg, cipher, poToken) are handled automatically.

### 1. Download the required files
Download `docker-compose.yml` and `.env.example` from this repository.

### 2. Create your environment file
```bash
cp .env.example .env
```

Edit `.env` with your values:
```
DISCORD_TOKEN=your_discord_bot_token_here
GUILD_ID=your_guild_id_here
```

### 3. Run the bot
```bash
docker compose up -d
```

This starts three services:
- **discordbot** - The bot itself (includes yt-dlp and ffmpeg)
- **yt-cipher** - YouTube signature decryption service
- **webpo-generator** - YouTube poToken generation for video playback

### Updating
If YouTube playback breaks, restart to pull the latest yt-dlp:
```bash
docker compose down
docker compose up -d
```

---

## Running from Source

### Prerequisites
- Java 25+
- Gradle
- [yt-dlp](https://github.com/yt-dlp/yt-dlp/releases) (required for live stream playback)
- [ffmpeg](https://ffmpeg.org/download.html) (required for live stream audio processing)

### Environment Variables

| Variable               | Required | Description                                      |
|------------------------|----------|--------------------------------------------------|
| `DISCORD_TOKEN`        | Yes      | Your Discord bot token                           |
| `GUILD_ID`             | Yes      | Your Discord server ID                           |
| `CIPHER_URL`           | No       | Cipher API URL (default: `http://yt-cipher:8001`). Falls back to public API if unreachable |
| `POT_URL`              | No       | poToken generator URL (default: `http://webpo-generator:8090`). Disabled if unreachable |
| `YTDLP_PATH`           | No       | Path to yt-dlp binary (default: `yt-dlp`)        |
| `GPT_SECRET`           | No       | OpenAI API key (for `/gpt` command)              |
| `GPT_PROMPT`           | No       | Custom personality prompt (for `/gpt` command)   |
| `GPT_MODEL`            | No       | OpenAI model name (for `/gpt` command)           |
| `TWITCH_CLIENT_ID`     | No       | Twitch client ID (for `/twitch` command)         |
| `TWITCH_CLIENT_SECRET` | No       | Twitch client secret (for `/twitch` command)     |

### Steps

1. Clone the repository:
```bash
git clone https://github.com/StoneTone/DiscordBotV3.git
cd DiscordBotV3
```

2. Set required environment variables:
```bash
export DISCORD_TOKEN=your_discord_bot_token
export GUILD_ID=your_server_id
```

3. Configure yt-dlp path in `application.yaml`:
```yaml
lofi:
  ytdlp-path: "/path/to/yt-dlp"
```
Or set the `YTDLP_PATH` environment variable.

4. Build and run:
```bash
./gradlew clean build -x test
./gradlew bootRun
```

### Running with Docker (from source)
```bash
./gradlew clean build -x test
docker compose up --build
```

#### Additional Notes

*To get your Guild ID, enable Developer Mode in Discord: User Settings > App Settings > Advanced > Developer Mode. Then right-click your server and click "Copy Server ID".*

*Make sure you move your bot to the highest role in the server: Server Settings > Roles, then click and drag.*

---

## Core Features

### Audio Commands
- **Play** - Play music from YouTube, SoundCloud, or any HTTP URL.
- **Lofi** - Plays lofi radio streams from the LofiGirl YouTube channel.
- **Pause / Unpause** - Pause and resume playback.
- **Stop** - Stop playback and clear the queue.
- **Skip** - Skip the current track.
- **Queue** - View upcoming tracks.
- **NowPlaying** - Display the currently playing track.
- **Leave** - Disconnect the bot from the voice channel.

### Role Request
- **Usage:** `/rolerequest <role_name>`
- Sends a direct message to the server owner for approval.

### Embed Builder
- **Usage:** `/embed`
- Create custom embed messages based on user input.

### ChatGPT Integration
- **Usage:** `/gpt <your_message>`
- Generates responses using OpenAI's API.

### CS2 Case Opening
- **Usage:** `/open <case>`
- Open virtual CS2 cases. Data sourced from [CS2 API](https://github.com/ByMykel/CSGO-API).

### Twitch Notifications
- **Usage:** `/twitch <streamer_name> <text_channel> <custom_message>`
- Get notified when a Twitch streamer goes live.
- Use `/twitchconfig` to manage notification settings.

---

## Architecture

```
                  ┌─────────────────────┐
                  │    Discord Bot       │
                  │  (Java/Spring Boot)  │
                  └──────────┬──────────┘
                             │
         ┌───────────┬───────┴───────┬───────────┐
         ▼           ▼               ▼           ▼
   ┌──────────┐ ┌──────────┐ ┌────────────┐ ┌────────┐
   │yt-cipher │ │webpo-gen │ │  yt-dlp +  │ │YouTube │
   │(signing) │ │(poToken) │ │  ffmpeg    │ │  API   │
   └──────────┘ └──────────┘ │(livestream)│ └────────┘
                             └────────────┘
```

### Audio Pipeline
- **Regular videos:** YouTube API (via lavaplayer youtube-source with poToken support)
- **Live streams:** yt-dlp + ffmpeg pipeline for smooth, continuous audio without segment gaps

---

## Contributions
Contributions are welcome! Feel free to fork this repository and submit pull requests for any improvements or additional features.

## Support
If you encounter any issues or have questions, please don't hesitate to contact me. You can reach out to me on [Discord](https://discord.com/users/480574457203916813).
