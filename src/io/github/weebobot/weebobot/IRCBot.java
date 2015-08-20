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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import io.github.weebobot.weebobot.commands.AddModerator;
import io.github.weebobot.weebobot.commands.CommandParser;
import io.github.weebobot.weebobot.commands.DelModerator;
import io.github.weebobot.weebobot.customcommands.CustomCommandParser;
import io.github.weebobot.weebobot.database.Database;
import io.github.weebobot.weebobot.twitch.TwitchUtilities;
import io.github.weebobot.weebobot.util.DelayedPermitTask;
import io.github.weebobot.weebobot.util.DelayedReJoin;
import io.github.weebobot.weebobot.util.DelayedWelcomeTask;
import io.github.weebobot.weebobot.util.PointsRunnable;
import io.github.weebobot.weebobot.util.TOptions;
import io.github.weebobot.weebobot.util.TType;
import io.github.weebobot.weebobot.util.Timeouts;
import io.github.weebobot.weebobot.util.WLogger;

/**
 *
 * @author JewsOfHazard, Donald10101, And Angablade.
 */

public class IRCBot extends PircBot {

	private static HashMap<String, String> chatPostSeen;
	private static HashMap<String, Boolean> welcomeEnabled;
	private static HashMap<String, Boolean> confirmationReplies;
	private static HashMap<String, Boolean> slowMode;
	private static HashMap<String, Boolean> subMode;
	private static HashMap<String, Boolean> isReJoin;
	private static HashMap<String, io.github.weebobot.weebobot.util.PollUtil> polls;
	private static HashMap<String, io.github.weebobot.weebobot.util.RaffleUtil> raffles;
	private static HashMap<String, io.github.weebobot.weebobot.util.VoteTimeOutUtill> voteTimeOuts;
	private static HashMap<String, ArrayList<DelayedPermitTask>> permits;
	private static HashMap<String, ArrayList<String>> welcomes;
	private static final Logger logger = Logger.getLogger(IRCBot.class + "");

	/**
	 * Creates a new instance of IRCBot for the specified channel
	 */
	public IRCBot() {
		this.setName(Main.getBotChannel().substring(1));
		initVariables();
	}

	/**
	 * initializes all of our HashMaps
	 */
	private void initVariables() {
		welcomeEnabled = new HashMap<>();
		confirmationReplies = new HashMap<>();
		chatPostSeen = new HashMap<>();
		slowMode = new HashMap<>();
		subMode = new HashMap<>();
		isReJoin = new HashMap<>();
		polls = new HashMap<>();
		raffles = new HashMap<>();
		permits = new HashMap<>();
		welcomes = new HashMap<>();
	}

	/**
	 * Ensures that a channel moderator is added to the bot's moderator list
	 */
	@Override
	protected void onOp(String channel, String sourceNick, String sourceLogin,
			String sourceHostname, String recipient) {
		try {
			new AddModerator().execute(Main.getBotChannel().substring(1),
					channel, new String[] { recipient });
		} catch (Exception e) {
			logger.log(Level.WARNING,
					"An error was thrown while executing onOp() in " + channel,
					e);
			WLogger.logError(e);
		}
	}

	/**
	 * Ensures that anyone who is unmodded in the channel is removed from the
	 * bot's moderator list
	 */
	@Override
	protected void onDeop(String channel, String sourceNick,
			String sourceLogin, String sourceHostname, String recipient) {
		try {
			if (Main.isDefaultMod(recipient, channel.substring(1))) {
				return;
			}
			new DelModerator().execute(Main.getBotChannel().substring(1),
					recipient, new String[] { sourceHostname });
		} catch (Exception e) {
			logger.log(Level.WARNING,
					"An error was thrown while executing onDeop() in "
							+ channel, e);
			WLogger.logError(e);
		}
	}

	/**
	 * Decides how to welcome a user and then sends that message to the the
	 * channel.
	 * 
	 * Also starts points accumulation for that user in the channel
	 */
	@Override
	public void onJoin(String channel, String sender, String login,
			String hostname) {
		try {
			if (sender.equalsIgnoreCase(Main.getBotChannel().substring(1))) {
				sendMessage(
						channel,
						"I have joined the channel and will stay with you unless you tell me to !leave or my creators do not shut me down properly because they are cruel people with devious minds.");
				for (User u : getUsers(channel)) {
					new PointsRunnable(u.getNick(), channel.substring(1));
					Database.addUser(channel.substring(1), sender);
				}
			}
			if (welcomeEnabled.get(channel) && !isReJoin.containsKey(channel)) {
				String msg = Database.getWelcomeMessage(channel.substring(1))
						.replace("%user%", sender);
				if (!msg.equalsIgnoreCase("none")
						&& !recentlyWelcomed(sender, channel)) {
					sendMessage(channel, msg);
					addWelcome(channel, sender);
				}
			}
			new PointsRunnable(sender, channel.substring(1));
			Database.addUser(channel.substring(1), sender);
		} catch (Exception e) {
			logger.log(Level.SEVERE,
					"An error occurred while executing onJoin()", e);
			WLogger.logError(e);
		}
	}

