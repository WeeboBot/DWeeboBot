package io.github.weebobot.dweebobot.commands;

import io.github.weebobot.dweebobot.Main;
import io.github.weebobot.dweebobot.database.Database;
import io.github.weebobot.dweebobot.util.WLogger;
import sx.blah.discord.handle.obj.IGuild;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by James on 10/17/2016.
 */
public class EditCommand extends Command {
    @Override
    public int getCommandLevel(IGuild guild) {
        return Database.getPermissionLevel(getCommandText(), guild);
    }

    @Override
    public String getCommandText() {
        return "editcom";
    }

    @Override
    public String execute(String channel, String sender, String... parameters) {
        String command = parameters[0].startsWith("!") ? parameters[0] : "!" + parameters[0];
        if (parameters[1].equalsIgnoreCase("--pl")) {
            try {
                if(!Database.updateCommandPermissions(Main.getBot().getChannelByID(channel).getGuild().getID(), command, Integer.valueOf(parameters[2]))) {
                    return "There was an issue updating %c% please try again".replace("%c%", command);
                }
            } catch (NumberFormatException e) {
                return String.format("Permission levels must be a number between 0 (lowest) and %d (level given to server administrators)", Main.MAX_USER_LEVEL-1);
            }
        }
        ResultSet c = Database.getCommand(Main.getBot().getChannelByID(channel).getGuild().getID(), command);
        try {
            if(c != null && c.getBoolean("isDefault")) {
                return "You can only edit the permission level of default commands";
            }
        } catch (SQLException e) {
            WLogger.logError(e);
        }
        StringBuilder params = new StringBuilder();
        for(int i = 2;i < parameters.length;i++) {
            params.append(parameters[i] + " ");
        }
        try {
            int level = Integer.valueOf(parameters[2]);
            if(!Database.updateCommand(Main.getBot().getChannelByID(channel).getGuild().getID(), parameters[0], params.toString(), parameters[1], level, false)) {
                return "There was an issue updating %c% please try again".replace("%c%", command);
            }
        } catch (NumberFormatException e) {
            if(!Database.updateCommand(Main.getBot().getChannelByID(channel).getGuild().getID(), parameters[0], params.toString(), parameters[1], 0, false)) {
                return "There was an issue updating %c% please try again".replace("%c%", command);
            }
        }
        return "Edited %command%".replace("%command%", parameters[0]);
    }
}