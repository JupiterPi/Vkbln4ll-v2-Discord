package kaye.vkbln4ll2.discord;

import jupiterpi.tools.files.Path;
import jupiterpi.tools.files.TextFile;
import kaye.vkbln4ll2.discord.bot.Bot;

import javax.security.auth.login.LoginException;

public class Main {
    public static void main(String[] args) throws LoginException, InterruptedException {
        TextFile tokenFile = new TextFile(Path.getRunningDirectory().subdir("data").file("token.txt"));
        String token = tokenFile.getLine(0);
        new Bot(token);
    }
}