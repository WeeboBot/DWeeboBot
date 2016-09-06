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

import java.io.File;
import java.util.ArrayList;

import io.github.weebobot.dweebobot.Main;
import io.github.weebobot.dweebobot.util.CLevel;
import io.github.weebobot.dweebobot.util.TFileWriter;

public class Shutdown extends Command {

	@Override
	public CLevel getCommandLevel() {
		return CLevel.Mod;
	}
	
	@Override
	public String getCommandText() {
		return "shutdown";
	}
	
	@Override
	public String execute(String channel, String sender, String...parameters) {
		if(channel.equalsIgnoreCase(Main.getBotChannel())) {
			Main.shutdownListeners();
			TFileWriter.overWriteFile(new File("connectedChannels.txt"), channels);
			while(Main.getBot() > 0);
			System.exit(0);
		}
		return null;
	}

}
