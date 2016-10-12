package io.github.weebobot.dweebobot.external;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.youtube.YouTube;

import java.io.IOException;

public class YoutubeUtilities {

    private static YouTube youtube;
    private static String key;

    public static void init(String key) {
        setKey(key);
    }

    public static boolean isValidLink(String videoLink) {
//        try{
//            youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), request -> {}).setApplicationName("dweebobot-1236").build();
//            YouTube.Search.List search = youtube.search().list("id,snippet");
//            search.setKey(key);
//            search.setQ(videoLink);
//            search.setType("video");
//            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
//            search.setMaxResults(25L);
//            SearchListResponse responses = search.execute();
//            List<SearchResult> videos = responses.getItems();
//            return videos.size() > 0;
//        }catch(IOException e){
//            e.printStackTrace();
//        }
        return false;
    }

    public static String[] getSongInfoFromLink(String videoLink, String channelNoHash) {
//        try{
//            youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), request -> {}).setApplicationName("dweebobot-1236").build();
//            YouTube.Search.List search = /*youtube.search().list("id,snippet")*/ null;
//            search.setKey(key);
//            search.setQ(videoLink);
//            search.setType("video");
//            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
//            search.setMaxResults(25L);

//            SearchListResponse responses = search.execute();
//            List<SearchResult> videos = responses.getItems();
//            if(videos.size() > 0){
//                SearchResult video = videos.get(0);
//                String[] videoInfo = new String[3];
//                videoInfo[0] = videoLink;
//                videoInfo[1] = video.getSnippet().getTitle();
//                videoInfo[2] = "" + Database.addSongToGlobalList(videoInfo[0], videoInfo[1]);
//                return videoInfo;
//            }
//        }catch(IOException e){
//        }
        return null;
    }

    private static void setKey(String apiKey) {
        key = apiKey;
    }

}