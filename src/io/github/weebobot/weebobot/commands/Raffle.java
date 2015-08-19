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

import io.github.weebobot.weebobot.Main;
import io.github.weebobot.weebobot.util.CLevel;
import io.github.weebobot.weebobot.util.ULevel;

public class Raffle extends Command {

	@Override
	public CLevel getCommandLevel() {
		return CLevel.Mod;
	}
	
	@Override
	public String getCommandText() {
		return "raffle";
	}
	
	@Override
	public String execute(String channel, String sender, String... parameters) {
		if(Main.getBot().getRaffle(channel) != null) {
			return "There is already a raffle in progress!";
		}
		ULevel level=ULevel.getTypeFromString(parameters[0]);
		if(level == null) {
			return "Improper raffle type! Valid choices are \"everyone\", \"followers\" \"subscribers\"";
		}
		Main.getBot().addRaffle(channel, new io.github.weebobot.weebobot.util.RaffleUtil(channel, level));
		if(level == ULevel.Normal) {
			return "Raffle started for everyone! Type !enter to enter!";
		}
		return "Raffle started for %level%s! Type !enter to enter!".replace("%level%", level.toString());
	}
}
