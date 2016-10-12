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
import sx.blah.discord.handle.obj.IGuild;

public class Shutdown extends Command {

    @Override
    public int getCommandLevel(IGuild guild) {
        return 9999;
    }

    @Override
    public String getCommandText() {
        return "shutdown";
    }

    @Override
    public String execute(String channel, String sender, String... parameters) {
        return null;
    }

    @Override
    public String execute(int userLevel, String...parameters) {
        if(userLevel == Main.MAX_USER_LEVEL) {
            Main.shutdown();
        }
        return null;
    }

}
