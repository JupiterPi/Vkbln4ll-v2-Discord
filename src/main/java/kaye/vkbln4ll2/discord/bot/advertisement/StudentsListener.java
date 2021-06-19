package kaye.vkbln4ll2.discord.bot.advertisement;

import kaye.vkbln4ll2.discord.bot.services.HelpMessage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class StudentsListener extends ListenerAdapter {
    private static final String STUDENTS_GUILD_ID = "688367609913147403";
    private Guild studentsGuild;

    public StudentsListener(JDA jda) {
        studentsGuild = jda.getGuildById(STUDENTS_GUILD_ID);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getGuild() == studentsGuild) {
            if (event.getMessage().getContentRaw().equals("!help")) {
                event.getChannel().sendMessage(HelpMessage.buildStudentsHelpMessage()).queue();
            }
        }
    }

    @Override
    public void onPrivateMessageReceived(@NotNull PrivateMessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            System.out.println("private message from " + event.getAuthor().getName() + ": " + event.getMessage().getContentRaw());
            event.getChannel().sendMessage(HelpMessage.buildStudentsHelpMessage()).queue();
        }
    }
}