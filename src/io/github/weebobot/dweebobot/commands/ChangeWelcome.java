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

public class ChangeWelcome extends Command {

	@Override
	public int getCommandLevel(IGuild guild) {
		return Database.getPermissionLevel(getCommandText(), guild);
	}
	
	@Override
	public String getCommandText() {
		return "changewelcome";
	}
	
	@Override
	public String execute(String channel, String sender, String... parameters) {
		if(parameters.length == 0) {
            return "Usage: !changewelcome \"Your new welcome message\"";
        }
        StringBuilder sb = new StringBuilder();
        for (String s : parameters) {
            sb.append(s);
        }
        String message = sb.toString();
		Database.setOption(channel.substring(1), TOptions.welcomeMessage.getOptionID(), message);
		if(!message.equalsIgnoreCase("none")) {
			return "The welcome message has been changed to: %message%".replace("%message%", message);
		}
		return "Welcome messages have been DISABLED! You can re-enable them by using !changewelcome at a later time";
	}

}