	/**
	 * Sends a message to the bot's channel when someone leaves a channel the
	 * bot is in
	 * 
	 * Stops the point accumulation for that user in the channel specified
	 */
	@Override
	public void onPart(String channel, String sender, String login,
			String hostname) {
		PointsRunnable.removeChannelFromUser(sender, channel.substring(1));
	}

	/**
	 * Handles spam checking, last seen, commands, and custom
	 * commands
	 */
	@Override
	public void onMessage(String channel, String sender, String login,
			String hostname, String message) {
		try {
			checkSpam(channel, message, sender);
			if (sender.equalsIgnoreCase(Main.getBotChannel().substring(1))) {
				return;
			}
			if (message.charAt(0) == '!') {
				String[] params = message.substring(message.indexOf(' ') + 1)
						.split(" ");
				String command;
				try {
					command = message.substring(1, message.indexOf(' '));
				} catch (StringIndexOutOfBoundsException e) {
					command = message.substring(1, message.length());
				}
				if (command.equalsIgnoreCase(params[0].substring(1))) {
					params = new String[0];
				}
				String reply = CommandParser.parse(command, sender, channel,
						params);
				if (reply != null) {
					sendMessage(channel, reply);
				}
				reply = CustomCommandParser.parse(command.toLowerCase(),
						sender, channel, params);
				if (reply != null) {
					sendMessage(channel, reply);
				}
			}
			chatPostSeen.put(sender,
					channel.substring(1) + "|" + new Date().toString());
		} catch (Exception e) {
			logger.log(Level.WARNING,
					"An error was thrown while executing onMessage() in "
							+ channel, e);
			WLogger.logError(e);
		}
	}

	/**
	 * Sends message when the bot join's a channel for the first time.
	 */
	public void onFirstJoin(String channel) {
		sendMessage(
				channel,
				"Hello, this appears to be the first time you have invited me to join your channel. We just have a few preliminary matters to attend to. First off make sure to mod me so I don't get timed out, then type !setup");
	}
	
	/**
	 * If IRC sends something PIRCBot doesn't recognize this is called.
	 */
	public void onUnkown(String line){
		logger.info(line);
		WLogger.log("Unknown IRC Line: " + line);
	}

