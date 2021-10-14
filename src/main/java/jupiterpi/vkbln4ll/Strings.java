package jupiterpi.vkbln4ll;

import jupiterpi.tools.files.Path;
import jupiterpi.tools.files.TextFile;

import java.util.HashMap;
import java.util.Map;

public class Strings {
    private static final Path stringsFilePath = Paths.strings;

    private static Map<String, String> strings = new HashMap<>();

    static {
        for (String line : new TextFile(stringsFilePath).getFile()) {
            if (line.isEmpty()) continue;
            String[] parts = line.split(": ");
            strings.put(parts[0], parts[1]);
        }
    }

    public static String getString(String name) {
        return strings.get(name);
    }
}
