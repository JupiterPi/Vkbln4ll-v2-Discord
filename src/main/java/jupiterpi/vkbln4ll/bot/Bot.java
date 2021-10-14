package jupiterpi.vkbln4ll.bot;

import jupiterpi.vkbln4ll.ConfigFile;
import jupiterpi.vkbln4ll.Strings;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import jupiterpi.vkbln4ll.bot.services.HelpMessage;
import jupiterpi.vkbln4ll.bot.services.SearchChannelService;
import jupiterpi.vkbln4ll.bot.services.WritingChannelService;
import jupiterpi.vkbln4ll.repo.VocabularyPortion;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bot extends ListenerAdapter {
    private final String GUILD_ID = ConfigFile.getProperty("guild-id");
    private final String LOBBY_CHANNEL_ID = ConfigFile.getProperty("lobby-channel-id");
    private final String VERIFIED_ROLE_ID = ConfigFile.getProperty("verified-role-id");

    private final String WRITING_CHANNEL_ID = ConfigFile.getProperty("writing-channel-id");
    private final String WRITER_ROLE_ID = ConfigFile.getProperty("writing-role-id");

    private final String SEARCH_CHANNEL_ID = ConfigFile.getProperty("search-channel-id");

    private JDA jda;
    private Guild guild;
    private TextChannel lobbyChannel;
    private Role verifiedRole;
    private Emote readEmote;

    private TextChannel writingChannel;
    private Role writerRole;

    private TextChannel searchChannel;

    private Guild studentsGuild;

    public Bot(String token) throws LoginException, InterruptedException {
        jda = JDABuilder.createDefault(token)
                .setActivity(Activity.listening("!help"))
                .addEventListeners(this)
                .build();
        jda.awaitReady();
        guild = jda.getGuildById(GUILD_ID);
        lobbyChannel = guild.getTextChannelById(LOBBY_CHANNEL_ID);
        verifiedRole = guild.getRoleById(VERIFIED_ROLE_ID);
        readEmote = guild.getEmotesByName(ConfigFile.getProperty("read-emote-name"), true).get(0);

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
                        lobbyChannel.sendMessage(Strings.getString("portion-not-found")).queue(msg -> {
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
                    lobbyChannel.sendMessage(Strings.getString("not-verified")).queue(msg -> {
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
                deleteAllMessages(writingChannel);
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
                deleteAllMessages(searchChannel);
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

    private void deleteAllMessages(TextChannel channel) {
        channel.getHistoryFromBeginning(100).queue(messageHistory -> {
            List<Message> history = messageHistory.getRetrievedHistory();
            if (!history.isEmpty()) {
                channel.deleteMessages(history).queue((v) -> {
                    deleteAllMessages(channel);
                });
            }
        });
    }
}