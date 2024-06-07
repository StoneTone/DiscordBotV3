package com.bot.discordbotv3.cmds;

import com.bot.discordbotv3.vo.RoleRequest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class RoleRequestCommand {
    private static final Logger logger = LoggerFactory.getLogger(RoleRequestCommand.class);
    public static Map<Long, RoleRequest> requests = new HashMap<>();
    public static void handleRoleRequestCommand(SlashCommandInteractionEvent event, Role requestedRole){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Role Request by " + event.getUser().getName());
        eb.setDescription("User " + event.getUser().getName() + " has requested the " + requestedRole.getName() + " role");
        RoleRequest roleRequest = new RoleRequest(event.getUser().getIdLong(), requestedRole.getIdLong());

        //async reply
        event.deferReply(true).queue(hook -> {
            TextChannel adminChannel = event.getGuild().getTextChannels().stream()
                    .filter(channel -> channel.getName().equals("admin-role-request"))
                    .findFirst()
                    .orElse(null);

            if(adminChannel.equals(null)){
                logger.error("Admin text channel either does not exist or there is a problem!");
                hook.editOriginal("There was an error processing your request").queue();
            }else{
                adminChannel.sendMessageEmbeds(eb.build()).setActionRow(
                        Button.success("approve", "Approve"),
                        Button.danger("decline", "Decline")
                ).queue(message -> {
                    requests.put(message.getIdLong(), roleRequest);
                });
                hook.editOriginal("Your role request has been submitted for review. Please wait for a response.").queue();
            }
        });

    }

    public static Map<Long, RoleRequest> getRoleRequests() {
        return requests;
    }
}


