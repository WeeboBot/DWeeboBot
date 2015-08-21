package io.github.weebobot.weebobot.commands;

import io.github.weebobot.weebobot.database.Database;
import io.github.weebobot.weebobot.external.SoundCloudUtilities;
import io.github.weebobot.weebobot.external.YoutubeUtilities;
import io.github.weebobot.weebobot.util.CLevel;

public class Play extends Command{

	@Override
	public CLevel getCommandLevel() {
		return CLevel.Normal;
	}

	@Override
	public String getCommandText() {
		return "play";
	}

	@Override
	public String execute(String channel, String sender, String... parameters) {
		if(parameters.length != 1){
			return "Incorrect usage. Correct usage is !play [youtube link/soundcloud link]";
		}
		String[] videoInformation = new String[4];
		if(!YoutubeUtilities.isValidLink(parameters[0])){
			if(!SoundCloudUtilities.isValidID(parameters[0])){
				return String.format("%s that is an invalid link", sender);
			}
			
		}
		Database.addSongToQueue(channel.substring(1), sender, videoInformation);
		
		return null;
	}

}
