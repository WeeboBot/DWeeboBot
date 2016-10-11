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
import sx.blah.discord.handle.obj.IGuild;

public class AddSpam extends Command {

	@Override
	public int getCommandLevel(IGuild guild) {
		return Database.getPermissionLevel(getCommandText(), guild);
	}

	@Override
	public String getCommandText() {
		return "addspam";
	}

	@Override
	public String execute(String channel, String sender, String... parameters) {
        String word = parameters[0];
        boolean emote = false;
        if(parameters.length == 2 && parameters[0].equalsIgnoreCase("emote")) {
            emote = true;
            word = parameters[1];
        }
		Database.addSpam(channel.substring(1), emote, word);
		return "Added %word% to the spam database".replace("%word%", word) + ((emote) ? " as an emote!" : "!");
	}

}
