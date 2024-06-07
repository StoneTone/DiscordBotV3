package com.bot.discordbotv3.options;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class RoleOptions {
    public static OptionData handleRoleOptions(Guild guild){
        OptionData data = new OptionData(OptionType.STRING, "rolerequest", "Allows you to request a role", true);
        for(Role role: guild.getRoles()){
            if(role.isPublicRole() || role.hasPermission(Permission.ADMINISTRATOR)){
                continue;
            }
            data.addChoice(role.getName(), role.getId());
        }
        return data;
    }
}
