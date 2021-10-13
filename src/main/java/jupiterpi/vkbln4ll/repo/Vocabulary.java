package jupiterpi.vkbln4ll.repo;

import jupiterpi.tools.files.csv.CSVCastable;
import jupiterpi.tools.util.ToolsUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Vocabulary implements CSVCastable {
    private static final String REGEX = ",";

    private String latin;
    private List<String> german;

    /* constructors */

    public Vocabulary(String latin, String[] german) {
        this.latin = latin;
        this.german = ToolsUtil.asArrayList(german);
    }

    /* accessors */

    public String getLatin() {
        return latin;
    }

    public List<String> getGerman() {
        return new ArrayList<>(german);
    }
    public String getGermanStr() {
        return ToolsUtil.appendWithSeparator(german, ", ");
    }

    public String toString() { return toStringLD(); }
    public String toStringLD() {
        return getLatin() + " | " + getGermanStr();
    }

    public String toStringDL() {
        return getGermanStr() + " | " + getLatin();
    }

    /* csv stuff */

    public Vocabulary(String[] f) {
        latin = f[0];
        String germanStr = f[1]
                .replaceAll("#a", "ä")
                .replaceAll("#o", "ö")
                .replaceAll("#u", "ü")
                .replaceAll("#s", "ß");
        german = new ArrayList<>(Arrays.asList(germanStr.split(REGEX)));
    }

    @Override
    public String[] toCSV() {
        String germanStr = ToolsUtil.appendWithSeparator(german, REGEX)
                .replaceAll("ä", "#a")
                .replaceAll("ö", "#o")
                .replaceAll("ü", "#u")
                .replaceAll("ß", "#s");

        return new String[]{
                latin, germanStr
        };
    }
}