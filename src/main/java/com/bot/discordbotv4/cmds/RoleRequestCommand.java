package com.bot.discordbotv4.cmds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class RoleRequestCommand {
    public static void handleRoleRequestCommand(SlashCommandInteractionEvent event, Role requestedRole, User requestedUser, long ownerId){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Role Request by " + event.getUser().getName());
        eb.setDescription("User " + event.getUser().getName() + " has requested the " + requestedRole.getName() + " role");
        requestedUser = event.getUser();
        User botOwner = event.getJDA().getUserById(ownerId);
        botOwner.openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessageEmbeds(eb.build())
                    .setActionRow(
                            Button.success("approve", "Approve"),
                            Button.danger("decline", "Decline")
                    ).queue();
        });
        event.reply("Your role request has been submitted for review. Please wait for a response.").setEphemeral(true).queue();
    }
}
