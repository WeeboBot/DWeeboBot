/*	  It's a Twitch bot, because we can.
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

import io.github.weebobot.dweebobot.backend.Backend;
import io.github.weebobot.dweebobot.commands.CommandParser;
import io.github.weebobot.dweebobot.commands.Shorten;
import io.github.weebobot.dweebobot.database.Database;
import io.github.weebobot.dweebobot.external.DiscordListener;
import io.github.weebobot.dweebobot.external.SoundCloudUtilities;
import io.github.weebobot.dweebobot.external.YoutubeUtilities;
import io.github.weebobot.dweebobot.util.TOptions;
import io.github.weebobot.dweebobot.util.ULevel;
import io.github.weebobot.dweebobot.util.WLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.DiscordException;

import java.util.ArrayList;
import java.util.Scanner;

public class Main implements Runnable{

    public static final int MAX_USER_LEVEL = 9999;
    private static Logger logger = LoggerFactory.getLogger(Main.class);
	private static ArrayList<Backend> listeners;
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
		listeners = new ArrayList<>();
		listeners.add(new Backend(6668));
		listeners.add(new Backend(6669));
		new Thread(listeners.get(0)).start();
		new Thread(listeners.get(1)).start();
		if(!Database.initDBConnection(args[1])) {
            shutdown();
        }
		YoutubeUtilities.init(args[2]);
		SoundCloudUtilities.setClientSecret(args[3]);
        Shorten.setApiKey(args[4]);
		dweebobot = new DWeeboBot();
		try {
			bot = new ClientBuilder().withToken(args[0]).login();
		} catch (DiscordException e) {
			logger.error("There was an issues with discord", e);
            WLogger.logError(e);
            e.printStackTrace();
		}
	}

	/**
	 * Performs all of the setup for the bot in the channel specified.
	 */
	public static void joinChannel(IGuild g) {
		boolean firstTime = false;
		if (Database.getChannelTables(g.getID())) {
			firstTime = true;
			Database.addOption(g.getID(), TOptions.welcomeMessage.getOptionID(), "none");
			Database.addOption(g.getID(), TOptions.welcomeChannel.getOptionID(), g.getChannels().get(0).getID());
			Database.addOption(g.getID(), TOptions.numCaps.getOptionID(), "20");
			Database.addOption(g.getID(), TOptions.numEmotes.getOptionID(), "20");
			Database.addOption(g.getID(), TOptions.numSymbols.getOptionID(), "20");
			Database.addOption(g.getID(), TOptions.link.getOptionID(), "0");
			Database.addOption(g.getID(), TOptions.regular.getOptionID(), "288");
			Database.addOption(g.getID(), TOptions.paragraphLength.getOptionID(), "400");
			Database.addOption(g.getID(), ULevel.Moderator.getName() + "Immunities", "111111");
			Database.addOption(g.getID(), ULevel.Subscriber.getName() + "Immunities", "001111");
			Database.addOption(g.getID(), ULevel.Regular.getName() + "Immunities", "000011");
			Database.addOption(g.getID(), ULevel.Follower.getName() + "Immunities", "000000");
			Database.addOption(g.getID(), ULevel.Normal.getName() + "Immunities", "000000");
		}


		if (firstTime) {
			dweebobot.onFirstJoin(g);
		}
	}
	
	/**
	 * @return - the instance of DWeeboBot
	 */
	public static IDiscordClient getBot() {
		return bot;
	}

	public static void shutdownListeners() {
		listeners.forEach(Backend::stop);
	}


    public static void shutdown() {
        shutdownListeners();
        while(DiscordListener.ActionQueue.getQueueSize() > 0);
        System.exit(0);
    }
}
