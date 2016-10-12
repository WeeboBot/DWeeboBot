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

import io.github.weebobot.dweebobot.database.Database;
import io.github.weebobot.dweebobot.util.CommandsPage;
import sx.blah.discord.handle.obj.IGuild;

public class Help extends Command {

    @Override
    public int getCommandLevel(IGuild guild) {
        return Database.getPermissionLevel(getCommandText(), guild);
    }

    @Override
    public String getCommandText() {
        return null;
    }

    @Override
    public String execute(String channel, String sender, String...parameters) {
        if(CommandsPage.pageExists(channel.substring(1))) {
            return "You can find all of my commands at http://dweebobot.no-ip.info/commands and all of the commands for %s at http://weebobott.no-ip.info/commands/%s.html.".replace("%channel%", channel.substring(1));
        }
        return "You can find all of my commands at http://dweebobot.no-ip.info/commands.";
    }

}
