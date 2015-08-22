package io.github.weebobot.weebobot.commands;

import io.github.weebobot.weebobot.Main;
import io.github.weebobot.weebobot.database.Database;
import io.github.weebobot.weebobot.external.SoundCloudUtilities;
import io.github.weebobot.weebobot.external.YoutubeUtilities;
import io.github.weebobot.weebobot.util.CLevel;

public class Play extends Command{

	@Override
	public CLevel getCommandLevel() {
		return CLevel.Owner;
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
		System.out.println(parameters[0]);
		String link = getProperLink(parameters[0]);
		if(link == null){
			return String.format("%s that is an invalid link", sender);
		}
		if(Database.isInList(Main.getBotChannel().substring(1), link)){
			if(Database.isInQueue(channel.substring(1), link)){
				return "That song is already in the queue!";
			}
			return Database.addSongToQueue(channel.substring(1), sender, Database.getSongInfoFromLink(link));
		}
		if(!YoutubeUtilities.isValidLink(link)){
			if(!SoundCloudUtilities.isValidID(link)){
				return String.format("%s that is an invalid link", sender);
			}
			return Database.addSongToQueue(channel.substring(1), sender, SoundCloudUtilities.getSongInfoFromLink(link));
		}
		return Database.addSongToQueue(channel.substring(1), sender, YoutubeUtilities.getSongInfoFromLink(link));
	}
	
	private String getProperLink(String link){
		if(link.matches("(http(?:s?)://)?(?:www\\.)?youtu(?:be\\.com/watch\\?v=|\\.be/)([\\w\\-]+)(&(amp;)?[\\w\\?=]*)?")){
			if(link.contains("v=")){
				String temp = link.substring(link.indexOf("v"));
				if(temp.contains("&")){
					temp = temp.substring(0, temp.indexOf("&"));
				}
				return "?" + temp;
			}
			return "?v=" + link.substring(link.lastIndexOf("/")+1);
		}
		if(!link.contains("/")){
			if(link.contains("v=")){
				String temp = link.substring(link.indexOf("v"));
				if(temp.contains("&")){
					temp = temp.substring(0, temp.indexOf("&"));
				}
				return "?" + temp;
			}
			return null;
		}
//		need to implement proper souncloud checking
//		if(link.matches("(https?:\\/\\/)?(www\\.)?(soundcloud.com|snd.sc)\\/(.*)")){
//			
//		}
		return null;
	}
}
