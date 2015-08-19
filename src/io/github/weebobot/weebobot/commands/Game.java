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

package io.github.weebobot.weebobot.commands;

import io.github.weebobot.weebobot.twitch.TwitchUtilities;
import io.github.weebobot.weebobot.util.CLevel;

public class Game extends Command {

	@Override
	public CLevel getCommandLevel() {
		return CLevel.Mod;
	}
	
	@Override
	public String getCommandText() {
		return "game";
	}
	
	@Override
	public String execute(String channel, String sender, String...parameters) {
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < parameters.length - 1; i++) {
            	sb.append(parameters[i] + " ");
            }
            sb.append(parameters[parameters.length-1]);
            
		if (TwitchUtilities.updateGame(channel.substring(1),
				sb.toString())) {
			return "Successfully changed the stream game to \""
							+ sb.toString() + "\"!";
		} else {
			return "I am not authorized to do that visit http://weebobot.no-ip.info/login to allow me to do this and so much more!";
		}
	}

}
