package kaye.vkbln4ll2.discord.bot;

import kaye.vkbln4ll2.discord.bot.advertisement.StudentsListener;
import kaye.vkbln4ll2.discord.bot.services.HelpMessage;
import kaye.vkbln4ll2.discord.bot.services.SearchChannelService;
import kaye.vkbln4ll2.discord.bot.services.WritingChannelService;
import kaye.vkbln4ll2.discord.repo.VocabularyPortion;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bot extends ListenerAdapter {
    private final String TOKEN = "ODA3MjI1ODQ4NzA0NDAxNDQ4.YB05pw.uFeZdtBWxCuuyviLhZKXPdmKVnA";
    private final String GUILD_ID = "807229629940629516";
    private final String LOBBY_CHANNEL_ID = "807229728095862794";
    private final String VERIFIED_ROLE_ID = "808662111482937394";

    private final String WRITING_CHANNEL_ID = "808696327307132978";
    private final String WRITER_ROLE_ID = "808680338447532053";

    private final String SEARCH_CHANNEL_ID = "809026608949231617";

    private final String STUDENTS_GUILD_ID = "688367609913147403";

    private JDA jda;
    private Guild guild;
    private TextChannel lobbyChannel;
    private Role verifiedRole;
    private Emote readEmote;

    private TextChannel writingChannel;
    private Role writerRole;

    private TextChannel searchChannel;

    private Guild studentsGuild;

    public Bot() throws LoginException, InterruptedException {
        jda = JDABuilder.createDefault(TOKEN)
                .setActivity(Activity.listening("!help"))
                .addEventListeners(this)
                .build();
        jda.awaitReady();
        jda.addEventListener(new StudentsListener(jda));
        guild = jda.getGuildById(GUILD_ID);
        lobbyChannel = guild.getTextChannelById(LOBBY_CHANNEL_ID);
        verifiedRole = guild.getRoleById(VERIFIED_ROLE_ID);
        readEmote = guild.getEmotesByName("bot_read", true).get(0);

        writingChannel = guild.getTextChannelById(WRITING_CHANNEL_ID);
        writerRole = guild.getRoleById(WRITER_ROLE_ID);

        searchChannel = guild.getTextChannelById(SEARCH_CHANNEL_ID);
    }

    private List<Session> sessions = new ArrayList<>();
    private WritingChannelService writingChannelService = new WritingChannelService();
    private SearchChannelService searchChannelService = new SearchChannelService();

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getGuild() != guild) return;
        if (event.getUser().isBot()) return;
        event.getChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
            if (message.getContentRaw().equals("")) {
                message.delete().queue();
            }
        });
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getGuild() != guild) return;
        if (event.getAuthor().isBot()) return;
        String message = event.getMessage().getContentRaw();
        String[] parts = message.split(" ");

        if (event.getChannel() == lobbyChannel) {
            if (message.equals("!help")) {
                event.getMessage().delete().queue();
                event.getChannel().sendMessage(HelpMessage.build()).queue(msg -> {
                    msg.addReaction(readEmote).queue();
                });
            }

            if (parts[0].equals("!start")) {
                if (parts.length == 1) parts = new String[]{"!start", ""};
                if (event.getMember().getRoles().contains(verifiedRole)) {
                    VocabularyPortion vocabularyPortion = null;
                    for (VocabularyPortion portion : VocabularyPortion.getPortions()) {
                        if (portion.getId().equals(parts[1])) vocabularyPortion = portion;
                    }
                    if (vocabularyPortion != null) {
                        sessions.add(new Session(vocabularyPortion, guild, event.getMember(), sessions::remove));
                        event.getMessage().delete().queue();
                    } else {
                        event.getMessage().delete().queue();
                        lobbyChannel.sendMessage("Vokabelportion nicht gefunden. Versuche es erneut oder wende dich an einen @supporter").queue(msg -> {
                            new Thread(() -> {
                                try {
                                    Thread.sleep(3 * 1000);
                                    msg.delete().queue();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        });
                    }
                } else {
                    event.getMessage().delete().queue();
                    lobbyChannel.sendMessage("Du musst das Produkt kaufen und dich in `#support` verifizieren lassen, um es zu nutzen.").queue(msg -> {
                        new Thread(() -> {
                            try {
                                Thread.sleep(5 * 1000);
                                msg.delete().queue();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    });
                }
            }
        } else if (event.getChannel() == writingChannel) {
            if (message.equals("bye")) {
                writingChannel.getHistoryFromBeginning(100).queue(messageHistory -> {
                    writingChannel.deleteMessages(messageHistory.getRetrievedHistory()).queue();
                });
                Member member = event.getMember();
                guild.removeRoleFromMember(member, writerRole).queue(x -> {
                    guild.addRoleToMember(member, writerRole).queue();
                });
            } else {
                String feedback = writingChannelService.onInput(message);
                if (feedback != null) {
                    writingChannel.sendMessage(feedback).queue();
                }
            }
        } else if (event.getChannel() == searchChannel) {
            if (message.equals("clear")) {
                searchChannel.getHistoryFromBeginning(100).queue(messageHistory -> {
                    searchChannel.deleteMessages(messageHistory.getRetrievedHistory()).queue();
                });
            } else {
                Message feedback = searchChannelService.search(message.split(" "));
                if (feedback != null) {
                    searchChannel.sendMessage(feedback).queue();
                }
            }
        } else {
            for (Session session : sessions) {
                if (session.getChannel() == event.getChannel()) {
                    String cmd = parts[0];
                    String[] args = Arrays.asList(parts).subList(1, parts.length).toArray(new String[0]);
                    String feedback = session.onInput(message);
                    if (feedback != null) {
                        event.getChannel().sendMessage(feedback).queue();
                    }
                }
            }
        }
    }
}