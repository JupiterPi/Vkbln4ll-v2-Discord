package jupiterpi.vkbln4ll.bot.log;

import jupiterpi.tools.files.Path;

public class Logger {
    private static Logger logger = new Logger();
    public static Logger get() { return logger; }

    private final Path logsDir = Path.getRunningDirectory().subdir("data").subdir("logs");

    private final Path privateMessagesPath = logsDir.copy().file("private.csv");
}