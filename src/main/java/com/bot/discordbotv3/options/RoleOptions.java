package com.bot.discordbotv3.options;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class RoleOptions {
    public static OptionData handleRoleOptions(){
        return new OptionData(OptionType.STRING, "rolerequest", "Allows you to request a role", true)
                .addChoice("Counter-Strike", "912638897316589579")
                .addChoice("ShreeActiveRoster", "1137982033671495761")
                .addChoice("Shreeveloper", "1044861143514099712")
                .addChoice("Amigos", "695888137146335253")
                .addChoice("Geek Squad", "698031938790752286")
                .addChoice("Branded Payment", "572633970198446091")
                .addChoice("Peasant", "747662211744399442");
    }
}
