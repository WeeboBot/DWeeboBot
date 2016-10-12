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
import io.github.weebobot.dweebobot.external.DiscordListener;
import sx.blah.discord.handle.obj.IGuild;

public class Setup extends Command {

    @Override
    public String execute(String channel, String sender, String... parameters) {
        DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, Main.getBot().getChannelByID(channel).getGuild().getID(), channel, "The first thing we need to do id configure the welcome message for when new users enter your guild. By default it is off, but if you want to enable it you can type !changewelcome \"message\" (with the quotes). If you want the users name to appear in the join message use %user%. This will cause the bot to replace that with the name of the person who is joining.\nIf you would like to disable this feature again type \"!changeWelcome none\" without the quotes! To temporarily disable it type !disableWelcome and to reenable it type !enableWelcome (This only works if the message is something other than \"none\")! To set the channel that users are welcomed in type !changeoption welcomechannel \"channelName\", a random channel was chosen when the bot first joined, you can test your welcome channel and message by typing \"!testwelcome\"!");
        DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, Main.getBot().getChannelByID(channel).getGuild().getID(), channel, "You don't truly need to, but if you don't like the default permission scheme (which you can see at http://weebo.jameswolff.me/dweebobot/commands) you can change those values using the \"!editcommand\" command, for help with any command type \"!help command\"!");
        return null;
    }

    @Override
    public int getCommandLevel(IGuild guild) {
        return Main.MAX_USER_LEVEL-1;
    }

    @Override
    public String getCommandText() {
        return "setup";
    }

}