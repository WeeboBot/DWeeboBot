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
import net.swisstech.bitly.builder.v3.ShortenRequest;
import net.swisstech.bitly.model.Response;
import net.swisstech.bitly.model.v3.ShortenResponse;
import sx.blah.discord.handle.obj.IGuild;

public class Shorten extends Command {

    private static String API_KEY;

    @Override
    public int getCommandLevel(IGuild guild) {
        return Database.getPermissionLevel(getCommandText(), guild);
    }

    @Override
    public String getCommandText() {
        return "shorten";
    }

    @Override
    public String execute(String channel, String sender, String... parameters) {
        String url = parameters[0];
        Response<ShortenResponse> respShort = new ShortenRequest(API_KEY)
                .setLongUrl(url)
                .call();

        if (respShort.status_txt.equalsIgnoreCase("ok")) {
            return respShort.data.url;
        }
        return "%url% is an invalid url! Make sure you include http(s)://.".replace("%url%", url);
    }

    public static void setApiKey(String key) {
        API_KEY = key;
    }

}
