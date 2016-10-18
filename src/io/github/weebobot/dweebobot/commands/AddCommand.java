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

public class AddCommand extends Command {

    @Override
    public int getCommandLevel(IGuild guild) {
        return Database.getPermissionLevel(getCommandText(), guild);
    }

    @Override
    public String getCommandText() {
        return "addcom";
    }

    @Override
    public String execute(String channel, String sender, String... parameters) {
        StringBuilder params = new StringBuilder();
        for(int i = 2;i < parameters.length;i++) {
            params.append(parameters[i] + " ");
        }
        if(!parameters[0].startsWith("!")) {
            parameters[0] = "!" + parameters[0];
        }
        try {
            if(Database.getCommand(Main.getBot().getChannelByID(channel).getGuild().getID(), parameters[0]).next()) {
                return "The command %c% already exists".replace("%c%", parameters[0]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            int level = Integer.valueOf(parameters[2]);
            Database.addCommand(Main.getBot().getChannelByID(channel).getGuild().getID(), parameters[0], params.toString(), parameters[1], level);
        } catch (NumberFormatException e) {
            Database.addCommand(Main.getBot().getChannelByID(channel).getGuild().getID(), parameters[0], params.toString(), parameters[1], 0);
        }
        return "Added %command% to the database.".replace("%command%", parameters[0]);
    }
}
