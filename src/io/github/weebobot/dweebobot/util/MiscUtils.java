package io.github.weebobot.dweebobot.util;

import sx.blah.discord.handle.obj.IUser;

/**
 * Created by James Wolff on 10/12/2016.
 */
public class MiscUtils {
    public static long delayToLong(String delay) {
        long delayAsLong;
        if(delay.contains("h")) {
            delayAsLong = Integer.valueOf(delay.replace("h", ""))*60L*60L*1000L;
        } else if(delay.contains("m")) {
            delayAsLong = Integer.valueOf(delay.replace("m", ""))*60L*1000L;
        } else if(delay.contains("s")) {
            delayAsLong = Integer.valueOf(delay.replace("s", ""))*1000L;
        } else {
            delayAsLong = (long) Integer.valueOf(delay.replace("s", ""));
        }
        return delayAsLong;
    }
}
