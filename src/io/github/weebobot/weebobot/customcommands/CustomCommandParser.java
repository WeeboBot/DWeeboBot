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

package io.github.weebobot.weebobot.customcommands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.weebobot.weebobot.database.Database;
import io.github.weebobot.weebobot.util.WLogger;

public class CustomCommandParser {
	private static final Logger logger = Logger.getLogger(CustomCommandParser.class+"");
	
	/**
	 * @param command - command without the leading !
	 * @param sender - person who sent the command
	 * @param channel - channel the command was sent in
	 * @param parameters - parameters passed with the command
	 * @return formatted if the command is valid, null otherwise
	 */
	public static String parse(String command, String sender, String channel, String[] parameters) {
		ResultSet rs = Database.getCustomCommands(channel.substring(1));
		try {
			while(rs.next()) {
				if(rs.getString(1).substring(1).equalsIgnoreCase(command)) {
					String reply = rs.getString(3);
					ArrayList<String> passed = new ArrayList<>();
					String[] params = rs.getString(2).split(" ");
					if(params[0].equalsIgnoreCase("")){
						return reply;
					}
					int i=0;
					while(i < parameters.length) {
						if(parameters[i].startsWith("\"")) {
							String temp=parameters[i].replace("\"", "") + " ";
							if(parameters[i].endsWith("\"")) {
								passed.add(parameters[i].replace("\"", ""));
								continue;
							}
							i++;
							boolean endQuote = true;
							while(!parameters[i].endsWith("\"")) {
								temp+=parameters[i] + " ";
								i++;
								if(i >= parameters.length) {
									endQuote = false;
								}
							}
							if(endQuote) {
								temp+=parameters[i].replace("\"", "");
							}
							i++;
							passed.add(temp);
							continue;
						}
						passed.add(parameters[i]);
						i++;
					}
					if(passed.size() == params.length) {
						for(i = 0;i < passed.size();i++) {
							reply = reply.replace(params[i], passed.get(i));
						}
						return reply;
					}
				}
			}
		} catch (SQLException e) {
			logger.log(Level.SEVERE, "There was an issue executing a custom command", e);
			WLogger.logError(e);
		}
		return null;
	}
}