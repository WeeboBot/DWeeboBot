package io.github.weebobot.dweebobot.external;

import io.github.weebobot.dweebobot.Main;
import io.github.weebobot.dweebobot.commands.CommandParser;
import io.github.weebobot.dweebobot.customcommands.CustomCommandParser;
import io.github.weebobot.dweebobot.database.Database;
import io.github.weebobot.dweebobot.util.MessageLog;
import io.github.weebobot.dweebobot.util.TOptions;
import io.github.weebobot.dweebobot.util.WLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.*;

/**
 * Created by James on 9/2/2016.
 */
public class DiscordListener {

    private final Logger logger = LoggerFactory.getLogger(DiscordListener.class);

    @EventSubscriber
    public void onReady(ReadyEvent event) {
        logger.info("Logged in as: " + Main.getBot().getOurUser().getName());
        logger.info("Ready!");
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
            if(u.equals(Main.getBot().getOurUser())) {
                Main.joinChannel(g);
            }
            if (!Main.getDWeeboBot().getWelcomeDisabled(g)) {
                String[] welcomeInfo = Database.getWelcomeInfo(g.getID());
                welcomeInfo[1] = welcomeInfo[1].replace("%user%", u.getDisplayName(g));
                if (!welcomeInfo[1].equalsIgnoreCase("none")) {
                    ActionQueue.addAction(ActionPriority.HIGH, ActionType.MESSAGESEND, g.getID(), g.getChannelByID(welcomeInfo[0]), welcomeInfo[1]);
                    Database.addWelcomedUser(g.getID(), u.getID());
                }
            }
        } catch (Exception e) {
            logger.error("An error occurred while executing onJoin()", e);
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
        String c = m.getContent();
        try {
            if (!isOwner(u, g)) {
                Main.getDWeeboBot().checkSpam(m);
            }
            if (u.equals(Main.getBot().getOurUser()) || u.isBot()) {
                return;
            }
            if (c.charAt(0) == '!') {
                String[] params = c.substring(c.indexOf(' ') + 1)
                        .split(" ");
                String command;
                try {
                    command = c.substring(1, c.indexOf(' '));
                } catch (StringIndexOutOfBoundsException e) {
                    command = c.substring(1, c.length());
                }
                if (command.equalsIgnoreCase(params[0].substring(1))) {
                    params = new String[0];
                }
                String reply = CommandParser.parse(m, command, params);
                if (reply != null) {
                    ActionQueue.addAction(ActionPriority.MEDIUM, ActionType.MESSAGESEND, g.getID(), m.getChannel(), reply);
                }
                reply = CustomCommandParser.parse(m, command, params);
                if (reply != null) {
                    ActionQueue.addAction(ActionPriority.MEDIUM, ActionType.MESSAGESEND, g.getID(), m.getChannel(), reply);
                }
            }
            Main.getDWeeboBot().autoReplyCheck(m);
        } catch (Exception e) {
            logger.warn("An error was thrown while executing onMessage() in Guild: "
                            + g.getID() + " Channel: " + m.getChannel().getID(), e);
            WLogger.logError(e);
        }
    }

    public static boolean isOwner(IUser user, IGuild guild) {
        return user.getID() == guild.getOwnerID();
    }

    public static class ActionQueue implements Runnable{

        private static HashMap<ActionPriority, HashMap<ActionType, List<List<Object>>>> queue = new HashMap<>();
        private static ArrayList<DelayedAction> delayedActions = new ArrayList<>();
        private static final Logger logger = LoggerFactory.getLogger(DiscordListener.class);
        private static int queueSize;

