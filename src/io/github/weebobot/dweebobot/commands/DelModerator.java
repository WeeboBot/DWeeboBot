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

import io.github.weebobot.dweebobot.database.Database;
import io.github.weebobot.dweebobot.util.CLevel;

public class DelModerator extends Command {

	@Override
	public CLevel getCommandLevel() {
		return CLevel.Owner;
	}

	@Override
	public String getCommandText() {
		return "delmoderator";
	}

	@Override
	public String execute(String channel, String sender, String... parameters) {
		if(Database.delModerator(parameters[0].toLowerCase(), channel.substring(1))) {
			return String.format("Successfully removed %s from the moderator list in %s", parameters[0], channel);
		}
		return String.format("Unable to remove %s from the moderator list in %s! Please try again.", parameters[0], channel);
	}

}
