package jupiterpi.vkbln4ll.bot.log;

import jupiterpi.tools.files.Path;
import jupiterpi.tools.files.csv.CSVCastable;
import jupiterpi.tools.files.csv.CSVObjectsFile;

import java.util.Date;
import java.util.List;

public class PrivateMessageLog implements CSVCastable {
    private Date time;
    private String sender;
    private String content;

    private static final Path path = Path.getRunningDirectory().subdir("data").subdir("logs").file("private_messages.csv");

    private PrivateMessageLog(String sender, String content) {
        time = new Date();
        this.sender = sender;
        this.content = content;
    }
    public static void log(String sender, String content) {
        CSVObjectsFile<PrivateMessageLog> file = new CSVObjectsFile<>(path, PrivateMessageLog.class);
        List<PrivateMessageLog> logs = file.getObjects();
        logs.add(new PrivateMessageLog(sender, content));
        file.writeObjects(logs);
    }

    public static List<PrivateMessageLog> readAll() {
        CSVObjectsFile<PrivateMessageLog> file = new CSVObjectsFile<>(path, PrivateMessageLog.class);
        List<PrivateMessageLog> logs = file.getObjects();
        return logs;
    }

    /* csv stuff */

    public PrivateMessageLog(String[] f) {
        time = new Date(Long.parseLong(f[0]));
        sender = f[1];
        content = f[2];
    }

    @Override
    public String[] toCSV() {
        return new String[]{
            Long.toString(time.getTime()), sender, content
        };
    }
}