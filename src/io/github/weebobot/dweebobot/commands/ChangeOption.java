/*      It's a Twitch bot, because we can.
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

import io.github.weebobot.dweebobot.Main;
import io.github.weebobot.dweebobot.database.Database;
import io.github.weebobot.dweebobot.util.GOptions;
import sx.blah.discord.handle.obj.IChannel;
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
        if (parameters[0].equalsIgnoreCase("welcomechannel")) {
            IChannel welcomeChannel;
            try {
                welcomeChannel = Main.getBot().getChannelByID(channel).getGuild().getChannelsByName(parameters[1]).get(0);
            } catch (NullPointerException e) {
                welcomeChannel = null;
            }
            if (welcomeChannel != null) {
                Database.setOption(Main.getBot().getChannelByID(channel).getGuild().getID(), GOptions.welcomeChannel.getOptionID(), welcomeChannel.getID());
                return "You have changed the Welcome Channel to %option%.".replace("%option%", parameters[1]);
            }
            return "The channel %option% does not exist!".replace("%option%", parameters[1]);
        } else if (parameters[0].equalsIgnoreCase("deleteWelcome")) {
            int delay;
            try {
                delay = Integer.valueOf(parameters[1]);
            } catch (NumberFormatException e) {
                return "The value for the option \"deleteWelcome\" should be a delay in seconds or 0 if you don't want messages to be automatically deleted.";
            }
            if(delay < 0) {
                delay = 0;
            }
            Database.setOption(Main.getBot().getChannelByID(channel).getGuild().getID(), GOptions.deleteWelcome.getOptionID(), String.valueOf(delay));
            if(delay == 0) {
                return "Welcome messages will not be automatically deleted!";
            }
            return "Welcome messages will be deleted after a delay of %delay% seconds!".replace("%delay%", String.valueOf(delay));
        }
        return "I am sorry, but you have tried to change a type of value that is not supported. Valid option(s) are \"welcomechannel\" \"deleteWelcome\"";
    }
}


