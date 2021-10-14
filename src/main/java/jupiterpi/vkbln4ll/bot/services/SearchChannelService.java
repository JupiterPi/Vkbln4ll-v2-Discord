package jupiterpi.vkbln4ll.bot.services;

import jupiterpi.tools.util.ToolsUtil;
import jupiterpi.vkbln4ll.Strings;
import jupiterpi.vkbln4ll.repo.Vocabulary;
import jupiterpi.vkbln4ll.repo.VocabularyPortion;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchChannelService {
    public Message search(String[] queries) {
        Map<Vocabulary, String> results = new HashMap<>();
        for (VocabularyPortion portion : VocabularyPortion.getPortions()) {
            for (Vocabulary vocabulary : portion.getVocabularies()) {
                String voc = vocabulary.toString();
                boolean wrong = false;
                for (String query : queries) {
                    if (!voc.contains(query)) {
                        wrong = true;
                        break;
                    }
                }
                if (!wrong) results.put(vocabulary, portion.getId());
            }
        }

        if (results.size() > 0) {
            List<String> vocabulariesResult = new ArrayList<>();
            for (Vocabulary vocabulary : results.keySet()) {
                vocabulariesResult.add(vocabulary.toString());
            }
            String vocabulariesFieldText = ToolsUtil.appendWithSeparator(vocabulariesResult, "\n");
            String portionsFieldText = ToolsUtil.appendWithSeparator(results.values().toArray(new String[0]), "\n");

            if (vocabulariesFieldText.length() > 1020) return new MessageBuilder().setContent(Strings.getString("sc-too-many-results")).build();

            MessageEmbed embed = new EmbedBuilder()
                    .setColor(Color.BLACK)
                    .addField(Strings.getString("sc-embed-vocabularies"), vocabulariesFieldText, true)
                    .addField(Strings.getString("sc-embed-in-portion"), portionsFieldText, true)
                    .build();
            return new MessageBuilder()
                    .setContent(Strings.getString("sc-embed-found"))
                    .setEmbed(embed)
                    .build();
        } else {
            return new MessageBuilder().setContent(Strings.getString("sc-embed-nothing-found")).build();
        }
    }
}