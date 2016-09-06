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

import io.github.weebobot.dweebobot.util.CLevel;
import sx.blah.discord.handle.obj.IMessage;

public abstract class Command {

	/**
	 * @return the level that the user must be to perform the command
	 */
	public abstract CLevel getCommandLevel();

	/**
	 * @return the command without the leading ! or parameters
	 */
	public abstract String getCommandText();

	/**
	 * @param channel - channel the command was sent in
	 * @param sender - user who sent the command
	 * @param parameters - parameters sent with the command
	 * @return a formatted message to send to the channel or null if no message is required
	 */
	public abstract String execute(String channel, String sender, String... parameters);

	/**
	 * @param message - IMessage object
	 * @param parameters - parameters sent with the command
	 * @return a formatted message to send to the channel or null if no message is required
	 */
	public String execute(IMessage message, String... parameters) {
		return execute(message.getChannel().getID(), message.getAuthor().getID(), parameters);
	}

}
