package com.bot.discordbotv3.embed;

import com.bot.discordbotv3.cmds.RoleRequestCommand;
import com.bot.discordbotv3.vo.RoleRequest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.Map;

public class RoleRequestEmbed {
    public static void handleRoleRequestEmbed(ButtonInteractionEvent event, long guildID){
        EmbedBuilder eb = new EmbedBuilder();
        Map<Long, RoleRequest> requests = RoleRequestCommand.getRoleRequests();
        RoleRequest request = requests.get(event.getMessage().getIdLong());
        Role requestedRole = event.getJDA().getRoleById(request.getRoleId());
        User requestedUser = event.getJDA().getUserById(request.getUserId());
        switch(event.getComponentId()){
            case "approve":
                //Delete message once interacted
                event.getMessage().delete().queue();
                // Approve button clicked
                eb.setTitle("Role Approved!");
                eb.setDescription("Your request for " + "**" +
                        requestedRole.getName() + "**" + " in " +  "*" + event.getJDA().getGuildById(guildID).getName() + "*"+ " has been approved!");
                requestedUser.openPrivateChannel().queue(privateChannel ->
                        privateChannel.sendMessageEmbeds(eb.build()).queue());
                // Add the role to the user
                event.getJDA().getGuildById(guildID).addRoleToMember(requestedUser, requestedRole).queue();
                event.reply("Role approved!").setEphemeral(true).queue();
                break;
            case "decline":
                //Delete message once interacted
                event.getMessage().delete().queue();
                // Decline button clicked
                eb.setTitle("Role Declined!");
                eb.setDescription("Your request for " + "**" +
                        requestedRole.getName() + "**" + " in " +  "*" + event.getJDA().getGuildById(guildID).getName() + "*"+ " has been declined!");
                requestedUser.openPrivateChannel().queue(privateChannel ->
                        privateChannel.sendMessageEmbeds(eb.build()).queue());
                event.reply("Role Declined!").setEphemeral(true).queue();
                break;
        }
    }
}
