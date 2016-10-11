package io.github.weebobot.dweebobot.commands;

import io.github.weebobot.dweebobot.Main;
import io.github.weebobot.dweebobot.database.Database;
import io.github.weebobot.dweebobot.util.TType;
import sx.blah.discord.handle.obj.IGuild;

public class DelMessage extends Command {
    @Override
    public int getCommandLevel(IGuild guild) {
        return Main.MAX_USER_LEVEL-1;
    }

    @Override
    public String getCommandText() {
        return "delmessage";
    }

    @Override
    public String execute(String channel, String sender, String... parameters) {
        if(parameters.length != 3 || !parameters[0].equalsIgnoreCase("permission") || !parameters[1].toLowerCase().matches("(caps|symbols|emote|link|spam|paragraph)")) {
            return "Usage: !delmessage <permission> <caps|symbols|emote|link|spam|paragraph> \"message\"";
        }
        switch(parameters[1].toLowerCase()) {
            case "caps":
                if(Database.delMessage(channel.substring(1), TType.CAPS.getId(), parameters[2])) {
                    return String.format("The message matching: \"%s\" has been deleted from the list for Caps Timeouts", parameters[2]);
                }
                return "That does not match a record in our database!";
            case "symbols":
                if(Database.delMessage(channel.substring(1), TType.SYMBOLS.getId(), parameters[2])) {
                    return String.format("The message matching: \"%s\" has been deleted from the list for Symbols Timeouts", parameters[2]);
                }
                return "That does not match a record in our database!";
            case "emote":
                if(Database.delMessage(channel.substring(1), TType.EMOTE.getId(), parameters[2])) {
                    return String.format("The message matching: \"%s\" has been deleted from the list for Emote Timeouts", parameters[2]);
                }
                return "That does not match a record in our database!";
            case "link":
                if(Database.delMessage(channel.substring(1), TType.LINK.getId(), parameters[2])) {
                    return String.format("The message matching: \"%s\" has been deleted from the list for Link Timeouts", parameters[2]);
                }
                return "That does not match a record in our database!";
            case "spam":
                if(Database.delMessage(channel.substring(1), TType.SPAM.getId(), parameters[2])) {
                    return String.format("The message matching: \"%s\" has been deleted from the list for Spam Timeouts", parameters[2]);
                }
                return "That does not match a record in our database!";
            case "paragraph":
                if(Database.delMessage(channel.substring(1), TType.PARAGRAPH.getId(), parameters[2])) {
                    return String.format("The message matching: \"%s\" has been deleted from the list for Paragraph Timeouts", parameters[2]);
                }
                return "That does not match a record in our database!";
        }
        return "Usage: !delmessage <permission> <caps|symbols|emote|link|spam|paragraph> \"message\"";
    }
}
