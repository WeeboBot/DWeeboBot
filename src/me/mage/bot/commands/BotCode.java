package me.mage.bot.commands;

import me.mage.bot.util.CLevel;

public class BotCode extends Command{
	
	
	@Override
	public CLevel getCommandLevel() {
		return CLevel.Normal;
	}

	@Override
	public String getCommandText() {
		return "botcode";
	}

	@Override
	public String execute(String channel, String sender, String... parameters) {
		return "All code for this bot can be found at http://github.com/weebobot/weebobot";
	}

}
