package jupiterpi.vkbln4ll.bot.log.session;

import jupiterpi.tools.files.Path; 
import jupiterpi.tools.files.csv.CSVCastable;
import jupiterpi.tools.files.csv.CSVObjectsFile;
import jupiterpi.tools.util.ToolsUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SessionLog implements CSVCastable {
    private Date start;
    private String portion;
    private Date end;
    private List<String> chat;
    private final String chatLineSeparator = "&";

    private static final Path path = Path.getRunningDirectory().subdir("data").subdir("logs").file("searches.csv");

    private SessionLog(Date start, String portion) {
        this.start = start;
        this.portion = portion;
        chat = new ArrayList<>();
    }
    public static SessionLog create(String portion) {
        return new SessionLog(new Date(), portion);
    }
    public static void log(SessionLog log) {
        CSVObjectsFile<SessionLog> file = new CSVObjectsFile<>(path, SessionLog.class);
        List<SessionLog> logs = file.getObjects();
        logs.add(log);
        file.writeObjects(logs);
    }
    public void setChat(List<String> lines) {
        chat = lines;
    }

    public static List<SessionLog> readAll() {
        CSVObjectsFile<SessionLog> file = new CSVObjectsFile<>(path, SessionLog.class);
        List<SessionLog> logs = file.getObjects();
        return logs;
    }

    /* csv stuff */

    public SessionLog(String[] f) {
        start = new Date(Long.parseLong(f[0]));
        portion = f[1];
        end = new Date(Long.parseLong(f[2]));
        chat = ToolsUtil.asArrayList(f[3].split(chatLineSeparator));
    }

    @Override
    public String[] toCSV() {
        return new String[]{
                Long.toString(start.getTime()), portion, Long.toString(end.getTime()), ToolsUtil.appendWithSeparator(chat, chatLineSeparator)
        };
    }
}