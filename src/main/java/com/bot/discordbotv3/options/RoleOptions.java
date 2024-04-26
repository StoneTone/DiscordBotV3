package com.bot.discordbotv3.options;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class RoleOptions {
    public static OptionData handleRoleOptions(){
        return new OptionData(OptionType.STRING, "rolerequest", "Allows you to request a role", true)
                .addChoice("Test", "912638897316589579")
                .addChoice("Test", "1137982033671495761")
                .addChoice("Test", "1044861143514099712")
                .addChoice("Test", "695888137146335253")
                .addChoice("Test", "698031938790752286")
                .addChoice("Test", "572633970198446091")
                .addChoice("Test", "747662211744399442");
    }
}
