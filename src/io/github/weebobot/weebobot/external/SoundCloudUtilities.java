package io.github.weebobot.weebobot.external;

import io.github.weebobot.weebobot.database.Database;
import io.github.weebobot.weebobot.util.WLogger;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class SoundCloudUtilities {

    private static String CLIENT_SECRET;
    private static String CLIENT_ID = "7e3eab5767b8deeb9e8544cf0d3ba0d3";

    /**
     * Checks if the song is valid; we know it is a song from Play#getProperLink() so there is no need to use the api,
     * just check for a non 200 response code.
     *
     * @param id - the ID that we are storing in the database
     * @return true if the link is valid
     */
	public static boolean isValidID(String id) {
		try {
            String url = "http://soundcloud.com" + id;
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("GET");
			int responseCode = connection.getResponseCode();
			if (responseCode > 400) {
				return false;
			}
		} catch (IOException e) {
            WLogger.logError(e);
		}
        return true;
	}

	public static String[] getSongInfoFromId(String id) {
        try {
            URL url = new URL(String.format("http://api.soundcloud.com/resolve?url=http://soundcloud.com%s&client_id=%s", id, CLIENT_ID));
            URLConnection con = url.openConnection();
            InputStream in = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            String body = IOUtils.toString(in, encoding);
            url = new URL(String.format("http://api.soundcloud.com/tracks%s?client_id=%s", body.substring(body.lastIndexOf('/'), body.indexOf('?')), CLIENT_ID));
            con = url.openConnection();
            in = con.getInputStream();
            encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            body = IOUtils.toString(in, encoding);
            String[] videoInfo = new String[3];
            videoInfo[0] = id;
            int titleStart = body.indexOf("\"title\":") + 9;
            videoInfo[1] = body.substring(titleStart, body.indexOf('"', titleStart + 1));
            videoInfo[2] = "" + Database.addSongToGlobalList(videoInfo[0], videoInfo[1]);
            return videoInfo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
	}

    public static void setClientSecret(String secret) {
        CLIENT_SECRET = secret;
    }

}
