package io.github.weebobot.weebobot.commands;

import io.github.weebobot.weebobot.util.CLevel;

public class Google extends Command {

	@Override
	public CLevel getCommandLevel() {
		return CLevel.Mod;
	}

	@Override
	public String getCommandText() {
		return "google";
	}

	@Override
	public String execute(String channel, String sender, String... parameters) {
		StringBuilder sb = new StringBuilder();
		sb.append("http://google.com/?q=");
		for(int i=0;i<parameters.length-1;i++) {
			sb.append(parameters[i]+"+");
		}
		sb.append(parameters[parameters.length-1]);
		return sb.toString();
	}

}
