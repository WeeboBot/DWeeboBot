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

public class AddAutoReply extends Command {

	@Override
	public CLevel getCommandLevel() {
		return CLevel.Mod;
	}
	
	@Override
	public String getCommandText() {
		return "addautoreply";
	}
	
	@Override
	public String execute(String channel, String sender, String... parameters){
		StringBuilder keywords = new StringBuilder();
		if(parameters.length > 1){
			for (int i = 0; i < parameters.length - 1; i++) {
				keywords.append(parameters[i] + ",");
			}
			String reply = parameters[parameters.length - 1];
			Database.addAutoReply(channel.substring(1), keywords.toString(), reply);
			return String.format("Added auto reply: \"%s\"! Which will be said when all of the following key words are said in %s: %s", reply, channel.substring(1), keywords.toString());
		}else{
			return "Insufficient paramaters. Format as !addautoreply keyword reply To add multiple keywords just write them infront of reply ex. !addautoreply keyword keyword keyword reply "
					+ "To add a key phrase write in quotes !addautoreply \"This is a key phrase\" reply"; 
		}
	}
	
}