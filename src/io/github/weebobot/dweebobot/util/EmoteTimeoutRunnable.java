package io.github.weebobot.dweebobot.util;

import io.github.weebobot.dweebobot.database.Database;

public class EmoteTimeoutRunnable implements Runnable{

    private String channel, sender, message;
    private int emotes;

    public EmoteTimeoutRunnable(String channel, String sender, String message, int emotes) {
        this.channel = channel;
        this.sender = sender;
        this.message = message;
        this.emotes = emotes;
        new Thread(this).start();
    }

    @Override
    public void run() {
        int emoteCount = 0;
        for (String s : Database.getEmoteListAsArray(channel.substring(1))) {
            emoteCount += countOccurrences(message, s);
        }
        if(emoteCount >= emotes) {
                new Timeouts(channel, sender, 1, TType.EMOTE);
        }
    }

    private int countOccurrences(String message, String s) {
        int occurrences = 0;
        if(message.contains(s)) {
            for(int i = 0; i < message.length(); i++) {
                if(message.charAt(i) == s.charAt(0)) {
                    boolean match = true;
                    for(int j = 1, k = i + 1; j < s.length() && k < message.length(); j++, k++) {
                        if(message.charAt(k) != s.charAt(j)) {
                            match = false;
                            break;
                        }
                    }
                    if(match) {
                        occurrences++;
                    }
                }
            }
            return occurrences;
        }
        return 0;
    }
}
