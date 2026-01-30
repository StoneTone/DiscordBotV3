# Java Discord Bot with JDA and Lavaplayer

## Introduction
Welcome to my Java Discord bot application! This bot is built using JDA (Java Discord API) and Lavaplayer, allowing seamless integration of audio playback in Discord servers. With a focus on simplicity and functionality, my bot provides essential features for managing roles, playing music, and even integrating ChatGPT for engaging conversations.

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

The easiest way to run this bot is using Docker with pre-built images.

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
GPT_SECRET=your_gpt_secret_here
GPT_PROMPT=your_custom_personality_prompt
GPT_MODEL=your_gpt_model
TWITCH_CLIENT_ID=your_client_id_here
TWITCH_CLIENT_SECRET=your_secret_here
```

### 3. Run the bot
```bash
docker-compose up -d
```

### Updating yt-dlp
If YouTube playback breaks due to yt-dlp being outdated:
```bash
docker-compose down
docker-compose up -d
```

---

## Running from Source

### Prerequisites
- Java 17
- Gradle

### Environment Variables

| Variable               | Required | Description                                    |
|------------------------|----------|------------------------------------------------|
| `DISCORD_TOKEN`        | Yes | Your Discord bot token                         |
| `GUILD_ID`             | Yes | Your Discord server ID                         |
| `GPT_SECRET`           | No | OpenAI API key (for `/gpt` command)            |
| `GPT_PROMPT`           | No | Custom Personality Prompt (for `/gpt` command) |
| `GPT_MODEL`            | No | Specific OpenAi Model (for `/gpt` command)     |
| `TWITCH_CLIENT_ID`     | No | Twitch client ID (for `/twitch` command)       |
| `TWITCH_CLIENT_SECRET` | No | Twitch client secret (for `/twitch` command)   |

### Steps

1. Clone the repository:
```bash
git clone https://github.com/StoneTone/DiscordBotV3.git
cd DiscordBotV3
```

2. Set environment variables:
```bash
export DISCORD_TOKEN=your_discord_bot_token
export GUILD_ID=your_server_id
export GPT_SECRET=your_openai_api_key
export GPT_PROMPT=your_custom_personality_prompt
export GPT_MODEL=your_gpt_model
export TWITCH_CLIENT_ID=your_client_id_here
export TWITCH_CLIENT_SECRET=your_secret_here
```

3. Build and run with Gradle:
```bash
./gradlew clean build -x test
./gradlew bootRun
```
3. Install YT-DLP and point to the EXE:
```yml
#Modify App.yml
lofi:
  channel-url: "https://www.youtube.com/@LofiGirl/streams"
  ytdlp-path: "C:/PATH/TO/YT-DLP" #change this to the path of yt-dlp for local testing
```

### Running with Docker (from source)
```bash
./gradlew clean build -x test
docker-compose up --build
```

#### Additional Notes

*To get your Guild ID, enable Developer Mode in Discord: User Settings > App Settings > Advanced > Developer Mode. Then right-click your server and click "Copy Server ID".*

*Make sure you move your bot to the highest role in the server: Server Settings > Roles, then click and drag.*

---

## Core Features

### Role Request
- **Description:** Users can request a role in the server.
- **Usage:** Use the `/rolerequest <role_name>` command to request a specific role.
- **Functionality:** Sends a direct message to the server owner for approval.

### Audio Commands
- **Play:** Play music from YouTube or any HTTP URL.
- **Lofi:** Plays lofi radio streams from the LofiGirl YouTube channel.
  - **Note:** Uses a yt-dlp service to fetch live streams.
- **Pause:** Pause the currently playing track.
- **Unpause:** Resume playback after pausing.
- **Stop:** Stop the playback entirely.
- **Skip:** Skip the current track in the queue.
- **Queue:** View the list of upcoming tracks.
- **NowPlaying:** Display the currently playing track.
- **Leave:** Disconnect the bot from the voice channel.

### Embed Builder
- **Description:** Create custom embed messages.
- **Usage:** Use the `/embed` command to create an embed message.
- **Functionality:** Generates a custom embed message based on user input.

### ChatGPT Integration
- **Description:** Interact with ChatGPT directly within the Discord server.
- **Usage:** Utilize the `/gpt <your_message>` command to engage in conversations.
- **Functionality:** Generates responses based on the provided input using ChatGPT.

### CS2 Case Opening
- **Description:** Opening virtual CS2 cases for FREE!
- **Usage:** Utilize the `/open <case>` command to open cases.
- **Functionality:** Utilizes CS2 JSON for all data. Check it out here: [CS2 API](https://github.com/ByMykel/CSGO-API)

### Twitch Notifications
- **Description:** Get notified when a Twitch streamer goes live.
- **Usage:** Use the `/twitch <streamer_name> <text_channel> <custom_message>` command to receive notifications.
- **Functionality:** Sends a message to the specified text channel when the streamer goes live.
- **Note:** You can also use `/twitchconfig` to remove the streamer from the notification list or edit the custom message.

---

## Architecture

```
┌─────────────────┐
│   discordbot    │ (Java/Spring Boot)
└────────┬────────┘
         │
    ┌────┴────┬─────────────┐
    ▼         ▼             ▼
┌────────┐ ┌────────┐ ┌──────────┐
│yt-dlp  │ │yt-cipher│ │ YouTube  │
│service │ │        │ │ Lavalink │
└────────┘ └────────┘ └──────────┘
```

---

## Contributions
Contributions are welcome! Feel free to fork this repository and submit pull requests for any improvements or additional features.

## Support
If you encounter any issues or have questions, please don't hesitate to contact me. You can reach out to me on [Discord](https://discord.com/users/480574457203916813).

Thank you for using my Java Discord bot! I hope you enjoy its features and find it useful for your server.
