package io.github.weebobot.weebobot.commands;

import io.github.weebobot.weebobot.database.Database;
import io.github.weebobot.weebobot.util.CLevel;
import io.github.weebobot.weebobot.util.TType;

public class AddMessage extends Command {
    @Override
    public CLevel getCommandLevel() {
        return CLevel.Owner;
    }

    @Override
    public String getCommandText() {
        return "addmessage";
    }

    @Override
    public String execute(String channel, String sender, String... parameters) {
        if(parameters.length != 3 || !parameters[0].equalsIgnoreCase("permission") || !parameters[1].toLowerCase().matches("(caps|symbols|emote|link|spam|paragraph)")) {
            return "Usage: !addmessage <permission> <caps|symbols|emote|link|spam|paragraph> \"message\"";
        }
        switch(parameters[1].toLowerCase()) {
            case "caps":
                Database.addMessage(channel.substring(1), TType.CAPS.getId(), parameters[2] + " (Too many Capital letters)");
                return String.format("The message: \"%s %s\" has been added to the list for Caps Timeouts", parameters[2], "(Too many Capital letters)");
            case "symbols":
                Database.addMessage(channel.substring(1), TType.SYMBOLS.getId(), parameters[2] + " (Too many symbols)");
                return String.format("The message: \"%s %s\" has been added to the list for Symbols Timeouts", parameters[2], "(Too many symbols)");
            case "emote":
                Database.addMessage(channel.substring(1), TType.EMOTE.getId(), parameters[2] + " (Too many emotes)");
                return String.format("The message: \"%s %s\" has been added to the list for Emote Timeouts", parameters[2], "(Too many emotes)");
            case "link":
                Database.addMessage(channel.substring(1), TType.LINK.getId(), parameters[2] + " (Link)");
                return String.format("The message: \"%s %s\" has been added to the list for Link Timeouts", parameters[2], "(Link)");
            case "spam":
                Database.addMessage(channel.substring(1), TType.SPAM.getId(), parameters[2] + " (Spam)");
                return String.format("The message: \"%s %s\" has been added to the list for Spam Timeouts", parameters[2], "(Spam)");
            case "paragraph":
                Database.addMessage(channel.substring(1), TType.PARAGRAPH.getId(), parameters[2] + " (Too many characters in your message)");
                return String.format("The message: \"%s %s\" has been added to the list for Paragraph Timeouts", parameters[2], "(Too many characters in your message)");
        }
        return "Usage: !addmessage <permission> <caps|symbols|emote|link|spam|paragraph> \"message\"";
    }
}
