package io.github.weebobot.weebobot.external;

import java.io.IOException;
import java.util.List;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import io.github.weebobot.weebobot.database.Database;

public class YoutubeUtilities {
	
	private static YouTube youtube;
	private static String key;
	
	public static boolean isValidLink(String videoLink) {
		try{
			YouTube.Search.List search = youtube.search().list("id,snippet");
			search.setKey(key);
			search.setType("video");
			search.setQ(videoLink);
			search.setMaxResults(1l);
			SearchListResponse responses = search.execute();
			List<SearchResult> videos = responses.getItems();
			return !videos.isEmpty();
		}catch(IOException e){
		}
		return false;
	}

	public static String[] getSongInfoFromLink(String videoLink) {
		try{
			YouTube.Search.List search = youtube.search().list("id,snippet");
			search.setKey(key);
			search.setType("video");
			search.setQ(videoLink);
			search.setMaxResults(1l);
			
			SearchListResponse responses = search.execute();
			List<SearchResult> videos = responses.getItems();
			if(!videos.isEmpty()){
				SearchResult video = videos.get(1);
				String[] videoInfo = new String[3];
				videoInfo[0] = videoLink;
				videoInfo[1] = video.getSnippet().getTitle();
				videoInfo[2] = Database.getNewSongID(videoInfo[0], videoInfo[1]);
				return videoInfo;
			}
		}catch(IOException e){
		}
		return null;
	}

	public static String setKey(String apiKey) {
		return key;
	}

}