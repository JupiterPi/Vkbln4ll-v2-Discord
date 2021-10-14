package jupiterpi.vkbln4ll.bot.services;

import jupiterpi.vkbln4ll.Strings;
import jupiterpi.vkbln4ll.repo.Vocabulary;
import jupiterpi.vkbln4ll.repo.VocabularyPortion;

public class WritingChannelService {
    private VocabularyPortion enteredPortion = null;
    private String cache_latin = null;
    private int cache_index = 0; // with 1 based index

    public String onInput(String msg) {
        String[] parts = msg.split(" ");
        if (enteredPortion != null) {
            if (msg.equals("list")) {
                String text = "";
                for (int i = 0; i < enteredPortion.getVocabularies().size(); i++) {
                    Vocabulary vocabulary = enteredPortion.getVocabularies().get(i);
                    text += "\n(" + (i+1) + ") " + vocabulary.toString(); // with 1 based index
                }
                if (text.equals("")) text = Strings.getString("w-vocabularies-list-empty");
                return Strings.getString("w-vocabularies-list-title") + " " + text;
            }
            if (parts[0].equals("add-in")) {
                try {
                    if (parts[1].equals("reset")) cache_index = 0;
                    else cache_index = Integer.parseInt(parts[1]); // with 1 based index
                    return String.format(Strings.getString("w-index-set"), cache_index);
                } catch (NumberFormatException e) {
                    return String.format(Strings.getString("w-nan"), parts[1]);
                }
            }
            if (parts[0].equals("remove")) {
                try {
                    enteredPortion.removeVocabulary(Integer.parseInt(parts[1])-1);
                    return String.format(Strings.getString("w-vocabulary-removed"), parts[1]);
                } catch (NumberFormatException e) {
                    return String.format(Strings.getString("w-nan"), parts[1]);
                }
            }
            if (msg.equalsIgnoreCase("x")) {
                cache_latin = null;
                return Strings.getString("w-canceled");
            }
            if (msg.equals("leave")) {
                enteredPortion = null;
                return Strings.getString("w-left");
            }
            if (cache_latin == null) {
                cache_latin = msg;
                return null;
            } else {
                String[] german = msg.split(", ");
                Vocabulary vocabulary;
                if (cache_index == 0) vocabulary = enteredPortion.addVocabulary(cache_latin, german);
                else vocabulary = enteredPortion.addVocabulary(cache_latin, german, cache_index-1);
                enteredPortion.write();
                cache_latin = null;

                String text = Strings.getString("w-added") + " " + vocabulary.toString();
                if (cache_index != 0) {
                    text += String.format(Strings.getString("w-at-position"), cache_index);
                    cache_index = 0;
                }
                return text;
            }
        } else {
            if (msg.equals("reload")) {
                VocabularyPortion.reload();
                return Strings.getString("w-reloaded");
            }
            if (parts[0].equals("create")) {
                enteredPortion = VocabularyPortion.createPortion(parts[1]);
                return String.format(Strings.getString("w-created-and-entered"), parts[1]);
            }
            if (parts[0].equals("enter")) {
                for (VocabularyPortion portion : VocabularyPortion.getPortions()) {
                    if (portion.getId().equals(parts[1])) {
                        enteredPortion = portion;
                        return String.format(Strings.getString("w-entered"), parts[1]);
                    }
                }
                return Strings.getString("w-not-found");
            }
            if (parts[0].equals("remove")) {
                VocabularyPortion toDelete = null;
                for (VocabularyPortion portion : VocabularyPortion.getPortions()) {
                    if (portion.getId().equals(parts[1])) toDelete = portion;
                }
                if (toDelete == null) return Strings.getString("w-not-found");
                VocabularyPortion.removePortion(toDelete.getId());
                return String.format(Strings.getString("w-portion-removed"), parts[1]);
            }
            if (msg.equals("list")) {
                String text = "";
                for (VocabularyPortion portion : VocabularyPortion.getPortions()) {
                    text += "\n" + portion.getId() + "      (" + portion.getVocabularies().size() + " " + Strings.getString("w-portions-list-size-unit") + ")";
                }
                if (text.equals("")) text = Strings.getString("w-portions-list-empty");
                return Strings.getString("w-portions-list-title") + text;
            }
            return Strings.getString("w-unknown-command");
        }
    }
}