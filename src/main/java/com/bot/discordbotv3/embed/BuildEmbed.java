package com.bot.discordbotv3.embed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;


public class BuildEmbed {
    public static void handleEmbedBuilder(SlashCommandInteractionEvent event, String title,
                                          String description, String URL, String author,
                                          String thumbnail, String image)
    {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title);
        eb.setDescription(description);

        if(URL != null){
            eb.setUrl(event.getOption("url").getAsString());
        }
        if(author != null){
            eb.setAuthor(author);
        }
        if(thumbnail != null){
            eb.setThumbnail(thumbnail);
        }
        if(image != null){
            eb.setImage(image);
        }

        event.replyEmbeds(eb.build()).queue();
    }
}
