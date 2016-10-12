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

package io.github.weebobot.dweebobot;

import io.github.weebobot.dweebobot.external.DiscordListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author James Wolff
 */

public class DWeeboBot {

    private static ArrayList<IGuild> welcomeDisabled;
    private static final Logger logger = LoggerFactory.getLogger(DWeeboBot.class);

    /**
     * Creates a new instance of DWeeboBot for the specified channel
     */
    public DWeeboBot() {
        initVariables();
    }

    /**
     * Initializes all of our variables
     */
    private void initVariables() {
        welcomeDisabled = new ArrayList<>();
    }

    /**
     * Sends message when the bot join's a guild for the first time.
     */
    public void onFirstJoin(IGuild guild, IChannel channel) {
        DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, guild.getID(), channel.getID(), "Hello, this appears to be the first time you have invited me to join your channel. We just have a few preliminary matters to attend to. To get started type !setup");
    }

    /**
     *
     * @param guild - guild that we are setting the value for
     */
    public void setWelcomeEnabled(IGuild guild) {
        welcomeDisabled.remove(guild);
    }
    /**
     * @param guild
     *            - guild that we are setting the value for
     */
    public void setWelcomeDisabled(IGuild guild) {
        welcomeDisabled.add(guild);
    }

    /**
     * @param guild - guild that we are checking the welcome status of
     */
    public boolean getWelcomeDisabled(IGuild guild) {
        return welcomeDisabled.contains(guild);
    }
}
