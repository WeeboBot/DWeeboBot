package io.github.weebobot.dweebobot.external;

import io.github.weebobot.dweebobot.Main;
import io.github.weebobot.dweebobot.commands.CommandParser;
import io.github.weebobot.dweebobot.customcommands.CustomCommandParser;
import io.github.weebobot.dweebobot.database.Database;
import io.github.weebobot.dweebobot.util.MessageLog;
import io.github.weebobot.dweebobot.util.GOptions;
import io.github.weebobot.dweebobot.util.WLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.GuildTransferOwnershipEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.UserJoinEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by James on 9/2/2016.
 */
public class DiscordListener {

    private static final Logger logger = LoggerFactory.getLogger(DiscordListener.class);

    public static void init() {
        new Thread(new ActionQueue()).start();
    }

    /**
     * Updates permission values when ownership of a guild is transferred to a new user.
     */
    @EventSubscriber
    public void onOwnershipTransfer(GuildTransferOwnershipEvent event) {
        Database.setUserPermissionLevel(event.getOldOwner().getID(), event.getGuild().getID(), 0);
        Database.setUserPermissionLevel(event.getNewOwner().getID(), event.getGuild().getID(), 9998);
    }

    /**
     * Called when the bot is finished initializing
     */
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
            welcomeUser(g, u);
            Database.addUser(u.getID(), g.getID());
            Database.addWelcomedUser(g.getID(), u.getID());
        } catch (Exception e) {
            logger.error("An error occurred while executing onJoin()", e);
            WLogger.logError(e);
        }
    }

    public static boolean welcomeUser(IGuild g, IUser u) {
        if (!Main.getDWeeboBot().getWelcomeDisabled(g)) {
            String[] welcomeInfo = Database.getWelcomeInfo(g.getID());
            for(String s : welcomeInfo) {
                logger.info(s);
            }
            welcomeInfo[1] = welcomeInfo[1].replace("%user%", u.getDisplayName(g));
            if (!welcomeInfo[1].equalsIgnoreCase("none")) {
                ActionQueue.addAction(ActionPriority.HIGH, ActionType.MESSAGESEND, g.getID(), welcomeInfo[0], welcomeInfo[1]);
                int delay = Integer.valueOf(welcomeInfo[2]);
                if(delay > 0 ) {
                    ActionQueue.addDelayedAction(ActionPriority.HIGH, ActionType.MESSAGEDELETE, delay + "s", g.getID(), welcomeInfo[0], MessageLog.getMessageFromContent(g.getID(), welcomeInfo[0], welcomeInfo[1]).getID());
                }
                return true;
            }
            return false;
        }
        return false;
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
        String command;
        try {
            command = c.substring(1, c.indexOf(' '));
        } catch (StringIndexOutOfBoundsException e) {
            command = c.substring(1, c.length());
        }
        if (Database.getChannelTables(g.getID())) {
            logger.info("Creating database channels for new guild!");
            Database.addOption(g.getID(), GOptions.welcomeMessage.getOptionID(), "none");
            Database.addOption(g.getID(), GOptions.deleteWelcome.getOptionID(), "0");
            Database.addOption(g.getID(), GOptions.welcomeChannel.getOptionID(), g.getChannels().get(0).getID());
            for (IChannel channel : g.getChannels()) {
                logger.info(channel.getName());
                logger.info(channel.getID());
            }
            Database.setUserPermissionLevel(g.getOwnerID(), g.getID(), Main.MAX_USER_LEVEL-1);
            if(!command.equalsIgnoreCase("setup")) {
                Main.getDWeeboBot().onFirstJoin(g, m.getChannel());
            }
        }
        logger.info(String.format("Received message in guild: %s channel: %s, with content: %s, at %s", g.getName(), m.getChannel().getName(), c, new SimpleDateFormat("MMM dd,yyyy HH:mm:ss").format(new Date(System.currentTimeMillis()))));
        try {
            if (u.equals(Main.getBot().getOurUser()) || u.isBot()) {
                return;
            }
            if (c.charAt(0) == '!') {
                String[] params = c.substring(c.indexOf(' ') + 1).split(" ");
                if (command.equalsIgnoreCase(params[0].substring(1))) {
                    params = new String[0];
                }
                String reply = CommandParser.parse(m, command, params);
                if (reply != null) {
                    ActionQueue.addAction(ActionPriority.MEDIUM, ActionType.MESSAGESEND, g.getID(), m.getChannel().getID(), reply);
                } else {
                    reply = CustomCommandParser.parse(m, command, params);
                    if (reply != null) {
                        ActionQueue.addAction(ActionPriority.MEDIUM, ActionType.MESSAGESEND, g.getID(), m.getChannel().getID(), reply);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("An error was thrown while executing onMessage() in Guild: "
                            + g.getID() + " Channel: " + m.getChannel().getID(), e);
            WLogger.logError(e);
        }
    }

    public static boolean isOwner(IUser user, IGuild guild) {
        return Objects.equals(user.getID(), guild.getOwnerID());
    }

    public static class ActionQueue implements Runnable{

        private static ConcurrentHashMap<ActionPriority, ConcurrentHashMap<ActionType, CopyOnWriteArrayList<CopyOnWriteArrayList<Object>>>> queue = new ConcurrentHashMap<>();
        private static CopyOnWriteArrayList<DelayedAction> delayedActions = new CopyOnWriteArrayList<>();
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
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        logger.warn("There was an issue sleeping to avoid rate limits.", e);
                    }
                } catch (MissingPermissionsException e) {
                    logger.warn("We don't have permission to preform an action!");
                } catch (DiscordException e) {
                    logger.warn("An error occurred trying to preform and action!");
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
            if (queue.get(priority).get(ActionType.MESSAGEEDIT) != null) {
                for (CopyOnWriteArrayList<Object> o : queue.get(priority).get(ActionType.MESSAGEEDIT)) {
                    MessageLog.addMessage(Main.getBot().getGuildByID((String) o.get(0)).getChannelByID((String) o.get(1)).getMessageByID((String) o.get(2)).edit((String) o.get(3)));
                    queue.get(priority).get(ActionType.MESSAGEEDIT).remove(o);
                    queueSize--;
                }
            }
        }

        /**
         * List<Object>[0] - guildID
         * List<Object>[1] - channelID
         * List<Object>[2] - messageID
         */
        private void processDeletes(ActionPriority priority) throws RateLimitException, DiscordException, MissingPermissionsException {
            if (queue.get(priority).get(ActionType.MESSAGEDELETE) != null) {
                for (CopyOnWriteArrayList<Object> o : queue.get(priority).get(ActionType.MESSAGEDELETE)) {
                    Main.getBot().getGuildByID((String) o.get(0)).getChannelByID((String) o.get(1)).getMessageByID((String) o.get(2)).delete();
                    queue.get(priority).get(ActionType.MESSAGEDELETE).remove(o);
                    queueSize--;
                }
            }
        }

        /**
         * List<Object>[0] - guildID
         * List<Object>[1] - channelID
         * List<Object>[2] - message
         */
        private void processSends(ActionPriority priority) throws RateLimitException, DiscordException, MissingPermissionsException {

            if (queue.get(priority).get(ActionType.MESSAGESEND) != null) {
                for (CopyOnWriteArrayList<Object> o : queue.get(priority).get(ActionType.MESSAGESEND)) {
                    MessageLog.addMessage(Main.getBot().getGuildByID((String) o.get(0)).getChannelByID((String) o.get(1)).sendMessage((String) o.get(2)));
                    queue.get(priority).get(ActionType.MESSAGESEND).remove(o);
                    queueSize--;
                }
            }
        }

        private void processExits(ActionPriority priority) {
            if (queue.get(priority).get(ActionType.EXIT) != null && queue.get(priority).get(ActionType.EXIT).size() > 0){
                Main.shutdown();
                queue.get(priority).get(ActionType.EXIT).remove(0);
                queueSize--;
            }
        }

        public static void addAction(ActionPriority priority, ActionType type, Object... parameters) {
            if(!queue.containsKey(priority)) {
                ConcurrentHashMap<ActionType, CopyOnWriteArrayList<CopyOnWriteArrayList<Object>>> tempSubMap = new ConcurrentHashMap<>();
                CopyOnWriteArrayList<CopyOnWriteArrayList<Object>> tempList = new CopyOnWriteArrayList<>();
                CopyOnWriteArrayList<Object> tempSubList = new CopyOnWriteArrayList<>();
                Collections.addAll(tempSubList, parameters);
                tempList.add(tempSubList);
                tempSubMap.put(type, tempList);
                queue.put(priority, tempSubMap);
                queueSize++;
                return;
            }
            ConcurrentHashMap<ActionType, CopyOnWriteArrayList<CopyOnWriteArrayList<Object>>> tempSubMap = queue.get(priority);
            if(!tempSubMap.containsKey(type)) {
                CopyOnWriteArrayList<CopyOnWriteArrayList<Object>> tempList = new CopyOnWriteArrayList<>();
                CopyOnWriteArrayList<Object> tempSubList = new CopyOnWriteArrayList<>();
                Collections.addAll(tempSubList, parameters);
                tempList.add(tempSubList);
                tempSubMap.put(type, tempList);
                queue.put(priority, tempSubMap);
                queueSize++;
                return;
            }
            CopyOnWriteArrayList<CopyOnWriteArrayList<Object>> tempList = queue.get(priority).get(type);
            CopyOnWriteArrayList<Object> tempSubList = new CopyOnWriteArrayList<>();
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
            static int queueSize;

            DelayedAction(ActionPriority priority, ActionType type, long delay, Object... parameters) {
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
