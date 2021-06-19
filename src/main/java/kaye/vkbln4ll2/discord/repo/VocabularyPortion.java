package kaye.vkbln4ll2.discord.repo;

import jupiterpi.tools.files.Path;
import jupiterpi.tools.files.TextFile;
import jupiterpi.tools.files.WrongPathTypeException;
import jupiterpi.tools.files.csv.CSVObjectsFile;

import java.util.ArrayList;
import java.util.List;

public class VocabularyPortion {
    private Path path;

    private String id;
    private List<Vocabulary> vocabularies;

    public VocabularyPortion(Path path) {
        this.path = path;
        id = path.getFileName().split("\\.")[0];
        vocabularies = new CSVObjectsFile<Vocabulary>(new TextFile(path, true), Vocabulary.class).getObjects();
    }

    /* edit */

    public String getId() {
        return id;
    }

    public List<Vocabulary> getVocabularies() {
        return vocabularies;
    }

    public Vocabulary addVocabulary(String latin, String[] german) {
        Vocabulary vocabulary = new Vocabulary(latin, german);
        vocabularies.add(vocabulary);
        return vocabulary;
    }

    public Vocabulary addVocabulary(String latin, String[] german, int index) {
        Vocabulary vocabulary = new Vocabulary(latin, german);
        vocabularies.add(index, vocabulary);
        return vocabulary;
    }

    public void removeVocabulary(int index) {
        vocabularies.remove(index);
    }

    /* write */

    public void write() {
        CSVObjectsFile<Vocabulary> file = new CSVObjectsFile<>(path, Vocabulary.class);
        file.writeObjects(vocabularies);
    }

    /* --- repo --- */

    private static final Path dataDir = Path.getRunningDirectory().subdir("data").subdir("voc");

    private static List<VocabularyPortion> portions = getPortions();

    public static void reload() {
        try {
            portions = new ArrayList<>();
            for (Path file : dataDir.subfiles()) {
                if (file.getFileName().endsWith(".csv")) portions.add(new VocabularyPortion(file));
            }
        } catch (WrongPathTypeException ignored) {}
    }

    public static List<VocabularyPortion> getPortions() {
        if (portions == null) reload();
        return portions;
    }

    public static VocabularyPortion createPortion(String id) {
        VocabularyPortion portion = new VocabularyPortion(dataDir.copy().file(id + ".csv"));
        portions.add(portion);
        return portion;
    }

    public static void removePortion(String id) {
        dataDir.copy().file(id + ".csv").file().delete();
        reload();
    }

    public static void writeAll() {
        for (VocabularyPortion portion : portions) {
            portion.write();
        }
    }
}