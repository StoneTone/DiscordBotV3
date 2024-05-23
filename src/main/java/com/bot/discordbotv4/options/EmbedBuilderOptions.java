package com.bot.discordbotv4.options;


import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class EmbedBuilderOptions {
    public static final OptionData TITLE = new OptionData(OptionType.STRING, "title", "Set the title", true);
    public static final OptionData DESCRIPTION = new OptionData(OptionType.STRING, "description", "Set the description", true);
    public static final OptionData URL = new OptionData(OptionType.STRING, "url", "Set the url", false);
    public static final OptionData AUTHOR = new OptionData(OptionType.STRING, "author", "Set an author", false);
    public static final OptionData THUMBNAIL = new OptionData(OptionType.STRING, "thumbnail", "Set a thumbnail", false);
    public static final OptionData IMAGE = new OptionData(OptionType.STRING, "image", "Set an image", false);
}