        public static int getQueueSize() {
            return queueSize + DelayedAction.queueSize;
        }

        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {
            while(true) {
                try {
                    while (queue.containsKey(ActionPriority.IMMEDIATE)) {
                        processEdits(ActionPriority.IMMEDIATE);
                        processDeletes(ActionPriority.IMMEDIATE);
                        processSends(ActionPriority.IMMEDIATE);
                        processExits(ActionPriority.IMMEDIATE);
                    }
                    while (queue.containsKey(ActionPriority.HIGH) && !queue.containsKey(ActionPriority.IMMEDIATE)) {
                        processEdits(ActionPriority.HIGH);
                        processDeletes(ActionPriority.HIGH);
                        processSends(ActionPriority.HIGH);
                        processExits(ActionPriority.HIGH);
                    }
                    while (queue.containsKey(ActionPriority.MEDIUM) && !queue.containsKey(ActionPriority.HIGH) && !queue.containsKey(ActionPriority.IMMEDIATE)) {
                        processEdits(ActionPriority.MEDIUM);
                        processDeletes(ActionPriority.MEDIUM);
                        processSends(ActionPriority.MEDIUM);
                        processExits(ActionPriority.MEDIUM);
                    }
                    while (queue.containsKey(ActionPriority.LOW) && !queue.containsKey(ActionPriority.MEDIUM) && !queue.containsKey(ActionPriority.HIGH) && !queue.containsKey(ActionPriority.IMMEDIATE)) {
                        processEdits(ActionPriority.LOW);
                        processDeletes(ActionPriority.LOW);
                        processSends(ActionPriority.LOW);
                        processExits(ActionPriority.LOW);
                    }
                } catch (RateLimitException e) {
                    logger.warn("We are being rate limited! Slowing things down a bit...");
                } catch (DiscordException | MissingPermissionsException e) {
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
        private void processEdits(ActionPriority priority) throws RateLimitException, DiscordException, MissingPermissionsException {
            for (List<Object> o : queue.get(priority).get(ActionType.MESSAGEEDIT)){
                Main.getBot().getGuildByID((String)o.get(0)).getChannelByID((String)o.get(1)).getMessageByID((String)o.get(2)).edit((String)o.get(3));
                queueSize--;
            }
        }

        /**
         * List<Object>[0] - guildID
         * List<Object>[1] - channelID
         * List<Object>[2] - messageID
         */
        private void processDeletes(ActionPriority priority) throws RateLimitException, DiscordException, MissingPermissionsException {
            for (List<Object> o : queue.get(priority).get(ActionType.MESSAGEDELETE)){
                Main.getBot().getGuildByID((String)o.get(0)).getChannelByID((String)o.get(1)).getMessageByID((String)o.get(2)).delete();
                queueSize--;
            }
        }

        /**
         * List<Object>[0] - guildID
         * List<Object>[1] - channelID
         * List<Object>[2] - message
         */
        private void processSends(ActionPriority priority) throws RateLimitException, DiscordException, MissingPermissionsException {
            for (List<Object> o : queue.get(priority).get(ActionType.MESSAGEEDIT)){
                MessageLog.addMessage(Main.getBot().getGuildByID((String)o.get(0)).getChannelByID((String)o.get(1)).sendMessage((String)o.get(3)));
                queueSize--;
            }
        }

        private void processExits(ActionPriority priority) {
            if (queue.get(priority).get(ActionType.EXIT) != null && queue.get(priority).get(ActionType.EXIT).size() > 0){
                Main.shutdown();
                queueSize--;
            }
        }

        public static void addAction(ActionPriority priority, ActionType type, Object... parameters) {
            if(!queue.containsKey(priority)) {
                HashMap<ActionType, List<List<Object>>> tempSubMap = new HashMap<>();
                List<List<Object>> tempList = new ArrayList<>();
                List<Object> tempSubList = new ArrayList<>();
                Collections.addAll(tempSubList, parameters);
                tempList.add(tempSubList);
                tempSubMap.put(type, tempList);
                queue.put(priority, tempSubMap);
                return;
            }
            HashMap<ActionType, List<List<Object>>> tempSubMap = queue.get(priority);
            if(!tempSubMap.containsKey(type)) {
                List<List<Object>> tempList = new ArrayList<>();
                List<Object> tempSubList = new ArrayList<>();
                Collections.addAll(tempSubList, parameters);
                tempList.add(tempSubList);
                tempSubMap.put(type, tempList);
                queue.put(priority, tempSubMap);
                return;
            }
            List<List<Object>> tempList = queue.get(priority).get(type);
            List<Object> tempSubList = new ArrayList<>();
            Collections.addAll(tempSubList, parameters);
            tempList.add(tempSubList);
            tempSubMap.put(type, tempList);
            queue.put(priority, tempSubMap);
            queueSize++;
        }

        public static void addDelayedAction(ActionPriority priority, ActionType type, String delay, Object... parameters) {
            DelayedAction.queueSize++;
            long delayAsLong;
            if(delay.contains("h")) {
                delayAsLong = Integer.valueOf(delay.replace("h", ""))*60L*60L*1000L;
            } else if(delay.contains("m")) {
                delayAsLong = Integer.valueOf(delay.replace("m", ""))*60L*1000L;
            } else if(delay.contains("s")) {
                delayAsLong = Integer.valueOf(delay.replace("s", ""))*1000L;
            } else {
                delayAsLong = (long) Integer.valueOf(delay.replace("s", ""));
            }
            delayedActions.add(new DelayedAction(priority, type, delayAsLong, parameters));
        }

        private static class DelayedAction extends TimerTask {

            private ActionPriority priority;
            private ActionType type;
            private Object[] parameters;
            private static final Timer timer = new Timer();
            protected static int queueSize;

            public DelayedAction(ActionPriority priority, ActionType type, long delay, Object... parameters) {
                this.priority = priority;
                this.type = type;
                this.parameters = parameters;
                timer.schedule(this, delay);
            }

            @Override
            public void run() {
                ActionQueue.addAction(priority, type, parameters);
                delayedActions.remove(this);
                queueSize--;
            }
        }
    }

    public enum ActionPriority {
        IMMEDIATE, HIGH, MEDIUM, LOW
    }

    public enum ActionType {
        MESSAGESEND, MESSAGEEDIT, MESSAGEDELETE, EXIT
    }
}
