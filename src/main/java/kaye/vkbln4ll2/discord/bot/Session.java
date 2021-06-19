package kaye.vkbln4ll2.discord.bot;

import kaye.vkbln4ll2.discord.repo.Vocabulary;
import kaye.vkbln4ll2.discord.repo.VocabularyPortion;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.*;
import java.util.function.Consumer;

public class Session {
    private final String IN_SESSION_ROLE_ID = "807945629967712256";
    private final String SESSIONS_CATEGORY_ID = "807946352884711444";

    private Guild guild;
    private Role inSessionRole;
    private Category sessionsCategory;

    private Member member;
    private TextChannel channel;

    private Consumer<Session> removeSession;

    private VocabularyPortion vocabularyPortion;

    public Session(VocabularyPortion vocabularyPortion, Guild guild, Member member, Consumer<Session> removeSession) {
        this.vocabularyPortion = vocabularyPortion;
        this.guild = guild;
        this.inSessionRole = guild.getRoleById(IN_SESSION_ROLE_ID);
        this.sessionsCategory = guild.getCategoryById(SESSIONS_CATEGORY_ID);

        this.member = member;
        guild.addRoleToMember(member, inSessionRole).queue();
        sessionsCategory.createTextChannel("abfrage-" + member.getEffectiveName())
                .addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null)
                .queue(channel -> {
                    this.channel = channel;
                    start();
                });

        this.removeSession = removeSession;
    }

    public TextChannel getChannel() {
        return channel;
    }

    /* logic */

    private Phase phase;
    private enum Phase {
        WAIT_DIRECTION, RUNNING, RESULT
    }

    public String onInput(String str) {
        if (str.equals("stop")) {
            stop();
        }

        // direction & start
        else if (phase == Phase.WAIT_DIRECTION) {
            try {
                direction = Direction.valueOf(str.toUpperCase());
                phase = Phase.RUNNING;
                askNext();
            } catch (IllegalArgumentException e) {
                return "Deutsch-Latein (dl), Latein-Deutsch (ld) oder je zufällig (zu)";
            }
        }

        // while running
         else if (phase == Phase.RUNNING) {
            sendFeedback(str);
            askNext();
        }

        // after result
        else if (phase == Phase.RESULT) {
            if (str.equals("Y")) start();
            else if (str.equals("n")) stop();
        }

        return null;
    }

    private void stop() {
        new Thread(() -> {
            try {
                Thread.sleep(2*1000);
                channel.delete().queue(r1 -> {
                    guild.removeRoleFromMember(member, inSessionRole).queue(r2 -> {
                        this.removeSession.accept(this);
                    });
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        channel.sendMessage("Wird gestoppt...").queue();
    }

    private Direction direction;
    private Direction currentDirection;
    private enum Direction {
        LD, DL, ZU
    }

    private List<Vocabulary> vocabularies = null;
    private Vocabulary currentVocabulary;
    //private int amountRight;
    private List<Vocabulary> wrongVocabularies = new ArrayList<>();

    private void start() {
        if (wrongVocabularies.size() == 0) {
            /* if (vocabularies == null) */ vocabularies = vocabularyPortion.getVocabularies();
        } else {
            vocabularies = new ArrayList<>(wrongVocabularies);
        }
        Collections.shuffle(vocabularies);
        wrongVocabularies = new ArrayList<>();
        currentVocabulary = null;

        phase = Phase.WAIT_DIRECTION;

        channel.sendMessage("Wie sollen die Vokabeln abgefragt werden? Deutsch-Latein (dl), Latein-Deutsch (ld) oder je zufällig (zu)").queue();
    }

    private void askNext() {
        if (currentVocabulary == null) {
            currentVocabulary = vocabularies.get(0);
        } else {
            int index = vocabularies.indexOf(currentVocabulary);
            if (index == vocabularies.size()-1) {
                sendFinalFeedback();
                return;
            } else currentVocabulary = vocabularies.get(index+1);
        }

        currentDirection = Direction.DL;
        if (direction == Direction.LD || (direction == Direction.ZU && new Random().nextBoolean())) currentDirection = Direction.LD;

        String askStr = currentVocabulary.getGermanStr();
        if (currentDirection == Direction.LD) askStr = currentVocabulary.getLatin();
        channel.sendMessage(askStr).queue();
    }

    private void sendFeedback(String response) {
        Feedback feedback = Feedback.WRONG;
        boolean forceRepeat = false;
        if (currentDirection == Direction.DL) {
            if (currentVocabulary.getLatin().equals(response)) feedback = Feedback.RIGHT;
        } else {
            List<String> germans = Arrays.asList(response.split(", "));
            List<String> solutionGermans = currentVocabulary.getGerman();
            int foundAmount = 0;
            for (String german : solutionGermans) {
                if (germans.contains(german)) foundAmount++;
            }
            float rightPercent = ((float)foundAmount) / ((float)solutionGermans.size());
            if (rightPercent > 0.6f) feedback = Feedback.RIGHT;
            else if (rightPercent >= 0.2f) feedback = Feedback.IMPERFECT;
            if (rightPercent < 0.9) forceRepeat = true;
        }

        if (feedback != Feedback.RIGHT) wrongVocabularies.add(currentVocabulary);

        String text = "";
        switch (feedback) {
            case RIGHT: text = "RICHTIG\n"; break;
            case IMPERFECT: text = "MANGELHAFT\n"; break;
            case WRONG: text = "FALSCH\n"; break;
        }

        if (feedback != Feedback.RIGHT || forceRepeat) text += currentVocabulary.toString();

        channel.sendMessage(text).queue();
    }
    private enum Feedback {
        RIGHT, IMPERFECT, WRONG
    }

    private void sendFinalFeedback() {
        String text = "";

        int amountWrong = wrongVocabularies.size();
        int amountRight = vocabularies.size() - amountWrong;
        float rightPercent = ((float) amountRight) / ((float)vocabularies.size());
        int rightPercentNumber = (int) (rightPercent*100);
        text += "Du hast " + rightPercentNumber + "% richtig. \n";

        if (wrongVocabularies.size() > 0) text += "Du hast " + amountWrong + " Vokabeln falsch. ";
        text += "Wiederholen? -> Y/n";

        channel.sendMessage(text).queue();

        phase = Phase.RESULT;
    }
}