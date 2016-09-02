package io.github.weebobot.dweebobot.commands;

import io.github.weebobot.dweebobot.util.CLevel;

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
		return "The website for WeeboBot can be found here http://dweebobot.no-ip.info/";
	}

}
