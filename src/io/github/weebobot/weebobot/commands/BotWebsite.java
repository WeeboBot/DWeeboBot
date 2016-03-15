package io.github.weebobot.weebobot.commands;

import io.github.weebobot.weebobot.util.CLevel;

public class BotWebsite extends Command{

	@Override
	public CLevel getCommandLevel() {
		return CLevel.Normal;
	}

	@Override
	public String getCommandText() {
		return "botwebsite";
	}

	@Override
	public String execute(String channel, String sender, String... parameters) {
		return "The website for WeeboBot can be found here http://weebobot.no-ip.info/";
	}

}
