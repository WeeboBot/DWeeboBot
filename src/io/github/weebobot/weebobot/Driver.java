package io.github.weebobot.weebobot;

import io.github.weebobot.weebobot.external.YoutubeUtilities;

public class Driver {


    private final static String BTTV_URL = "https://api.betterttv.net/";

	public static void main(String[] args) {
        YoutubeUtilities.init(args[0]);
        System.out.println(YoutubeUtilities.getSongInfoFromLink("donald10101", "?v=YER5uqxVs2A"));
	}

}
