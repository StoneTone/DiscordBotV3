# Java Discord Bot with JDA and Lavaplayer

## Introduction
Welcome to my Java Discord bot application! This bot is built using JDA (Java Discord API) and Lavaplayer, allowing seamless integration of audio playback in Discord servers. With a focus on simplicity and functionality, my bot provides essential features for managing roles, playing music, and even integrating ChatGPT for engaging conversations.

## Setup
### Discord Portal
1. Go to the [Discord Developer](https://discord.com/developers)  portal
2. Click `New Application`
3. Customized however you'd like
4. Click `Bot` Tab then `Reset Secret`
5. Copy your token (Keep this secure and do not share with anyone!)
6. Scroll down to `Privileged Gateway Intents` and enable all intents (This is required for the bot to function properly)
7. Click `OAuth2` Tab
8. In the OAuth2 URL Generator, make sure to select (bot, application.commands)
9. Select administrator permissions
10. Copy the generated url and paste into your browser and invite it to your server 
11. Move your bot role to the highest priority in your server `Server Settings/Roles`

### OpenAi (ChatGPT) #Not Required ONLY for /gpt command
1. Go to [OpenAI](https://platform.openai.com/) portal
2. Create an account (if you don't have one)
3. Click API Keys
4. Create a new secret key

### Twitch Notifications #Not Required ONLY for /twitch command
1. Go to [Twitch Developer](https://dev.twitch.tv/) portal
2. Create an account (if you don't have one)
3. Click `Create Application`
4. Fill out the required fields and click `Create`
5. Copy your client id and client secret

### Run Application Locally
To run this application locally, follow these steps:

1. Clone the repository to your local machine.
2. Add the environment variables when running the application
3. Compile and run the application using Maven.

```
DISCORD_TOKEN=your_discord_bot_token
GUILD_ID=your_server_id
YOUTUBE_SECRET=your_youtube_api_key
OWNER_ID=your_discord_id
GPT_SECRET=your_gpt_secret_key
TWITCH_CLIENT_ID=your_twitch_client_id
TWITCH_CLIENT_SECRET=your_twitch_client_secret
```

#### Additional Notes

*-to get your guild id you need to enable developer mode in discord-*

*-Make sure you move your bot to the highest role in the server. `Server Settings/Roles` then click and drag-*

Navigate to `src/main/java/com/bot/discordbotv3/gpt/ChatRequest` and change the system message to configure your own personality to your bot.
Otherwise, you can leave this blank. 

## Core Features

### Role Request
- **Description:** Users can request a role in the server.
- **Usage:** Use the `/rolerequest <role_name>` command to request a specific role.
- **Functionality:** Sends a direct message to the server owner for approval.

### Audio Commands
- **Play:** Play music from YouTube.
- **Lofi:** Plays lofi from YouTube.
  - **Note:** This uses a webscraper to get all streams from a youtube channel
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
- **Description:** Opening Virtual CS2 cases for FREE!
- **Usage:** Utilize the `/open <case>` command to open cases.
- **Functionality:** Utilizes CS2 API for all data. Check it out here: [CS2 API](https://github.com/ByMykel/CSGO-API)

### Twitch Notifications
- **Description:** Get notified when a Twitch streamer goes live.
- **Usage:** Use the `/twitch <streamer_name> <text_channel> <custom_message>` command to receive notifications.
- **Functionality:** Sends a message to the specified text channel when the streamer goes live.
- **Note:** You can also use `/twitchconfig` to remove the streamer from the notification list or edit the custom message.

## Dependencies
- Java 17
- Maven

## Getting Started
1. Ensure you have Java 17 installed on your system.
2. Install Maven for dependency management.
3. Set up your environment variables in GitHub for deployment
4. Change the deploy stage in the cicd.yaml file for your deployment
5. Compile and run the application using Maven.

## Contributions
Contributions are welcome! Feel free to fork this repository and submit pull requests for any improvements or additional features.

## Support
If you encounter any issues or have questions, please don't hesitate to contact me. You can reach out to me on [Discord](https://discord.com/users/480574457203916813).

Thank you for using my Java Discord bot! We hope you enjoy its features and find it useful for your server.
