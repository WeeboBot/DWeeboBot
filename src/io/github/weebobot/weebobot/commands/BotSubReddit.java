package io.github.weebobot.weebobot.commands;

import io.github.weebobot.weebobot.util.CLevel;

public class BotSubReddit extends Command{

	@Override
	public CLevel getCommandLevel() {
		return CLevel.Normal;
	}

	@Override
	public String getCommandText() {
		return "botsubreddit";
	}

	@Override
	public String execute(String channel, String sender, String... parameters) {
		return "The SubReddit associated with WeeboBot can be found here https://www.reddit.com/r/weebobot/";
	}

}
