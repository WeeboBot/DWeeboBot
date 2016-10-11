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

package io.github.weebobot.dweebobot.commands;

import io.github.weebobot.dweebobot.Main;
import io.github.weebobot.dweebobot.external.DiscordListener;
import sx.blah.discord.handle.obj.IGuild;

public class Setup extends Command {

	@Override
	public String execute(String channel, String sender, String... parameters) {
		if(parameters.length != 0 && !parameters[0].equalsIgnoreCase("continue")) {
			DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, Main.getBot().getChannelByID(channel).getGuild(), Main.getBot().getChannelByID(channel), "To begin with, we use a two step system to define a few options. Let's begin with timing out a user.");
			DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, Main.getBot().getChannelByID(channel).getGuild(), Main.getBot().getChannelByID(channel), "Users are timed out for excessive caps, symbols, emotes, long messages, links, and blacklisted words (spam).");
			DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, Main.getBot().getChannelByID(channel).getGuild(), Main.getBot().getChannelByID(channel), "We would like you to configure the amount of capital letters, symbols, and emotes (all of which default to 20), and paragraph length (defaults to 400 characters) allowed in a message.");
			DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, Main.getBot().getChannelByID(channel).getGuild(), Main.getBot().getChannelByID(channel), "To change this, please run !changeOption <caps, symbols, emotes, paragraph> <new value>. Note: If you make paragraph too short users may not be able to post proper sentences. Think of it like twitter messages.");
			DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, Main.getBot().getChannelByID(channel).getGuild(), Main.getBot().getChannelByID(channel), "If you would like to disable any spam protection simply set its value to -1, except for links which only take \"enable\" or \"disable\".");
			DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, Main.getBot().getChannelByID(channel).getGuild(), Main.getBot().getChannelByID(channel), "Now you need to set up how regulars are handled in your channel. Regulars are essentially people who have spent a certain ammount of time in your chat (which you define).");
			DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, Main.getBot().getChannelByID(channel).getGuild(), Main.getBot().getChannelByID(channel), "To become a regular a user must earn a certain number of points, which are earned by being in chat while the stream is live, 1 point for every 5 minutes.");
			DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, Main.getBot().getChannelByID(channel).getGuild(), Main.getBot().getChannelByID(channel), "Set how many points a user must earn to become a regular by typing \"!changeOption regular <ammount>\" without the quotes!");
			DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, Main.getBot().getChannelByID(channel).getGuild(), Main.getBot().getChannelByID(channel), "Once you are finished type \"!setup continue\" in chat!");
		} else {
			DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, Main.getBot().getChannelByID(channel).getGuild(), Main.getBot().getChannelByID(channel), "Next we are going to configure the welcome message for when new users enter your channel. By default it is off, but if you want to enable it you can type !changewelcome \"message\" (with the quotes).");
			DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, Main.getBot().getChannelByID(channel).getGuild(), Main.getBot().getChannelByID(channel), "If you want the users name to appear in the join message use %user%. This will cause the bot to replace that with the name of the person who is joining.");
			DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, Main.getBot().getChannelByID(channel).getGuild(), Main.getBot().getChannelByID(channel), "If you would like to disable this feature again type \"!changeWelcome none\" without the quotes! To temporarily disable it type !disableWelcome and to enable it type !enableWelcome (This only works if the message is something other than \"none\")!");
			DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, Main.getBot().getChannelByID(channel).getGuild(), Main.getBot().getChannelByID(channel), "Also, if you would like to use subscriber raffles or change the stream title and game, please go to http://dweebobot.com/login to authorize the bot!");
			DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, Main.getBot().getChannelByID(channel).getGuild(), Main.getBot().getChannelByID(channel), "Congrats, you're all done! Go take a nap!");
		}
		return null;
	}

	@Override
	public int getCommandLevel(IGuild guild) {
		return Main.MAX_USER_LEVEL-1;
	}

	@Override
	public String getCommandText() {
		return "setup";
	}

}
