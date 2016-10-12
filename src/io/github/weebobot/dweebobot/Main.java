/*      It's a Twitch bot, because we can.
 *    Copyright (C) 2015  Timothy Chandler, James Wolff
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.weebobot.dweebobot;

import io.github.weebobot.dweebobot.commands.CommandParser;
import io.github.weebobot.dweebobot.commands.Shorten;
import io.github.weebobot.dweebobot.database.Database;
import io.github.weebobot.dweebobot.external.DiscordListener;
import io.github.weebobot.dweebobot.external.SoundCloudUtilities;
import io.github.weebobot.dweebobot.external.YoutubeUtilities;
import io.github.weebobot.dweebobot.util.WLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

import java.util.Scanner;

public class Main implements Runnable{

    /**
     * The user level required to preform commands that are command-line specific, Guild owners are this-1 and regular users cannot be greater than this-2
     */
    public static final int MAX_USER_LEVEL = 9999;
    private static Logger logger = LoggerFactory.getLogger(Main.class);
    private static DWeeboBot dweebobot;
    private static IDiscordClient bot;
    private static String[] args;

    /**
     * Starts a new thread for the bot to exists in so we can pass
     * bot commands on the command line.
     */
    public Main() {
        new Thread(this).start();
    }

    /**
     *
     * @param args
     *            [0] - Discord OAuth
     *            [1] - Database Password
     *            [2] - YouTube Developers API Key
     *            [3] - Soundcloud CLIENT_SECRET
     *            [4] - Bit.ly API Key
     */
    public static void main(String[] args) {
        Main.args = args;
        new Main();
        try(Scanner scan=new Scanner(System.in)) {
            while(true) {
                String message = scan.nextLine();
                String[] params = message.substring(message.indexOf(' ') + 1).split(" ");
                String command;
                try {
                    command = message.substring(1, message.indexOf(' '));
                } catch(StringIndexOutOfBoundsException e) {
                    try{
                        command = message.substring(1, message.length());
                    }catch(StringIndexOutOfBoundsException e1){
                        command = " ";
                    }
                }
                if(command.equalsIgnoreCase(params[0].substring(1))) {
                    params = new String[0];
                }
                logger.info(CommandParser.parse(command, params));
            }
        }
    }

    public static DWeeboBot getDWeeboBot() {
        return dweebobot;
    }

    /**
     * Performs all of the setup for the bot on first run.
     */
    @Override
    public void run() {
        if(!Database.initDBConnection(args[1])) {
            shutdown();
        }
        CommandParser.init();
        YoutubeUtilities.init(args[2]);
        SoundCloudUtilities.setClientSecret(args[3]);
        Shorten.setApiKey(args[4]);
        dweebobot = new DWeeboBot();
        DiscordListener.init();
        try {
            bot = new ClientBuilder().withToken(args[0]).login();
            bot.getDispatcher().registerListener(new DiscordListener());
        } catch (DiscordException e) {
            logger.error("There was an issues with discord", e);
            WLogger.logError(e);
            e.printStackTrace();
        }
    }

    /**
     * @return - the instance of DWeeboBot
     */
    public static IDiscordClient getBot() {
        return bot;
    }


    public static void shutdown() {
        while(DiscordListener.ActionQueue.getQueueSize() > 0);
        System.exit(0);
    }
}
