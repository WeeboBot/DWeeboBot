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
import io.github.weebobot.dweebobot.util.WLogger;
import sx.blah.discord.handle.obj.IGuild;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DelCommand extends Command {

    @Override
    public int getCommandLevel(IGuild guild) {
        return Database.getPermissionLevel(getCommandText(), guild);
    }

    @Override
    public String getCommandText() {
        return "delcom";
    }

    @Override
    public String execute(String channel, String sender, String... parameters) {
        if(!parameters[0].startsWith("!")) {
            parameters[0] = "!" + parameters[0];
        }
        ResultSet c = Database.getCommand(Main.getBot().getChannelByID(channel).getGuild().getID(), parameters[0]);
        try {
            if(c != null && c.getBoolean("isDefault")) {
                return "You can not delete default commands";
            }
        } catch (SQLException e) {
            WLogger.logError(e);
        }
        Database.delCommand(Main.getBot().getChannelByID(channel).getGuild().getID(), parameters[0]);
        return "Removed %command% from the database.".replace("%command%", parameters[0]);
    }

}
