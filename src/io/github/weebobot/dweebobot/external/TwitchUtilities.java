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

package io.github.weebobot.dweebobot.external;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import io.github.weebobot.dweebobot.database.Database;
import io.github.weebobot.dweebobot.util.ULevel;
import io.github.weebobot.dweebobot.util.WLogger;

import javax.net.ssl.HttpsURLConnection;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TwitchUtilities {

	private final static String BASE_URL = "https://api.twitch.tv/kraken/";
    private final static String EMOTE_URL = "https://twitchemotes.com/api_cache/v2/";
    private final static String BTTV_URL = "https://api.betterttv.net/";
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
		String query;
		URLConnection connection;
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

	public static String getTitle(String channelNoHash) {
        try {
            return new JsonParser().parse(
                    new JsonReader(new InputStreamReader(new URL(
                            BASE_URL + "channels/" + channelNoHash).openStream()))).getAsJsonObject().getAsJsonPrimitive("status").getAsString();
        } catch (IOException e) {
            return "There was an issue getting the title for this channel. Please try again later!";
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
		String query;
		URLConnection connection;
		try {
            if(oauth_token == null)
                throw new NullPointerException();
			query = String.format("channel[game]=%s&_method=%s&oauth_token=%s",
					URLEncoder.encode(game, CHARSET),
					URLEncoder.encode(_method, CHARSET),
					URLEncoder.encode(oauth_token, CHARSET));
			connection = new URL(url + "?" + query).openConnection();
			connection.setRequestProperty("Accept-Charset", CHARSET);
			connection.getInputStream();
			return true;
		} catch (NullPointerException | IOException e) {
			logger.log(Level.SEVERE, "An error occurred updating the game for "
					+ channelNoHash, e);
			WLogger.logError(e);
		}
		return false;
	}

    public static String getGame(String channelNoHash) {
        try {
            return new JsonParser().parse(
                    new JsonReader(new InputStreamReader(new URL(
                            BASE_URL + "channels/" + channelNoHash).openStream()))).getAsJsonObject().getAsJsonPrimitive("game").getAsString();
        } catch (IOException e) {
            return "There was an issue getting the game for this channel. Please try again later!";
        }
    }

	/**
	 * Checks if the sender is a follower of channel
	 * 
	 * @param sender - the user who sent the message
	 * @param channelNoHash - the channel the message was sent in
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
					nextUrl = obj.getAsJsonObject("_links").getAsJsonPrimitive("next").getAsString();
					obj = new JsonParser().parse(
							new JsonReader(new InputStreamReader(new URL(
									nextUrl).openStream()))).getAsJsonObject();
				}
				return false;
			}
		} catch (FileNotFoundException e) {
			WLogger.log(channelNoHash + "Does not have any followers.");
		} catch (JsonIOException | JsonSyntaxException | IOException e) {
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
	 * Checks if the sender is subscribed to channel
	 * 
	 * @param sender - user who sent the message
	 * @param channelNoHash - channel the message was sent in
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
					nextUrl = obj.getAsJsonObject("_links")
							.getAsJsonPrimitive().getAsString()
							+ "?oauth_token=" + userOAuth;
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
		try(InputStream stream = new URL(
                BASE_URL + "channels/" + channelNoHash + "/follows")
                .openStream()) {
			return new JsonParser()
					.parse(new JsonReader(new InputStreamReader(stream))).getAsJsonObject()
					.getAsJsonPrimitive("_total").getAsInt();
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
	 * @param channelNoHash - channel to get subscriber count for
	 * @param oAuth - oAuth of the channel
	 * @return number of subscribers for the channel
	 */
	public static int subscriberCount(String channelNoHash, String oAuth) {
		try(InputStream stream = new URL(
                BASE_URL + "channels/" + channelNoHash
                        + "/subscriptions/?oauth_token=" + oAuth)
                .openStream()) {
			return new JsonParser()
					.parse(new JsonReader(new InputStreamReader(stream))).getAsJsonObject()
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
		try(InputStream stream = new URL(BASE_URL + "streams/" + channelNoHash).openStream()) {
			new JsonParser()
					.parse(new JsonReader(
							new InputStreamReader(stream)))
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

	public static String getUserLevelNoMod(String channelNoHash, String user) {
		if(isSubscriber(user, channelNoHash)) {
			return ULevel.Subscriber.getName();
		}
		if(Database.isRegular(user, channelNoHash)) {
			return ULevel.Regular.getName();
		}
		if(isFollower(channelNoHash, user)) {
			return ULevel.Follower.getName();
		}
		return ULevel.Normal.getName();
	}

	public static void updateEmoteDatabase() {
		for(String emote : getGlobalEmotes()) {
			if(!Database.emoteExists(emote)) {
				Database.addEmote(emote);
			}
		}
		for(String emote : getBTTVEmotes()) {
			if(!Database.emoteExists(emote)) {
                Database.addEmote(emote);
			}
		}
        for(String emote : getSubEmotes()) {
            if(!Database.emoteExists(emote)) {
                Database.addEmote(emote);
            }
        }
	}

    private static ArrayList<String> getGlobalEmotes() {
        ArrayList<String> globalEmotes = new ArrayList<>();
        try(InputStream stream = new URL(EMOTE_URL + "global.json").openStream()) {
            Set<Map.Entry<String, JsonElement>> channels = new JsonParser()
                    .parse(new JsonReader(
                            new InputStreamReader(stream)))
                    .getAsJsonObject().getAsJsonObject("emotes").entrySet();

            for(Map.Entry entry: channels) {
                globalEmotes.add(entry.getKey().toString());
            }
        } catch (JsonSyntaxException | IOException e) {
            logger.log(Level.WARNING,
                    "An error occurred updating the emote database!", e);
            WLogger.logError(e);
        }
        return globalEmotes;
    }

    public static ArrayList<String> getSubEmotes() {
        ArrayList<String> subEmotes = new ArrayList<>();
        InputStream stream = null;
        InputStream stream1 = null;
        try {
            stream = new URL(EMOTE_URL + "sets.json").openStream();
            Set<Map.Entry<String, JsonElement>> channels = new JsonParser()
                    .parse(new JsonReader(
                            new InputStreamReader(stream)))
                    .getAsJsonObject().getAsJsonObject("sets").entrySet();
            stream1 = new URL(EMOTE_URL + "subscriber.json").openStream();
            JsonObject subEmotesObject = new JsonParser().parse(new JsonReader(
                    new InputStreamReader(stream1))).getAsJsonObject().getAsJsonObject("channels");
            channels.stream().filter(entry -> !entry.getValue().toString().replaceAll("\"", "").startsWith("--")).forEach(entry -> {
                JsonElement channel = subEmotesObject.getAsJsonObject(entry.getValue().toString().replaceAll("\"", ""));
                if (channel != null) {
                    for (JsonElement element : channel.getAsJsonObject().getAsJsonArray("emotes").getAsJsonArray()) {
                        subEmotes.add(element.getAsJsonObject().getAsJsonPrimitive("code").getAsString());
                    }
                }
            });
        } catch (JsonSyntaxException | IOException e) {
            logger.log(Level.WARNING,
                    "An error occurred updating the emote database!", e);
            WLogger.logError(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
                if (stream1 != null) {
                    stream1.close();
                }
            } catch(IOException e) {
                WLogger.logError(e);
            }
        }
        return subEmotes;
    }

    private static ArrayList<String> getBTTVEmotes() {
        ArrayList<String> bttvEmotes = new ArrayList<>();
        try(InputStream stream = new URL(BTTV_URL + "emotes").openStream()) {
            JsonArray jsonEmotes = new JsonParser()
                    .parse(new JsonReader(
                            new InputStreamReader(stream)))
                    .getAsJsonObject().getAsJsonArray("emotes").getAsJsonArray();

            for (JsonElement element :jsonEmotes) {
                bttvEmotes.add(element.getAsJsonObject().getAsJsonPrimitive("regex").getAsString());
            }
        } catch (JsonSyntaxException | IOException e) {
            logger.log(Level.WARNING,
                    "An error occurred updating the emote database!", e);
            WLogger.logError(e);
        }
        return bttvEmotes;
    }
}
