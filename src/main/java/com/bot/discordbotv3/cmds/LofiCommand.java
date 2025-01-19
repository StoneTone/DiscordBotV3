package com.bot.discordbotv3.cmds;

import com.bot.discordbotv3.lavaplayer.GuildMusicManager;
import com.bot.discordbotv3.lavaplayer.PlayerManager;
import com.bot.discordbotv3.lavaplayer.TrackScheduler;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LofiCommand {
    private static final Logger logger = LoggerFactory.getLogger(LofiCommand.class);
    public static void handleLofiCommand(SlashCommandInteractionEvent event, String option){
        Member member = event.getMember();
        GuildVoiceState memberVoiceState = member.getVoiceState();

        if (!memberVoiceState.inAudioChannel()) {
            event.reply("You need to be in a voice channel!").setEphemeral(true).queue();
            return;
        }

        Member self = event.getGuild().getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        if (!selfVoiceState.inAudioChannel()) {
            //Join VC
            event.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
        } else {
            if (selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
                event.reply("You need to be in the same channel as me").setEphemeral(true).queue();
                return;
            }
        }
        //clear the queue and change the song
        GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
        TrackScheduler trackScheduler = guildMusicManager.getTrackScheduler();
        trackScheduler.getQueue().clear();
        trackScheduler.getPlayer().stopTrack();

        event.deferReply().queue(hook -> {
            PlayerManager playerManager = PlayerManager.get();
            playerManager.play(event.getGuild(), option, hook);
        });

    }
}
