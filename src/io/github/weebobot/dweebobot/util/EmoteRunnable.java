package io.github.weebobot.dweebobot.util;

import io.github.weebobot.dweebobot.external.TwitchUtilities;

public class EmoteRunnable implements Runnable{

    public EmoteRunnable() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        TwitchUtilities.updateEmoteDatabase();
        try {
            Thread.sleep(1800000);
        } catch (InterruptedException e) {
            WLogger.logError(e);
        }
    }
}
