# Java Discord Bot with JDA and Lavaplayer

## Introduction
Welcome to my Java Discord bot application! This bot is built using JDA (Java Discord API) and Lavaplayer, allowing seamless integration of audio playback in Discord servers. With a focus on simplicity and functionality, my bot provides essential features for managing roles, playing music, and even integrating ChatGPT for engaging conversations.

## Setup
### Discord Portal
1. Go to the [Discord Developer](https://discord.com/developers)  portal
2. Click `New Application`
3. Click `OAuth2` Tab
4. Click `Reset Secret` and copy the key and paste in your .env file
5. In the OAuth2 URL Generator, make sure to select (bot, application.commands)
6. Select administrator permissions
7. Copy the generated url and paste into your browser and invite it to your server

### OpenAi (ChatGPT)
1. Go to [OpenAI](https://platform.openai.com/) portal
2. Create an account (if you don't have one)
3. Click API Keys
4. Create a new secret key

### Google (YouTube Search)
1. Go to the [Google Cloud](https://console.cloud.google.com) console
2. Create an account (if you don't have one)
3. Create a project
4. Go to `API & Services`
5. Click `Library`
6. Find and enable ***YouTube Data API v3***
7. Click `Create Credentials` and follow the prompts

### Run Application Locally
To run this application locally, follow these steps:

1. Clone the repository to your local machine.
2. Create a `.env` file in the root directory of the project.
3. Add the following properties to the `.env` file:
```
DISCORD_TOKEN=your_discord_bot_token
GUILD_ID=your_server_id
YOUTUBE_SECRET=your_youtube_api_key
OWNER_ID=your_discord_id
GPT_SECRET=your_gpt_secret_key
```
*-to get your guild id and owner id you need to enable developer mode in discord-*

## Core Features

### Role Request
- **Description:** Users can request a role in the server.
- **Usage:** Use the `/rolerequest <role_name>` command to request a specific role.
- **Functionality:** Sends a direct message to the server owner for approval.

### Audio Commands
- **Play:** Play music from YouTube.
- **Pause:** Pause the currently playing track.
- **Unpause:** Resume playback after pausing.
- **Stop:** Stop the playback entirely.
- **Skip:** Skip the current track in the queue.
- **Queue:** View the list of upcoming tracks.
- **NowPlaying:** Display the currently playing track.
- **Leave:** Disconnect the bot from the voice channel.

### ChatGPT Integration
- **Description:** Interact with ChatGPT directly within the Discord server.
- **Usage:** Utilize the `/gpt <your_message>` command to engage in conversations.
- **Functionality:** Generates responses based on the provided input using ChatGPT.

## Dependencies
- Java 17
- Maven

## Getting Started
1. Ensure you have Java 17 installed on your system.
2. Install Maven for dependency management.
3. Set up the `.env` file with the required properties.
4. Compile and run the application using Maven.

## Contributions
Contributions are welcome! Feel free to fork this repository and submit pull requests for any improvements or additional features.

## Support
If you encounter any issues or have questions, please don't hesitate to contact me. You can reach out to me on [Discord](https://discord.com/users/480574457203916813).

Thank you for using my Java Discord bot! We hope you enjoy its features and find it useful for your server.
