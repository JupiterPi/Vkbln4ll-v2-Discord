package jupiterpi.vkbln4ll.bot.log;

import jupiterpi.tools.files.Path;
import jupiterpi.tools.files.csv.CSVCastable;
import jupiterpi.tools.files.csv.CSVObjectsFile;

import java.util.Date;
import java.util.List;

public class SearchLog implements CSVCastable {
    private Date time;
    private String sender;
    private String query;

    private static final Path path = Path.getRunningDirectory().subdir("data").subdir("logs").file("searches.csv");

    private SearchLog(String sender, String query) {
        time = new Date();
        this.sender = sender;
        this.query = query;
    }
    public static void log(String sender, String query) {
        CSVObjectsFile<SearchLog> file = new CSVObjectsFile<>(path, SearchLog.class);
        List<SearchLog> logs = file.getObjects();
        logs.add(new SearchLog(sender, query));
        file.writeObjects(logs);
    }

    public static List<SearchLog> readAll() {
        CSVObjectsFile<SearchLog> file = new CSVObjectsFile<>(path, SearchLog.class);
        List<SearchLog> logs = file.getObjects();
        return logs;
    }

    /* csv stuff */

    public SearchLog(String[] f) {
        time = new Date(Long.parseLong(f[0]));
        sender = f[1];
        query = f[2];
    }

    @Override
    public String[] toCSV() {
        return new String[]{
                Long.toString(time.getTime()), sender, query
        };
    }
}