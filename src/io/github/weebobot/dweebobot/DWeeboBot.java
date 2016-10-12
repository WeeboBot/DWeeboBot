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

import io.github.weebobot.dweebobot.database.Database;
import io.github.weebobot.dweebobot.external.DiscordListener;
import io.github.weebobot.dweebobot.util.PollUtil;
import io.github.weebobot.dweebobot.util.WLogger;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JewsOfHazard, Donald10101, And Angablade.
 */

public class DWeeboBot {

    private static ArrayList<IGuild> welcomeDisabled;
    private static HashMap<String, Boolean> confirmationReplies;
    private static HashMap<String, PollUtil> polls;
    private static final Logger logger = Logger.getLogger(DWeeboBot.class + "");

    /**
     * Creates a new instance of DWeeboBot for the specified channel
     */
    public DWeeboBot() {
        initVariables();
    }

    /**
     * initializes all of our HashMaps
     */
    private void initVariables() {
        welcomeDisabled = new ArrayList<>();
        confirmationReplies = new HashMap<>();
        polls = new HashMap<>();
    }

    /**
     * Sends message when the bot join's a channel for the first time.
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

    /**
     * @param channel
     *            - channel that we are setting the value for
     * @param value
     *            - true if confirmations should be on, false otherwise
     */
    public void setConfirmationEnabled(String channel, boolean value) {
        confirmationReplies.put(channel, value);
    }

    /**
     * @param channel
     *            - the channel the poll is in
     * @param poll
     *            - the Poll Object
     */
    public void addPoll(String channel, io.github.weebobot.dweebobot.util.PollUtil poll) {
        polls.put(channel, poll);
    }

    /**
     * @param channel
     *            - the channel the poll is in
     */
    public void removePoll(String channel) {
        polls.remove(channel);
    }

    /**
     * @param channel
     *            - the channel the Poll might be in
     * @return true if the channel has a Poll, false otherwise
     */
    public boolean hasPoll(String channel) {
        return polls.containsKey(channel);
    }

    /**
     * @param channel
     *            - the channel the Poll is in
     * @return the Poll Object
     */
    public PollUtil getPoll(String channel) {
        return polls.get(channel);
    }

    /**
     * @param channel
     *            - the channel to get confirmation reply for
     * @return true if the replies are enabled, false otherwise
     */
    public boolean getConfirmationReplies(String channel) {
        return confirmationReplies.get(channel);
    }

    /**
     * @param channel
     *            - the channel to remove from the welcome enabled list
     */
    public void removeWelcomeDisabled(String channel) {
        welcomeDisabled.remove(channel);
    }

    /**
     * @param channel
     *            - the channel to remove from the confirmation replies list
     */
    public void removeConfirmationReplies(String channel) {
        confirmationReplies.remove(channel);
    }
}
