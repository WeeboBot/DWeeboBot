package io.github.weebobot.weebobot;

import io.github.weebobot.weebobot.database.Database;
import io.github.weebobot.weebobot.external.TwitchUtilities;

public class Driver {


    private final static String BTTV_URL = "https://api.betterttv.net/";

	public static void main(String[] args) {
        Database.initDBConnection(args[1]);
        TwitchUtilities.updateEmoteDatabase();
	}

}
