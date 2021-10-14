package jupiterpi.vkbln4ll;

import jupiterpi.tools.files.TextFile;
import jupiterpi.vkbln4ll.bot.Bot;

import javax.security.auth.login.LoginException;

public class Main {
    public static void main(String[] args) throws LoginException, InterruptedException {
        TextFile tokenFile = new TextFile(Paths.token);
        String token = tokenFile.getLine(0);
        new Bot(token);
    }
}