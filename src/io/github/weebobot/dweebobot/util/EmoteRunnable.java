package io.github.weebobot.dweebobot.util;

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
