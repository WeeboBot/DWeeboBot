package io.github.weebobot.dweebobot.commands;

import io.github.weebobot.dweebobot.database.Database;
import io.github.weebobot.dweebobot.util.ULevel;
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
		if(parameters.length < 3) {
			return "To change permissions for a user level type !permission <userlevel> <links|caps|symbols|emotes|paragraph|blacklist> <allow|disallow>";
		}
		ULevel level = ULevel.getTypeFromString(parameters[0]);
		if(level != null) {
			String oldPerms = Database.getOption(channel.substring(1), level.getName().toLowerCase() + "Immunities");
			String newPerm;
			if(parameters[2].equalsIgnoreCase("allow")) {
				newPerm="1";
			} else if(parameters[2].equalsIgnoreCase("disallow")) {
				newPerm="0";
			} else {
				return "To change permissions for a user level type !permission <userlevel> <links|caps|symbols|emotes|paragraph|blacklist> <allow|disallow>";
			}
			if(parameters[1].equalsIgnoreCase("blacklist")) {
				Database.setOption(channel.substring(1), level.getName().toLowerCase() + "immunities", newPerm + oldPerms.substring(1));
				return String.format("%s's are now %sed to use blacklisted words!", level.getName(), parameters[2]);
			}
			if(parameters[1].equalsIgnoreCase("paragraph")) {
				Database.setOption(channel.substring(1), level.getName().toLowerCase() + "immunities", oldPerms.substring(0, 1) + newPerm + oldPerms.substring(2));
				return String.format("%s's are now %sed to use paragraphs!", level.getName(), parameters[2]);
			}
			if(parameters[1].equalsIgnoreCase("caps")) {
				Database.setOption(channel.substring(1), level.getName().toLowerCase() + "immunities", oldPerms.substring(0, 2) + newPerm + oldPerms.substring(3));
				return String.format("%s's are now %sed to use excessive caps!", level.getName(), parameters[2]);
			}
			if(parameters[1].equalsIgnoreCase("emotes")) {
				Database.setOption(channel.substring(1), level.getName().toLowerCase() + "immunities", oldPerms.substring(0, 3) + newPerm + oldPerms.substring(4));
				return String.format("%s's are now %sed to use excessive emotes!", level.getName(), parameters[2]);
			}
			if(parameters[1].equalsIgnoreCase("links")) {
				Database.setOption(channel.substring(1), level.getName().toLowerCase() + "immunities", oldPerms.substring(0, 4) + newPerm + oldPerms.substring(5));
				return String.format("%s's are now %sed to use links!", level.getName(), parameters[2]);
			}
			if(parameters[1].equalsIgnoreCase("symbols")) {
				Database.setOption(channel.substring(1), level.getName().toLowerCase() + "immunities", oldPerms.substring(0, 5) + newPerm);
				return String.format("%s's are now %sed to use excessive symbols!", level.getName(), parameters[2]);
			}
		}
		return "To change permissions for a user level type !permission <userlevel> <links|caps|symbols|emotes|paragraph|blacklist> <allow|disallow>";
	}

}