	/**
	 * @param channel
	 *            - channel we might be in
	 * @return true if we are in the channel specified
	 */
	public boolean isWatchingChannel(String channel) {
		for (String s : getChannels()) {
			if (s.equalsIgnoreCase(channel)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param channel
	 *            - channel the massage happened in
	 * @param message
	 *            - message that might contain spam
	 * @param sender
	 *            - the person the sent the message
	 *            blacklist, paragraph, caps, emotes, links, symbols;
	 *            111111  -  Moderator
	 *            001111  -  Subscriber
	 *            000011  -  Regular
	 *            000000  -  Follower
	 *            000000  -  Normal
	 */
	public void checkSpam(String channel, String message, String sender) {
		String level = Database.getUserLevel(channel.substring(1), sender);
		boolean[] immunities = Database.getImmunities(channel.substring(1), level);
		int caps = Integer.valueOf(Database.getOption(channel.substring(1), TOptions.numCaps.getOptionID()));
		int symbols = Integer.valueOf(Database.getOption(channel.substring(1), TOptions.numSymbols.getOptionID()));
		int link = Integer.valueOf(Database.getOption(channel.substring(1), TOptions.link.getOptionID()));
		int paragraph = Integer.valueOf(Database.getOption(channel.substring(1), TOptions.paragraphLength.getOptionID()));
		int emotes = Integer.valueOf(Database.getOption(channel.substring(1), TOptions.numEmotes.getOptionID()));
		if(!immunities[0]){
			ResultSet rs = Database.getSpam(channel.substring(1));
			try {
				while (rs.next()) {
					if (message.matches("(" + rs.getString(1) + ")+")){
						new Timeouts(channel, sender, 1, TType.SPAM);
					}
				}
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "An error occurred checking if "
						+ sender + "'s message has bad words", e);
				WLogger.logError(e);
			}
		}
		if(!immunities[1] && paragraph != -1 && message.length() >= paragraph){
			new Timeouts(channel, sender, 1, TType.PARAGRAPH);
		}
		if(!immunities[2] && caps != -1 && message.matches("[A-Z\\s]{" + caps + ",}")){
			new Timeouts(channel, sender, 1, TType.CAPS);
		}
		if(!immunities[3] && emotes != -1) {
			String emoteList=Database.getEmoteList(channel.substring(1));
			if(emoteList != null && message.matches("(" + emoteList + "\\s){" + emotes + ",}")) {
				new Timeouts(channel, sender, 1, TType.EMOTE);
			}
		}
		if(!immunities[4] && link !=-1){
			if (message.matches("([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})")
					|| message.matches("(https?:\\/\\/)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([\\/\\w \\.-]*)*\\/?")
					&& link != -1) {
				if (!isPermitted(channel, sender)) {
					new Timeouts(channel, sender, 1, TType.LINK);
				} else {
					removePermit(channel, sender);
				}
			}
		}
		if(!immunities[5] && symbols != -1
				&& message.matches("[\\W_\\s]{" + symbols + ",}")){
			new Timeouts(channel, sender, 1, TType.SYMBOLS);
		}
		if (!Database.isMod(sender, channel.substring(1))
				&& !Database.isRegular(sender, channel.substring(1))
				&& !TwitchUtilities.isSubscriber(sender, channel.substring(1))) {
			
		}
	}

	/**
	 * @param channel
	 *            - channel the link was sent in
	 * @param sender
	 *            - person who might be permitted to post a link
	 * @return true if sender is permitted in channel
	 */
	public boolean isPermitted(String channel, String sender) {
		ArrayList<DelayedPermitTask> ps = permits.get(sender);
		if (ps == null) {
			return false;
		}
		for (DelayedPermitTask p : ps) {
			if (p.getChannel().equalsIgnoreCase(channel)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param channel
	 *            - channel that we are setting the value for
	 * @param value
	 *            - true if welcome should be enabled, false otherwise
	 */
	public void setWelcomeEnabled(String channel, boolean value) {
		welcomeEnabled.put(channel, value);
	}

	/**
	 * @param channel
	 *            - channel that we are setting the value for
	 * @param value
	 *            - true if confirmations should be on, false otherwise
	 */
	public void setConfirmationEnabled(String channel, boolean value) {
		confirmationReplies.put(channel, value);
	}

	/**
	 * @param target
	 *            - person who might have been seen
	 * @return the time and channel they were seen in, null otherwise
	 */
	public String getChatPostSeen(String target) {
		return chatPostSeen.get(target);
	}

	/**
	 * @param channel
	 *            - the channel the poll is in
	 * @param poll
	 *            - the Poll Object
	 */
	public void addPoll(String channel, io.github.weebobot.weebobot.util.PollUtil poll) {
		polls.put(channel, poll);
	}

	/**
	 * @param channel
	 *            - the channel the poll is in
	 */
	public void removePoll(String channel) {
		polls.remove(channel);
	}

	/**
	 * @param channel
	 *            - the channel the Poll might be in
	 * @return true if the channel has a Poll, false otherwise
	 */
	public boolean hasPoll(String channel) {
		return polls.containsKey(channel);
	}

	/**
	 * @param channel
	 *            - the channel the Poll is in
	 * @return the Poll Object
	 */
	public io.github.weebobot.weebobot.util.PollUtil getPoll(String channel) {
		return polls.get(channel);
	}

	/**
	 * @param channel
	 *            - the channel to get confirmation reply for
	 * @return true if the replies are enabled, false otherwise
	 */
	public boolean getConfirmationReplies(String channel) {
		return confirmationReplies.get(channel);
	}

	/**
	 * @param channel
	 *            - the channel to add the raffle to
	 * @param raffle
	 *            - the Raffle Object
	 */
	public void addRaffle(String channel, io.github.weebobot.weebobot.util.RaffleUtil raffle) {
		raffles.put(channel, raffle);
	}

	/**
	 * @param channel
	 *            - the channel to remove the raffle from
	 */
	public void removeRaffle(String channel) {
		raffles.remove(channel);
	}

	/**
	 * @param channel
	 *            - the channel to get the Raffle for
	 * @return the Raffle Object
	 */
	public io.github.weebobot.weebobot.util.RaffleUtil getRaffle(String channel) {
		return raffles.get(channel);
	}

	/**
	 * @param chanel
	 *            - the channel to set slow mode for
	 * @param slowMode
	 *            - true if slow mode is enabled, false otherwise
	 */
	public void setSlowMode(String chanel, boolean slowMode) {
		IRCBot.slowMode.put(chanel, slowMode);
	}

	/**
	 * @param channel
	 *            - the channel to get slow mode for
	 * @return true if the channel is in slow mode, false otherwise
	 */
	public boolean getSlowMode(String channel) {
		return slowMode.get(channel);
	}

	/**
	 * @param channel
	 *            - the channel to set slow mode for
	 * @param value
	 *            - true if sub mode is enabled, false otherwise
	 */
	public void setSubMode(String channel, boolean value) {
		subMode.put(channel, value);
	}

	/**
	 * @param channel
	 *            - the channel to get ub mode for
	 * @return true if the channel is in sub mode, false otherwise
	 */
	public boolean getSubMode(String channel) {
		return subMode.get(channel);
	}

	/**
	 * @param channel
	 *            - the channel to remove from the welcome enabled list
	 */
	public void removeWelcomeEnabled(String channel) {
		welcomeEnabled.remove(channel);
	}

	/**
	 * @param channel
	 *            - the channel to remove from the confirmation replies list
	 */
	public void removeConfirmationReplies(String channel) {
		confirmationReplies.remove(channel);
	}

	/**
	 * @param channel
	 *            - the channel to remove from the slow mode list
	 */
	public void removeSlowMode(String channel) {
		slowMode.remove(channel);
	}

	/**
	 * @param channel
	 *            - the channel to remove from the sub mode list
	 */
	public void removeSubMode(String channel) {
		subMode.remove(channel);
	}

	/**
	 * @param channel
	 *            - the channel to add a vote time out for
	 * @param voteTimeOut
	 *            - VoteTimeOut Object
	 */
	public void addVoteTimeOut(String channel,
			io.github.weebobot.weebobot.util.VoteTimeOutUtill voteTimeOut) {
		voteTimeOuts.put(channel, voteTimeOut);
	}

	/**
	 * @param channel
	 *            - channel to get the VoteTimeOut Object for
	 * @return VoteTimeOut Object
	 */
	public io.github.weebobot.weebobot.util.VoteTimeOutUtill getVoteTimeOut(
			String channel) {
		return voteTimeOuts.get(channel);
	}

	/**
	 * @param permit
	 *            - Permit Object
	 * @param user
	 *            - user to permit
	 */
	public void addPermit(DelayedPermitTask permit, String user) {
		ArrayList<DelayedPermitTask> p = permits.get(user);
		if (p == null) {
			p = new ArrayList<>();
		}
		p.add(permit);
		permits.put(user, p);
	}

	/**
	 * @param permit
	 *            - Permit Object to remove
	 * @param user
	 *            - user to remove the permit for
	 */
	public void removePermit(DelayedPermitTask permit, String user) {
		ArrayList<DelayedPermitTask> p = permits.get(user);
		if (p == null) {
			return;
		}
		p.remove(permit);
		if (p.size() > 0) {
			permits.put(user, p);
		} else {
			permits.remove(user);
		}
	}

	/**
	 * @param channel
	 *            - the channel the permit might be in
	 * @param sender
	 *            - the person who might be permitted
	 */
	private void removePermit(String channel, String sender) {
		ArrayList<DelayedPermitTask> ps = permits.get(sender);
		if (ps == null) {
			return;
		}
		for (DelayedPermitTask p : ps) {
			if (p.getChannel().equalsIgnoreCase(channel)) {
				ps.remove(p);
				break;
			}
		}
		if (ps.size() > 0) {
			permits.put(sender, ps);
		} else {
			permits.remove(sender);
		}
	}

	/**
	 * @param channel
	 *            - the channel to set teJoin for
	 * @param reJoin
	 *            - true if this is a re join, false otherwise
	 */
	public void setReJoin(String channel, boolean reJoin) {
		if (reJoin) {
			isReJoin.put(channel, reJoin);
			new DelayedReJoin(channel);
		}
	}

	/**
	 * @param channel
	 *            - the channel that might contain re join
	 */
	public void removeReJoin(String channel) {
		isReJoin.remove(channel);
	}

	/**
	 * @param channel
	 *            - the channel to add a welcome to
	 * @param user
	 *            - the user to add to the welcomes list
	 */
	public void addWelcome(String channel, String user) {
		ArrayList<String> p = welcomes.get(user);
		if (p == null) {
			p = new ArrayList<>();
		}
		p.add(channel);
		welcomes.put(user, p);
		new DelayedWelcomeTask(channel, user);
	}

	/**
	 * @param channel
	 *            - the channel to remove the welcome from
	 * @param user
	 *            -the user to remove from the welcomes list
	 */
	public void removeWelcome(String channel, String user) {
		ArrayList<String> ws = welcomes.get(user);
		if (ws == null) {
			return;
		}
		for (String s : ws) {
			if (s.equalsIgnoreCase(channel)) {
				ws.remove(s);
				break;
			}
		}
		if (ws.size() > 0) {
			welcomes.put(user, ws);
		} else {
			welcomes.remove(user);
		}
	}

	/**
	 * @param sender
	 *            - person who joined
	 * @param channel
	 *            - channel they joined
	 * @return true if they have joined this channel in the last 30 minutes,
	 *         false otherwise
	 */
	private boolean recentlyWelcomed(String sender, String channel) {
		ArrayList<String> p = welcomes.get(sender);
		if (p == null) {
			return false;
		}
		return p.contains(channel);
	}
}
