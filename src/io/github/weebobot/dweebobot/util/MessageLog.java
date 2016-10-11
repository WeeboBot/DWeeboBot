package io.github.weebobot.dweebobot.util;

import io.github.weebobot.dweebobot.Main;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

import java.util.ArrayList;

/**
 * Created by James Wolff on 10/11/2016.
 */
public class MessageLog {
    private static ArrayList<IMessage> messageLog;

    public static void addMessage(IMessage message) {
        messageLog.add(message);
    }

    public static IMessage getMessageFromContent(IGuild g, IChannel c, String content) {
        for (IMessage m : messageLog) {
            if(m.getAuthor().equals(Main.getBot().getOurUser())) {
                if (m.getGuild().equals(g)) {
                    if (m.getChannel().equals(c)) {
                        if (m.getContent().equals(content)) {
                            return m;
                        }
                    }
                }
            }
        }
        return null;
    }
}
