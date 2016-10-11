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
import io.github.weebobot.dweebobot.util.TOptions;
import sx.blah.discord.handle.obj.IGuild;

public class ChangeOption extends Command {

	@Override
	public int getCommandLevel(IGuild guild) {
		return Database.getPermissionLevel(getCommandText(), guild);
	}
	
	@Override
	public String getCommandText() {
		return "changeoption";
	}
	
	@Override
	public String execute(String channel, String sender, String...parameters){
		try {
			if (parameters[0].equalsIgnoreCase("paragraph")) {
				Database.setOption(channel.substring(1), TOptions.paragraphLength.getOptionID(), parameters[1]);
				return "You have changed the paragraph length to %option%.".replace("%option%", parameters[1]);
			} else if (parameters[0].equalsIgnoreCase("emotes")) {
				Database.setOption(channel.substring(1), TOptions.numEmotes.getOptionID(), parameters[1]);
				return "You have changed the emote cap to %option%.".replace("%option%", parameters[1]);
			} else if (parameters[0].equalsIgnoreCase("symbols")) {
				Database.setOption(channel.substring(1), TOptions.numSymbols.getOptionID(), parameters[1]);
				return "You have changed the symbol cap to %option%.".replace("%option%", parameters[1]);
			} else if (parameters[0].equalsIgnoreCase("caps")) {
				Database.setOption(channel.substring(1), TOptions.numCaps.getOptionID(), parameters[1]);
				return "You have changed the capitals cap to %option%.".replace("%option%", parameters[1]);
			} else if (parameters[0].equalsIgnoreCase("regulars")) {
				Database.setOption(channel.substring(1), TOptions.regular.getOptionID(), parameters[1]);
				return "You have changed the time for regulars to %option%.".replace("%option%", parameters[1]);
			} else if (parameters[0].equalsIgnoreCase("links")) {
				if (parameters[1].toLowerCase().equalsIgnoreCase("enable")) {
					Database.setOption(channel.substring(1), TOptions.link.getOptionID(), "0");
					return "Enabled link protection!";
				} else if (parameters[1].toLowerCase().equalsIgnoreCase("disable")) {
					Database.setOption(channel.substring(1), TOptions.link.getOptionID(), "-1");
					return "Disabled link protection!";
				} else {
					return "Valid values for links are: Enable and Disable";
				}
			}	
		} catch (NumberFormatException e) {
			return "You must pass a number for the value of %option%".replace("%option%", parameters[0]);
		}
		return "I am sorry, but you have tried to change a type of value that is not supported. Valid options are \"symbols\", \"emotes\", \"paragraph\", \"caps\", \"links\", or \"regulars\"";
	}
}


