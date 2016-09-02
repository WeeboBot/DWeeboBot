package io.github.weebobot.dweebobot.commands;

import io.github.weebobot.dweebobot.database.Database;
import io.github.weebobot.dweebobot.util.CLevel;

public class StealSong extends Command {
    @Override
    public CLevel getCommandLevel() {
        return CLevel.Normal;
    }

    @Override
    public String getCommandText() {
        return "stealsong";
    }

    @Override
    public String execute(String channel, String sender, String... parameters) {
        return Database.addSongToList(channel.substring(1), sender, Database.getSongInfoFromLink(channel.substring(1), Database.getCurrentSong(channel.substring(1))));
    }
}
