package jupiterpi.vkbln4ll.bot.services;

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
                if (text.equals("")) text = "(keine vorhanden)";
                return "Vorhandene Vokabeln: " + text;
            }
            if (parts[0].equals("add-in")) {
                try {
                    if (parts[1].equals("reset")) cache_index = 0;
                    else cache_index = Integer.parseInt(parts[1]); // with 1 based index
                    return "Nächste Vokabel wird an Position " + cache_index + " eingefügt. ";
                } catch (NumberFormatException e) {
                    return parts[1] + " ist keine Zahl...";
                }
            }
            if (parts[0].equals("remove")) {
                try {
                    enteredPortion.removeVocabulary(Integer.parseInt(parts[1])-1);
                    return "Vokabel bei Position " + parts[1] + " wurde entfernt. ";
                } catch (NumberFormatException e) {
                    return parts[1] + " ist keine Zahl...";
                }
            }
            if (msg.equalsIgnoreCase("x")) {
                cache_latin = null;
                return "Abgebrochen. ";
            }
            if (msg.equals("leave")) {
                enteredPortion = null;
                return "Portion verlassen. ";
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

                String text = "Vokabel hinzugefügt: " + vocabulary.toString();
                if (cache_index != 0) {
                    text += " (an Position " + cache_index + "). ";
                    cache_index = 0;
                }
                return text;
            }
        } else {
            if (msg.equals("reload")) {
                VocabularyPortion.reload();
                return "Neu geladen. ";
            }
            if (parts[0].equals("create")) {
                enteredPortion = VocabularyPortion.createPortion(parts[1]);
                return "Portion " + parts[1] + " erstellt und betreten. ";
            }
            if (parts[0].equals("enter")) {
                for (VocabularyPortion portion : VocabularyPortion.getPortions()) {
                    if (portion.getId().equals(parts[1])) {
                        enteredPortion = portion;
                        return "Portion " + parts[1] + " betreten. ";
                    }
                }
                return "Nicht gefunden. ";
            }
            if (parts[0].equals("remove")) {
                VocabularyPortion toDelete = null;
                for (VocabularyPortion portion : VocabularyPortion.getPortions()) {
                    if (portion.getId().equals(parts[1])) toDelete = portion;
                }
                if (toDelete == null) return "Nicht gefunden. ";
                VocabularyPortion.removePortion(toDelete.getId());
                return "Portion " + parts[1] + " wurde entfernt. ";
            }
            if (msg.equals("list")) {
                String text = "";
                for (VocabularyPortion portion : VocabularyPortion.getPortions()) {
                    text += "\n" + portion.getId() + "      (" + portion.getVocabularies().size() + " Vokabeln)";
                }
                if (text.equals("")) text = "(keine vorhanden)";
                return "Vorhandene Vokabelportionen: " + text;
            }
            return "Unbekannter Befehl. ";
        }
    }
}