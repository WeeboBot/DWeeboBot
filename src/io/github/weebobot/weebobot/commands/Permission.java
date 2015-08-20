package io.github.weebobot.weebobot.commands;

import io.github.weebobot.weebobot.database.Database;
import io.github.weebobot.weebobot.util.CLevel;
import io.github.weebobot.weebobot.util.ULevel;

public class Permission extends Command {

	@Override
	public CLevel getCommandLevel() {
		return CLevel.Mod;
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
		if(parameters[1].matches("(links|caps|symbols|emotes|paragraph|blacklist)")) {
			if(ULevel.getTypeFromString(parameters[0]) != null) {
				String oldPerms = Database.getOption(channel.substring(1), parameters[0].toLowerCase() + "Immunities");
				String newPerm;
				if(parameters[2].equalsIgnoreCase("allow")) {
					newPerm="1";
				}
				if(parameters[2].equalsIgnoreCase("disallow")) {
					newPerm="0";
				} else {
					return "To change permissions for a user level type !permission <userlevel> <links|caps|symbols|emotes|paragraph|blacklist> <allow|disallow>";
				}
				if(parameters[1].equalsIgnoreCase("blacklist")) {
					Database.setOption(channel, parameters[0].toLowerCase() + "Immunities", newPerm + oldPerms.substring(1));
					return String.format("%s's are now %sed to use blacklisted words!", parameters[0], parameters[2]);
				}
				if(parameters[1].equalsIgnoreCase("paragraph")) {
					Database.setOption(channel, parameters[0].toLowerCase() + "Immunities", oldPerms.substring(0, 1) + newPerm + oldPerms.substring(2));
					return String.format("%s's are now %sed to use paragraphs!", parameters[0], parameters[2]);
				}
				if(parameters[1].equalsIgnoreCase("caps")) {
					Database.setOption(channel, parameters[0].toLowerCase() + "Immunities", oldPerms.substring(0, 2) + newPerm + oldPerms.substring(3));
					return String.format("%s's are now %sed to use excessive caps!", parameters[0], parameters[2]);
				}
				if(parameters[1].equalsIgnoreCase("emotes")) {
					Database.setOption(channel, parameters[0].toLowerCase() + "Immunities", oldPerms.substring(0, 3) + newPerm + oldPerms.substring(4));
					return String.format("%s's are now %sed to use excessive emotes!", parameters[0], parameters[2]);
				}
				if(parameters[1].equalsIgnoreCase("links")) {
					Database.setOption(channel, parameters[0].toLowerCase() + "Immunities", oldPerms.substring(0, 4) + newPerm + oldPerms.substring(5));
					return String.format("%s's are now %sed to use links!", parameters[0], parameters[2]);
				}
				if(parameters[1].equalsIgnoreCase("symbols")) {
					Database.setOption(channel, parameters[0].toLowerCase() + "Immunities", oldPerms.substring(0, 5) + newPerm);
					return String.format("%s's are now %sed to use excessive symbols!", parameters[0], parameters[2]);
				}
			}
		}
		return "To change permissions for a user level type !permission <userlevel> <links|caps|symbols|emotes|paragraph|blacklist> <allow|disallow>";
	}

}
