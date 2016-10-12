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

package io.github.weebobot.dweebobot.util;

import java.util.ArrayList;
import java.util.Random;

import io.github.weebobot.dweebobot.Main;
import io.github.weebobot.dweebobot.external.DiscordListener;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

public class PollUtil {
    /**
     * ringazin, may he forever be known as the one who initially tried to vote
     * for an option out of the bounds of the choices
     */
    private ArrayList<String> ringazinUsers;
    private ArrayList<ArrayList<String>> voting;

    private IGuild guild;
    private IChannel channel;
    private String question;
    private String[] answers;
    int answerCount;
    private int length;

    /**
     * @param c - channel the poll is in
     * @param a - possible answers
     * @param l - the length of time the poll should last
     */
    public PollUtil(String g, String c, String q, String[] a, int l) {
        guild = Main.getBot().getGuildByID(g);
        channel = Main.getBot().getChannelByID(c);
        question = q;
        answers = a;
        answerCount = answers.length;
        length = l;
    }

    /**
     * Performs all of the setup for the poll
     * @return this instance of Poll
     */
    public PollUtil start() {
        ringazinUsers = new ArrayList<>();
        voting = new ArrayList<>();
        DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, guild.getID(), channel.getID(), question);
        for (int i = 0; i < answers.length; i++) {
            DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, guild.getID(), channel.getID(), (i + 1) + ": " + answers[i]);
            voting.add(new ArrayList<String>());
            voting.get(i).add(answers[i]);
        }
        DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, guild.getID(), channel.getID(), "Please input your choice by typing !vote {vote number}.");
        for (int i = 0; i < answers.length; i++) {
            voting.add(new ArrayList<String>());
            voting.get(i).add(answers[i]);
        }
        DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, guild.getID(), channel.getID(), "You have %length% seconds to vote.".replace("%length%", length
                        + ""));
        new DelayedVoteTask(length * 1000 + ((answers.length + 3) * 2000), this);
        return this;
    }

    /**
     * Counts the votes and chooses a winner
     */
    public void count() {
        int chosen = 0;
        for (int i = 0; i < voting.size(); i++) {
            if (voting.get(i).size() > voting.get(chosen).size()) {
                chosen = i;
            }
        }

        DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, guild.getID(), channel.getID(), "The community chose %choice%".replace("%choice%",
                        voting.get(chosen).get(0)));
        Main.getDWeeboBot().removePoll(channel.getID());
    }

    /**
     * @param sender - person who voted
     * @param v - answer choice
     */
    public void vote(String sender, String v) {
        boolean canVote = true;
        for (int i = 0; i < voting.size(); i++) {
            if (voting.get(i).contains(sender)) {
                if (Main.getDWeeboBot().getConfirmationReplies(channel.getID())) {
                    DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, guild.getID(), channel.getID(), "I am sorry %sender% you cannot vote more than once."
                                    .replace("%sender%", sender));
                }
                canVote = false;
            }
        }
        if (ringazinUsers.contains(sender)) {
            if (Main.getDWeeboBot().getConfirmationReplies(channel.getID())) {
                DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, guild.getID(), channel.getID(), "I am sorry %sender% you cannot vote more than once."
                                .replace("%sender%", sender));
            }
            canVote = false;
        }
        if (canVote) {
            int vote;
            if (v.equalsIgnoreCase("random")) {
                vote = new Random().nextInt();
            } else {
                vote = Integer.valueOf(v);
            }
            if (vote < answerCount + 1) {
                voting.get(vote - 1).add(sender);
            } else {
                if (Main.getDWeeboBot().getConfirmationReplies(channel.getID())) {
                    DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, guild.getID(), channel.getID(), "%sender% tried to break me, may hell forever reign upon him! (You cannot participate in this vote.)"
                                            .replace("%sender%", sender));
                }
                ringazinUsers.add(sender);
                return;
            }
            if (Main.getDWeeboBot().getConfirmationReplies(channel.getID())) {
                DiscordListener.ActionQueue.addAction(DiscordListener.ActionPriority.MEDIUM, DiscordListener.ActionType.MESSAGESEND, guild.getID(), channel.getID(), String.format("%s has voted for %s", sender, voting
                                .get(vote - 1).get(0)));
            }
        }
    }
}
