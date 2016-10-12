package io.github.weebobot.dweebobot.commands;

import io.github.weebobot.dweebobot.database.Database;
import sx.blah.discord.handle.obj.IGuild;

public class TopExemption extends Command {

    @Override
    public int getCommandLevel(IGuild guild) {
        return Database.getPermissionLevel(getCommandText(), guild);
    }

    @Override
    public String getCommandText() {
        return "topexemption";
    }

    @Override
    public String execute(String channel, String sender, String... parameters) {
        if (parameters.length == 2) {
            if (!parameters[0].toLowerCase().matches("(add|remove)")) {
                return "Incorrect parameters. Correct usage is !topexemption <add|remove> <username>";
            }
            if (parameters[0].toLowerCase().equals("add")) {
                Database.topExemption(channel.substring(1), parameters[1], false);
                return String.format("%s was successfully added to the list of people exempt from the !top command!",
                        parameters[1]);
            }
            Database.topExemption(channel.substring(1), parameters[1], true);
            return String.format("%s was successfully removed from the list of people exempt from the !top command!",
                    parameters[1]);
        } else {
            return "Insufficient paramaters. Format as !topexemption <add|remove> <username>";
        }
    }

}
