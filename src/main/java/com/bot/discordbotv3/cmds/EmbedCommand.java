package com.bot.discordbotv3.cmds;

import com.bot.discordbotv3.embed.BuildEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Objects;

public class EmbedCommand {
    public static void handleEmbedCommand(SlashCommandInteractionEvent event){
        String title = Objects.requireNonNull(event.getOption("title")).getAsString();
        String description = Objects.requireNonNull(event.getOption("description")).getAsString();
        OptionMapping urlOption = event.getOption("url");
        String url = urlOption != null ? urlOption.getAsString() : null;

        OptionMapping authorOption = event.getOption("author");
        String author = authorOption != null ? authorOption.getAsString() : null;

        OptionMapping thumbnailOption = event.getOption("thumbnail");
        String thumbnail = thumbnailOption != null ? thumbnailOption.getAsString() : null;

        OptionMapping imageOption = event.getOption("image");
        String image = imageOption != null ? imageOption.getAsString() : null;

        BuildEmbed.handleEmbedBuilder(event, title, description, url, author, thumbnail, image);
    }
}
