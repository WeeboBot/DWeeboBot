package io.github.weebobot.dweebobot.commands;

import io.github.weebobot.dweebobot.Main;
import io.github.weebobot.dweebobot.database.Database;
import sx.blah.discord.handle.obj.IGuild;

public class Points extends Command {

    @Override
    public int getCommandLevel(IGuild guild) {
        return Database.getPermissionLevel(getCommandText(), guild);
    }

    @Override
    public String getCommandText() {
        return "points";
    }

    @Override
    public String execute(String channel, String sender, String... parameters) {
        if(parameters.length == 0) {
            String pts = Database.getPoints(sender, channel.substring(1));
            if (pts == null) {
                return "%user% has no points!".replace("%user%", sender);
            } else {
                return String.format("%s has %s point(s)!", sender, pts);
            }
        }
        if(Database.getUserPermissionLevel(Main.getBot().getUserByID(sender), Main.getBot().getChannelByID(channel).getGuild()) < Database.getPermissionLevel(getCommandText() + "Update", Main.getBot().getChannelByID(channel).getGuild())) {
            return null;
        }
        if (parameters.length < 3) {
            return "Hey, thats not right, use !points <add|remove> <user> <ammount>";
        }
        if (!parameters[0].matches("(add|remove)")) {
            return "Hey, thats not right, use !points <add|remove> <user> <ammount>";
        }
        if (Database.getPoints(parameters[1], channel.substring(1)) == null) {
            return "%user% has never been in the channel before!".replace("%user%", parameters[1]);
        }
        int amt = -1;
        int currentPoints = Integer.valueOf(Database.getPoints(parameters[1], channel.substring(1)));
        try {
            try {
                amt = Math.abs(Integer.valueOf(parameters[2]));
            } catch(Exception e1){
                return "That number is too big!";
            }
            if(amt>9000){
                return "That number is OVER 9000!!! Kappa";
            }
        } catch (NumberFormatException e1) {
            return "Hey, thats not right, use !points <add|remove> <user> <ammount>";
        }
        if (parameters[0].equalsIgnoreCase("add")) {
            Database.addPoints(parameters[1], channel.substring(1), amt);
            return String.format("Succesfully changed the points for %s to %s", parameters[1], Database.getPoints(parameters[1], channel.substring(1)));
        } else {
            if(amt>currentPoints){
                Database.addPoints(parameters[1], channel.substring(1), -currentPoints);
                return String.format("Succesfully changed the points for %s to %s", parameters[1], Database.getPoints(parameters[1], channel.substring(1)));
            }
            Database.addPoints(parameters[1], channel.substring(1), -amt);
            return String.format("Succesfully changed the points for %s to %s", parameters[1], Database.getPoints(parameters[1], channel.substring(1)));
        }


    }

}
