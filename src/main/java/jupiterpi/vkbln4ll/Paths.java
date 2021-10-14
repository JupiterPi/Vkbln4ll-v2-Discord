package jupiterpi.vkbln4ll;

import jupiterpi.tools.files.Path;

public class Paths {
    public static final Path dataDir = Path.getRunningDirectory().subdir("data");

    public static final Path properties = dataDir.copy().file("properties.txt");
    public static final Path token = dataDir.copy().file("token.txt");

    public static final Path help = dataDir.copy().file("help.txt");
}
