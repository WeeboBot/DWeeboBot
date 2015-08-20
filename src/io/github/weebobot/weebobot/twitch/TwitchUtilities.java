/*	  It's a Twitch bot, because we can.
 *    Copyright (C) 2015  Timothy Chandler, James Wolff
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.weebobot.weebobot.twitch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import io.github.weebobot.weebobot.database.Database;
import io.github.weebobot.weebobot.util.ULevel;
import io.github.weebobot.weebobot.util.WLogger;

public class TwitchUtilities {

	private final static String BASE_URL = "https://api.twitch.tv/kraken/";
	private final static String CHARSET = StandardCharsets.UTF_8.name();

	private final static Logger logger = Logger.getLogger(TwitchUtilities.class
			+ "");

	/**
	 * Changes the title on streamers page
	 * 
	 * @param channelNoHash
	 *            - channel to change the title on
	 * @param title
	 *            - title to be changed to
	 */
	public static boolean updateTitle(String channelNoHash, String title) {
		String url = BASE_URL + "channels/" + channelNoHash + "/";
		String _method = "put";
		String oauth_token = Database.getUserOAuth(channelNoHash);
		String query = null;
		URLConnection connection = null;
		if (oauth_token == null) {
			return false;
		}
		try {
			query = String.format(
					"channel[status]=%s&_method=%s&oauth_token=%s",
					URLEncoder.encode(title, CHARSET),
					URLEncoder.encode(_method, CHARSET),
					URLEncoder.encode(oauth_token, CHARSET));
			connection = new URL(url + "?" + query).openConnection();
			connection.setRequestProperty("Accept-Charset", CHARSET);
			connection.getInputStream();
			return true;
		} catch (IOException e) {
			logger.log(
					Level.SEVERE,
					"An error occurred updating the title for " + channelNoHash,
					e);
			WLogger.logError(e);
			return false;
		}
	}

	/**
	 * Changes the game on the streamers page
	 * 
	 * @param channelNoHash
	 *            - channel to change the game on
	 * @param game
	 *            - game to be changed to
	 */
	public static boolean updateGame(String channelNoHash, String game) {
		String url = BASE_URL + "channels/" + channelNoHash + "/";
		String _method = "put";
		String oauth_token = Database.getUserOAuth(channelNoHash);
		String query = null;
		URLConnection connection = null;
		try {
			query = String.format("channel[game]=%s&_method=%s&oauth_token=%s",
					URLEncoder.encode(game, CHARSET),
					URLEncoder.encode(_method, CHARSET),
					URLEncoder.encode(oauth_token, CHARSET));
			connection = new URL(url + "?" + query).openConnection();
			connection.setRequestProperty("Accept-Charset", CHARSET);
			connection.getInputStream();
			return true;
		} catch (IOException e) {
			logger.log(Level.SEVERE, "An error occurred updating the game for "
					+ channelNoHash, e);
			WLogger.logError(e);
		}
		return false;
	}

	/**
	 * Checks if the sender is a follower of channel
	 * 
	 * @param sender
	 * @param channelNoHash
	 * @return - true if sender is following channel
	 */
	public static boolean isFollower(String channelNoHash, String sender) {
		try {
			String nextUrl = "https://api.twitch.tv/kraken/channels/"+channelNoHash+"/follows";
			JsonObject obj = new JsonParser().parse(
					new JsonReader(new InputStreamReader(new URL(nextUrl)
							.openStream()))).getAsJsonObject();
			if (obj.get("error") != null) { // ie it finds it
				return false;
			} else { // it does not find it
				int count = followerCount(channelNoHash);
				int pages = count / 25;
				if (count % 25 != 0) {
					pages++;
				}
				for (int i = 0; i < pages; i++) {
					for (int j = 0; j < 25; j++) {
						try {
							if (sender.equalsIgnoreCase(obj
									.getAsJsonArray("follows").get(j)
									.getAsJsonObject().get("user")
									.getAsJsonObject()
									.getAsJsonPrimitive("name")
									.getAsString())) {
								return true;
							}
						} catch (IndexOutOfBoundsException e) {
							return false;
						}
					}
					nextUrl = URLEncoder.encode(obj.getAsJsonArray("_links")
							.get(1).getAsJsonPrimitive().getAsString(), CHARSET);
					obj = new JsonParser().parse(
							new JsonReader(new InputStreamReader(new URL(
									nextUrl).openStream()))).getAsJsonObject();
				}
				return false;
			}
		} catch (FileNotFoundException e) {
			WLogger.log(channelNoHash + "Does not have any subscribers or is not partnered.");
		}catch (JsonIOException | JsonSyntaxException | IOException e) {
			logger.log(Level.SEVERE, "An error occurred checking if " + sender
					+ " is following " + channelNoHash.substring(1), e);
			WLogger.logError(e);
		}
		return false;
	}

	/**
	 * Checks if the sender is subscribed to channel
	 * 
	 * @param sender
	 * @param channelNoHash
	 * @return - true if sender is subscribed to channel
	 */
	public static boolean isSubscriber(String sender, String channelNoHash) {
		try {
			String userOAuth = Database.getUserOAuth(channelNoHash);
			String nextUrl = "https://api.twitch.tv/kraken/channels/"
					+ channelNoHash + "/subscriptions/?oauth_token="
					+ userOAuth;
			if(userOAuth == null) {
				return false;
			}
			JsonObject obj = null;
			try {
				obj = new JsonParser().parse(
						new JsonReader(new InputStreamReader(new URL(nextUrl)
								.openStream()))).getAsJsonObject();
			} catch (IOException e) {
				if(e.getLocalizedMessage().equalsIgnoreCase("Server returned HTTP response code: 422 for URL: https://api.twitch.tv/kraken/channels/"+channelNoHash+"/subscriptions/?oauth_token=" + userOAuth)) {
					return false;
				}
				logger.log(Level.SEVERE, String.format("There was an issue checking is %s is subscribed to %s", sender, channelNoHash), e);
				WLogger.logError(e);
			}
			if (obj.get("error") != null) { // ie it finds it
				return false;
			} else { // it does not find it
				int count = subscriberCount(channelNoHash, userOAuth);
				int pages = count / 25;
				if (count % 25 != 0) {
					pages++;
				}
				for (int i = 0; i < pages; i++) {
					for (int j = 0; j < 25; j++) {
						if (sender.equalsIgnoreCase(obj
								.getAsJsonArray("subscriptions").get(j)
								.getAsJsonObject()
								.getAsJsonPrimitive("display_name")
								.getAsString())) {
							return true;
						}
					}
					nextUrl = URLEncoder.encode(obj.getAsJsonArray("_links")
							.get(1).getAsJsonPrimitive().getAsString()
							+ "?oauth_token=" + userOAuth, CHARSET);
					obj = new JsonParser().parse(
							new JsonReader(new InputStreamReader(new URL(
									nextUrl).openStream()))).getAsJsonObject();
				}
				return false;
			}
		} catch (FileNotFoundException e) {
			WLogger.log(channelNoHash + "Does not have any subscribers or is not partnered.");
		}catch (JsonIOException | JsonSyntaxException | IOException e) {
			logger.log(Level.SEVERE, "An error occurred checking if " + sender
					+ " is following " + channelNoHash, e);
			WLogger.logError(e);
		}
		return false;
	}

	/**
	 * @param channelNoHash
	 *            - channel to run the commercial in without the leading #
	 * @return true if the commercial runs successfully
	 */
	public static boolean runCommercial(String channelNoHash) {
		String USER_AGENT = "Mozilla/5.0";
		String oauth_token = Database.getUserOAuth(channelNoHash);
		String url = BASE_URL + "channels/" + channelNoHash
				+ "/commercial/?oauth_token=" + oauth_token;
		URL obj = null;
		try {
			obj = new URL(url);
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE,
					"An error occurred trying to start a commercial for "
							+ channelNoHash, e);
			WLogger.logError(e);
		}

		HttpsURLConnection con = null;
		try {
			con = (HttpsURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
		} catch (IOException e) {
			logger.log(Level.SEVERE,
					"An error occurred trying to start a commercial for "
							+ channelNoHash, e);
			WLogger.logError(e);
		}
		con.setRequestProperty("User-agent", USER_AGENT);
		return false;
	}

	/**
	 * @param channelNoHash
	 *            - channel to run the commercial in without the leading #
	 * @param length
	 *            - commercial length
	 * @return true if the commercial runs successfully
	 */
	public static boolean runCommercial(String channelNoHash, int length) {
		String USER_AGENT = "Mozilla/5.0";
		String oauth_token = Database.getUserOAuth(channelNoHash);
		String url = BASE_URL + "channels/" + channelNoHash
				+ "/commercial/?oauth_token=" + oauth_token + "&length="
				+ length;
		URL obj = null;
		try {
			obj = new URL(url);
		} catch (MalformedURLException e) {
			logger.log(Level.SEVERE,
					"An error occurred trying to start a commercial for "
							+ channelNoHash, e);
			WLogger.logError(e);
		}

		HttpsURLConnection con = null;
		try {
			con = (HttpsURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
		} catch (IOException e) {
			logger.log(Level.SEVERE,
					"An error occurred trying to start a commercial for "
							+ channelNoHash, e);
			WLogger.logError(e);
		}
		con.setRequestProperty("User-agent", USER_AGENT);
		return false;
	}

	/**
	 * Gets the amount of people following the specified channel
	 * 
	 * @param channelNoHash
	 * @return number of followers for channel, 0 if an error occurs
	 */
	public static int followerCount(String channelNoHash) {
		try {
			return new JsonParser()
					.parse(new JsonReader(new InputStreamReader(new URL(
							BASE_URL + "channels/" + channelNoHash + "/follows")
							.openStream()))).getAsJsonObject()
					.getAsJsonPrimitive("followers").getAsInt();
		} catch (JsonIOException | JsonSyntaxException | IOException | NullPointerException e) {
			logger.log(Level.SEVERE,
					"An error occurred getting the follower count for "
							+ channelNoHash, e);
			WLogger.logError(e);
		}
		return 0;
	}

	/**
	 * Gets the amount of people subscribed to the specified channel
	 * 
	 * @param channelNoHash
	 * @param oAuth
	 * @return number of subscribers for the channel
	 */
	public static int subscriberCount(String channelNoHash, String oAuth) {
		try {
			return new JsonParser()
					.parse(new JsonReader(new InputStreamReader(new URL(
							BASE_URL + "channels/" + channelNoHash
									+ "/subscriptions/?oauth_token=" + oAuth)
							.openStream()))).getAsJsonObject()
					.getAsJsonPrimitive("_total").getAsInt();
		} catch (JsonIOException | JsonSyntaxException | IOException | NullPointerException e) {
			logger.log(Level.SEVERE,
					"An error occurred getting the subscriber count for "
							+ channelNoHash, e);
			WLogger.logError(e);
		}
		return 0;
	}

	/**
	 * @param channelNoHash
	 *            - channel to run the commercial in without the leading #
	 * @return true if the channel is live, false otherwise
	 */
	public static boolean isLive(String channelNoHash) {
		try {
			new JsonParser()
					.parse(new JsonReader(
							new InputStreamReader(new URL(BASE_URL + "streams/"
									+ channelNoHash).openStream())))
					.getAsJsonObject().getAsJsonObject("stream").getAsJsonNull();
			return false;
		} catch (IllegalStateException | ClassCastException e) {
			return true;
		} catch (JsonSyntaxException | IOException e) {
			logger.log(Level.SEVERE,
					"An error occurred checking if the streamer is live!", e);
			WLogger.logError(e);
		}
		return false;
	}

	public static String getUserLevelNoMod(String channelNoHash, String moderator) {
		if(isSubscriber(moderator, channelNoHash)) {
			return ULevel.Subscriber.getName();
		}
		if(Database.isRegular(moderator, channelNoHash)) {
			return ULevel.Regular.getName();
		}
		if(isFollower(channelNoHash, moderator)) {
			return ULevel.Follower.getName();
		}
		return ULevel.Normal.getName();
	}

}
