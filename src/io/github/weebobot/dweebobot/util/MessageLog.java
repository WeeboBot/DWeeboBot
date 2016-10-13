package io.github.weebobot.dweebobot.util;

import io.github.weebobot.dweebobot.Main;
import io.github.weebobot.dweebobot.external.DiscordListener;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

import java.util.ArrayList;
import java.util.TimerTask;

/**
 * Created by James Wolff on 10/11/2016.
 */
public class MessageLog {
    private static ArrayList<IMessage> messageLog = new ArrayList<>();

    public static void addMessage(IMessage message) {
        messageLog.add(message);
    }

    public static IMessage getMessageFromContent(String gID, String cID, String content) {
        for (IMessage m : messageLog) {
            if(m.getAuthor().equals(Main.getBot().getOurUser())) {
                if (m.getGuild().getID().equals(gID)) {
                    if (m.getChannel().getID().equals(cID)) {
                        if (m.getContent().equals(content)) {
                            return m;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static IMessage removeMessage(IMessage message) {
        messageLog.remove(message);
        return message;
    }

    public static String getMessageIDFromContent(String gID, String cID, String content) {
        IMessage message = getMessageFromContent(gID, cID, content);
        if(message == null) {
            return null;
        }
        return message.getID();
    }

    public static class LogReader extends Thread {
        private DiscordListener.ActionQueue.DelayedAction delayedAction;
        private Object[] parameters;

        public LogReader(DiscordListener.ActionQueue.DelayedAction delayedAction, Object... parameters) {
            this.delayedAction = delayedAction;
            this.parameters = parameters;
            this.start();
        }

        @Override
        public void run() {
            if(delayedAction != null) {
                if(delayedAction.getAction().getType().equals(DiscordListener.ActionType.MESSAGEDELETE)) {
                    while(MessageLog.getMessageFromContent((String) parameters[0], (String) parameters[1], (String) parameters[2]) == null);
                    delayedAction.getAction().setParameters(new Object[] {parameters[0], parameters[1], MessageLog.getMessageIDFromContent((String) parameters[0], (String) parameters[1], (String) parameters[2])});
                    DiscordListener.ActionQueue.addDelayedAction(delayedAction);
                    delayedAction.startTimer();
                }
            }
        }
    }
}
