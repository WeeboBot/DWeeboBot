package io.github.weebobot.weebobot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class Driver {


    private final static String BTTV_URL = "https://api.betterttv.net/";

	public static void main(String[] args) {
        try {
            JsonArray jsonEmotes = new JsonParser()
                    .parse(new JsonReader(
                            new InputStreamReader(new URL(BTTV_URL + "emotes").openStream())))
                    .getAsJsonObject().getAsJsonArray("emotes").getAsJsonArray();

            for (JsonElement element :jsonEmotes) {
                System.out.println(element.getAsJsonObject().getAsJsonPrimitive("regex").getAsString());
            }
        } catch (JsonSyntaxException | IOException e) {
            e.printStackTrace();
        }
	}

}
