package io.github.weebobot.dweebobot.commands;

import io.github.weebobot.dweebobot.Main;
import io.github.weebobot.dweebobot.database.Database;
import sx.blah.discord.handle.obj.IGuild;

public class Permission extends Command {

    @Override
    public int getCommandLevel(IGuild guild) {
        return Database.getPermissionLevel(getCommandText(), guild);
    }

    @Override
    public String getCommandText() {
        return "permission";
    }

    @Override
    public String execute(String channel, String sender, String... parameters) {
        if(parameters.length < 2) {
            return "To change permissions for a user type !permission <user> <amount|++|-->";
        }
        int level = Database.getUserPermissionLevel(sender, Main.getBot().getChannelByID(channel).getGuild().getID());
        if (level == 9998) {
            return "You cannot change the permission level of the server owner!";
        }
        if(level != -1) {
            switch (parameters[1].toLowerCase()) {
                case "++":
                    level++;
                    break;
                case "--":
                    level--;
                    break;
                default:
                    try {
                        level = Integer.valueOf(parameters[1]);
                        if(level >= Main.MAX_USER_LEVEL-1) {
                            level = Main.MAX_USER_LEVEL-2;
                        }
                    } catch (NumberFormatException e) {
                        return "To change permissions for a user type !permission <user> <amount|++|-->";
                    }
            }
            if(level < Main.MAX_USER_LEVEL-1) {
                Database.setUserPermissionLevel(sender, Main.getBot().getChannelByID(channel).getGuild().getID(), level);
                return String.format("%s's permission level set to %d", Main.getBot().getUserByID(sender), level);
            }
            return String.format("%s already has the maximum permission level!", Main.getBot().getUserByID(sender));
        }
        return "To change permissions for a user type !permission <user> <amount|++|-->";
    }

}
