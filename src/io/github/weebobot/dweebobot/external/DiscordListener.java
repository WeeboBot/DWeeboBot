package io.github.weebobot.dweebobot.external;

import io.github.weebobot.dweebobot.Main;
import io.github.weebobot.dweebobot.commands.CommandParser;
import io.github.weebobot.dweebobot.customcommands.CustomCommandParser;
import io.github.weebobot.dweebobot.database.Database;
import io.github.weebobot.dweebobot.util.PointsRunnable;
import io.github.weebobot.dweebobot.util.TOptions;
import io.github.weebobot.dweebobot.util.WLogger;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.impl.obj.User;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.*;
import java.util.logging.Level;

/**
 * Created by James on 9/2/2016.
 */
public class DiscordListener {

    private final Logger logger = Logger.getLogger(DiscordListener.class);

    @EventSubscriber
    public void onReady(ReadyEvent event) {

    }

    /**
     * Decides how to welcome a user and then sends that message to the the
     * channel.
     *
     * Also starts points accumulation for that user in the channel
     */
    @EventSubscriber
    public void onJoin(UserJoinEvent event) {
        IUser u = event.getUser();
        IGuild g = event.getGuild();
        try {
            if (!Main.getDWeeboBot().getWelcomeDisabled(g)) {
                String msg = Database.getOption(g.getID(), TOptions.welcomeMessage.getOptionID())
                        .replace("%user%", u.getDisplayName(g));
                if (!msg.equalsIgnoreCase("none")) {
                    Main.getDWeeboBot().sendMessage(Database.getWelcomeChannel(g.getID()), msg);
                }
            }
        } catch (Exception e) {
            logger.log(Priority.ERROR,
                    "An error occurred while executing onJoin()", e);
            WLogger.logError(e);
        }
    }

    /**
     * Handles spam checking, last seen, commands, and custom
     * commands
     */
    @EventSubscriber
    public void onMessage(MessageReceivedEvent event) {
        IGuild g = event.getMessage().getGuild();
        IUser u = event.getMessage().getAuthor();
        IMessage m = event.getMessage();
        try {
            if (!Main.isDefaultMod(u.getID(), g.getID())) {
                Main.getDWeeboBot().checkSpam(m);
            }
            if (sender.equalsIgnoreCase(Main.getBotChannel().substring(1))) {
                return;
            }
            if (message.charAt(0) == '!') {
                String[] params = message.substring(message.indexOf(' ') + 1)
                        .split(" ");
                String command;
                try {
                    command = message.substring(1, message.indexOf(' '));
                } catch (StringIndexOutOfBoundsException e) {
                    command = message.substring(1, message.length());
                }
                if (command.equalsIgnoreCase(params[0].substring(1))) {
                    params = new String[0];
                }
                String reply = CommandParser.parse(command, sender, channel,
                        params);
                if (reply != null) {
                    sendMessage(channel, reply);
                }
                reply = CustomCommandParser.parse(command.toLowerCase(),
                        sender, channel, params);
                if (reply != null) {
                    sendMessage(channel, reply);
                }
            }
            autoReplyCheck(channel, message, sender);
            chatPostSeen.put(sender,
                    channel.substring(1) + "|" + new Date().toString());
        } catch (Exception e) {
            logger.log(Level.WARNING,
                    "An error was thrown while executing onMessage() in "
                            + channel, e);
            WLogger.logError(e);
        }
    }

    public static class ActionQueue implements Runnable{

        private static HashMap<ActionPriority, HashMap<ActionType, List<List<Object>>>> queue = new HashMap<>();
        private static final Logger logger = Logger.getLogger(DiscordListener.class);

        @Override
        public void run() {
            while(true) {
                try {
                    while (queue.containsKey(ActionPriority.IMMEDIATE)) {
                        processEdits();
                        processExits();
                    }
                    while (queue.containsKey(ActionPriority.HIGH) && !queue.containsKey(ActionPriority.IMMEDIATE)) {
                        processEdits();
                        processExits();
                    }
                    while (queue.containsKey(ActionPriority.MEDIUM) && !queue.containsKey(ActionPriority.HIGH) && !queue.containsKey(ActionPriority.IMMEDIATE)) {
                        processEdits();
                        processExits();
                    }
                    while (queue.containsKey(ActionPriority.LOW) && !queue.containsKey(ActionPriority.MEDIUM) && !queue.containsKey(ActionPriority.HIGH) && !queue.containsKey(ActionPriority.IMMEDIATE)) {
                        processEdits();
                        processExits();
                    }
                } catch (RateLimitException e) {
                    logger.log(Priority.WARN, "We are being rate limited! Slowing things down a bit...");
                } catch (DiscordException e) {
                    e.printStackTrace();
                } catch (MissingPermissionsException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * List<Object>[0] - guildID
         * List<Object>[1] - channelID
         * List<Object>[2] - messageID
         * List<Object>[3] - newContent
         */
        private void processEdits() throws RateLimitException, DiscordException, MissingPermissionsException {
            for (List<Object> o : queue.get(ActionPriority.IMMEDIATE).get(ActionType.MESSAGEEDIT)){
                Main.getBot().getGuildByID((String)o.get(0)).getChannelByID((String)o.get(1)).getMessageByID((String)o.get(2)).edit((String)o.get(3));
            }
        }

        private void processExits() {
            for (List<Object> o : queue.get(ActionPriority.IMMEDIATE).get(ActionType.MESSAGEEDIT)){
                Main.shutdownListeners();
            }
        }

        public static void addAction(ActionPriority priority, ActionType type, Object... parameters) {
            if(!queue.containsKey(priority)) {
                HashMap<ActionType, List<List<Object>>> tempSubMap = new HashMap<>();
                List<List<Object>> tempList = new ArrayList<>();
                List<Object> tempSubList = new ArrayList<>();
                for(Object o : parameters) {
                    tempSubList.add(o);
                }
                tempList.add(tempSubList);
                tempSubMap.put(type, tempList);
                queue.put(priority, tempSubMap);
                return;
            }
            HashMap<ActionType, List<List<Object>>> tempSubMap = queue.get(priority);
            if(!tempSubMap.containsKey(type)) {
                List<List<Object>> tempList = new ArrayList<>();
                List<Object> tempSubList = new ArrayList<>();
                for(Object o : parameters) {
                    tempSubList.add(o);
                }
                tempList.add(tempSubList);
                tempSubMap.put(type, tempList);
                queue.put(priority, tempSubMap);
                return;
            }
            List<List<Object>> tempList = queue.get(priority).get(type);
            List<Object> tempSubList = new ArrayList<>();
            for(Object o : parameters) {
                tempSubList.add(o);
            }
            tempList.add(tempSubList);
            tempSubMap.put(type, tempList);
            queue.put(priority, tempSubMap);
        }
    }

    public enum ActionPriority {
        IMMEDIATE, HIGH, MEDIUM, LOW
    }

    public enum ActionType {
        MESSAGEEDIT, EXIT
    }
}
