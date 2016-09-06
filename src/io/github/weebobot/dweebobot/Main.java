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
import io.github.weebobot.dweebobot.database.Database;
import io.github.weebobot.dweebobot.external.SoundCloudUtilities;
import io.github.weebobot.dweebobot.external.YoutubeUtilities;
import io.github.weebobot.dweebobot.util.EmoteRunnable;
import io.github.weebobot.dweebobot.util.TOptions;
import io.github.weebobot.dweebobot.util.ULevel;
import io.github.weebobot.dweebobot.util.WLogger;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;

import java.util.ArrayList;
import java.util.Scanner;

public class Main implements Runnable{
	
	private static ArrayList<Backend> listeners;
	private static DWeeboBot dweebobot;
	private static IDiscordClient bot;
	private static String[] args;
	private static final String botChannel = "#dweebobot";
	
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
				CommandParser.parse(command, getBotChannel().substring(1), getBotChannel(), params);
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
		Database.initDBConnection(args[1]);
		YoutubeUtilities.init(args[2]);
		SoundCloudUtilities.setClientSecret(args[3]);
		new EmoteRunnable();
		dweebobot = new DWeeboBot();
		try {
			bot = new ClientBuilder().withToken(args[0]).login();
		} catch (DiscordException e) {
			WLogger.logError(e);
		}
	}

	/**
	 * Performs all of the setup for the bot in the channel specified.
	 */
	public static void joinChannel(String channel) {
		boolean firstTime = false;
		if (Database.getChannelTables(channel.substring(1))) {
			firstTime = true;
			Database.addMod(botChannel.substring(1), channel.substring(1));
			Database.addMod("mysteriousmage", channel.substring(1));
			Database.addMod("donald10101", channel.substring(1));
			if(!Database.isMod(channel.substring(1), channel.substring(1))) {
				Database.addMod(channel.substring(1), channel.substring(1));
			}
			Database.addOption(channel.substring(1), TOptions.welcomeMessage.getOptionID(), "none");
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

//		dweebobot.joinChannel(channel);
//		dweebobot.setWelcomeEnabled(channel, true);
//		dweebobot.setConfirmationEnabled(channel, true);
//		dweebobot.setSlowMode(channel, false);
//		dweebobot.setSubMode(channel, false);
//		dweebobot.setReJoin(channel, isReJoin);
//		CommandsPage.createCommandsHTML(channel.substring(1));
		if (firstTime) {
//			bot.onFirstJoin(channel);
		}
	}
	
	/**
	 * Makes the bot leave the channel specified
	 * 
	 * @param channel - channel to be left
	 */
//	public static void partChannel(String channel) {
//		bot.partChannel(channel);
//		bot.removeWelcomeEnabled(channel);
//		bot.removeConfirmationReplies(channel);
//		bot.removeSlowMode(channel);
//		bot.removeSubMode(channel);
//	}
	
	/**
	 * @return - the instance of DWeeboBot
	 */
	public static IDiscordClient getBot() {
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
		listeners.forEach(Backend::stop);
	}


}
