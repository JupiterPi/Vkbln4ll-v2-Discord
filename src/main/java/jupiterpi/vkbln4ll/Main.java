package jupiterpi.vkbln4ll;

import jupiterpi.tools.files.TextFile;
import jupiterpi.vkbln4ll.bot.Bot;

import javax.security.auth.login.LoginException;

public class Main {
    public static final boolean debugMode = System.getProperty("debug").equals("true");

    public static void main(String[] args) throws LoginException, InterruptedException {
        System.out.println("debug: " + debugMode);

        TextFile tokenFile = new TextFile(Paths.token);
        String token = tokenFile.getLine(0);
        new Bot(token);
    }
}