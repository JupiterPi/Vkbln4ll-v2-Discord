package jupiterpi.vkbln4ll.bot.services;

import jupiterpi.tools.files.Path;
import jupiterpi.tools.files.TextFile;
import jupiterpi.tools.util.ToolsUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HelpMessage {
    private static final Path HELP_TEXT_PATH = Path.getRunningDirectory().subdir("data").file("help.txt");

    public static MessageEmbed build() {
        EmbedBuilder helpMessage = new EmbedBuilder()
                .setColor(Color.BLACK)
                .setTitle("Hilfe: Vkbln4ll Discord")
                .setFooter("Klicke auf den Haken, wenn du fertig bist!");

        String fieldHeader = null;
        List<String> fieldLines = new ArrayList<>();
        for (String line : new TextFile(HELP_TEXT_PATH).getFile()) {
            line = line
                    .replaceAll("#a", "ä")
                    .replaceAll("#o", "ö")
                    .replaceAll("#u", "ü")
                    .replaceAll("#s", "ß");
            //if (!line.equals("")) {
                if (line.startsWith("# ")) {
                    if (fieldHeader != null) {
                        if (fieldHeader.equals("-")) fieldHeader = "";
                        helpMessage.addField(fieldHeader, ToolsUtil.appendWithSeparator(fieldLines, "\n"), false);
                        fieldHeader = null;
                        fieldLines = new ArrayList<>();
                    }
                    fieldHeader = line.substring("# ".length());
                } else {
                    if (line.equals("-")) line = "";
                    fieldLines.add(line);
                }
            //}
        }
        if (fieldHeader != null && fieldHeader.equals("-")) fieldHeader = "";
        helpMessage.addField(fieldHeader, ToolsUtil.appendWithSeparator(fieldLines, "\n"), false);

        return helpMessage.build();
    }

    public static Message buildStudentsHelpMessage() {
        MessageEmbed embed =  new EmbedBuilder()
                .setTitle("Vkbln4ll Discord")
                .setColor(Color.BLACK)
                .addField("", "Viele erinnern sich bestimmt an meinen Vokabeltrainer, Vkbln4ll. Nun, hier ist er. Generalüberholt. Neue Features. DIREKT IN DISCORD! Aber das Beste ist: Diesmal funktioniert er auch!", false)
                .build();
        return new MessageBuilder()
                .setContent("Viele erinnern sich bestimmt an meinen Vokabeltrainer, Vkbln4ll. Nun, hier ist er. Generalüberholt. Neue Features. DIREKT IN DISCORD! Aber das Beste ist: Diesmal funktioniert er auch!\nhttps://discord.gg/spbDMudXdh")
                .build();
    }
}