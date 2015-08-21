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

package io.github.weebobot.weebobot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jibble.pircbot.IrcException;

import io.github.weebobot.weebobot.backend.Backend;
import io.github.weebobot.weebobot.commands.CommandParser;
import io.github.weebobot.weebobot.database.Database;
import io.github.weebobot.weebobot.util.CommandsPage;
import io.github.weebobot.weebobot.util.TFileReader;
import io.github.weebobot.weebobot.util.TOptions;
import io.github.weebobot.weebobot.util.ULevel;
import io.github.weebobot.weebobot.util.WLogger;

public class Main implements Runnable{
	
	private static ArrayList<Backend> listeners;
	private static IRCBot bot;
	private static String[] args;
	private static final String botChannel = "#weebobot";
	private static final Logger logger = Logger.getLogger(Main.class + "");
	
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
	 *            [0] - Twitch OAuth
	 *            [1] - Database Password
	 */
	public static void main(String[] args) {
		listeners = new ArrayList<>();
		Main.args=args;
		listeners.add(new Backend(6668));
		listeners.add(new Backend(6669));
		new Thread(listeners.get(0)).start();
		new Thread(listeners.get(1)).start();
		new Main();
		try(Scanner scan=new Scanner(System.in)) {
			while(true) {
				String message = scan.nextLine();
				String[] params = message.substring(message.indexOf(' ') + 1).split(" ");
				String command;
				try {
					command = message.substring(1, message.indexOf(' '));
				} catch(StringIndexOutOfBoundsException e) {
					command = message.substring(1, message.length());
				}
				if(command.equalsIgnoreCase(params[0].substring(1))) {
					params = new String[0];
				}
				CommandParser.parse(command, getBotChannel().substring(1), getBotChannel(), params);
			}
		}
	}

	/** 
	 * Performs all of the setup for the bot on first run.
	 */
	@Override
	public void run() {
		Database.initDBConnection(args[1]);
		bot = new IRCBot();

		bot.setVerbose(true);
		try {
			bot.connect(
					"irc.twitch.tv", 6667, args[0]);
		} catch (IOException | IrcException e) {
			logger.log(Level.SEVERE, "An error occurred while connecting to Twitch IRC", e);
			WLogger.logError(e);
		}
		bot.sendRawLine("CAP REQ :twitch.tv/membership");
		bot.setMessageDelay(1700);
		joinChannel(getBotChannel(), false);
		CommandParser.init();
		
		File f = new File("connectedChannels.txt");
		if (f.exists()) {
			for (String s : TFileReader.readFile(f)) {
				joinChannel(s, true);
			}
			f.delete();
		}
	}

	/**
	 * Performs all of the setup for the bot in the channel specified.
	 */
	public static void joinChannel(String channel, boolean isReJoin) {

		if(bot.isWatchingChannel(channel)) {
			return;
		}

		boolean firstTime = false;
		if (Database.getChannelTables(channel.substring(1))) {
			firstTime = true;
			Database.addMod(botChannel.substring(1), channel.substring(1));
			Database.addMod("mysteriousmage", channel.substring(1));
			Database.addMod("donald10101", channel.substring(1));
			if(!Database.isMod(channel.substring(1), channel.substring(1))) {
				Database.addMod(channel.substring(1), channel.substring(1));
			}
			Database.addOption(channel.substring(1), TOptions.welcomeMessage.getOptionID(), "Welcome %user% to our channel, may you find it entertaining or flat out enjoyable.");
			Database.addOption(channel.substring(1), TOptions.numCaps.getOptionID(), "20");
			Database.addOption(channel.substring(1), TOptions.numEmotes.getOptionID(), "20");
			Database.addOption(channel.substring(1), TOptions.numSymbols.getOptionID(), "20");
			Database.addOption(channel.substring(1), TOptions.link.getOptionID(), "0");
			Database.addOption(channel.substring(1), TOptions.regular.getOptionID(), "288");
			Database.addOption(channel.substring(1), TOptions.paragraphLength.getOptionID(), "400");
			Database.addOption(channel.substring(1), ULevel.Moderator.getName() + "Immunities", "111111");
			Database.addOption(channel.substring(1), ULevel.Subscriber.getName() + "Immunities", "001111");
			Database.addOption(channel.substring(1), ULevel.Regular.getName() + "Immunities", "000011");
			Database.addOption(channel.substring(1), ULevel.Follower.getName() + "Immunities", "000000");
			Database.addOption(channel.substring(1), ULevel.Normal.getName() + "Immunities", "000000");
		}
		
		bot.joinChannel(channel);
		bot.setWelcomeEnabled(channel, true);
		bot.setConfirmationEnabled(channel, true);
		bot.setSlowMode(channel, false);
		bot.setSubMode(channel, false);
		bot.setReJoin(channel, isReJoin);
		CommandsPage.createCommandsHTML(channel.substring(1));
		if (firstTime) {
			bot.onFirstJoin(channel);
		}
	}
	
	/**
	 * Makes the bot leave the channel specified
	 * 
	 * @param channel - channel to be left
	 */
	public static void partChannel(String channel) {
		bot.partChannel(channel);
		bot.removeWelcomeEnabled(channel);
		bot.removeConfirmationReplies(channel);
		bot.removeSlowMode(channel);
		bot.removeSubMode(channel);
	}
	
	/**
	 * @return - the instance of IRCBot
	 */
	public static IRCBot getBot() {
		return bot;
	}

	/**
	 * @return - the main channel we are running in
	 */
	public static String getBotChannel() {
		return botChannel;
	}

	/**
	 * @param moderator - name of the person to check
	 * @param channelNoHash - name of the channel without the
	 * leading #
	 * @return true if the person passed is a moderator added when
	 * the table is set up to begin with
	 */
	public static boolean isDefaultMod(String moderator, String channelNoHash) {
		return moderator.equalsIgnoreCase(channelNoHash) || moderator.equalsIgnoreCase("donald10101") || moderator.equalsIgnoreCase("mysteriousmage") || moderator.equalsIgnoreCase(botChannel.substring(1));
	}

	public static void shutdownListeners() {
		for(Backend b: listeners){
			b.stop();
		}
	}


}
