package io.github.weebobot.dweebobot.commands;

import io.github.weebobot.dweebobot.Main;
import io.github.weebobot.dweebobot.database.Database;
import sx.blah.discord.handle.obj.IGuild;

public class StealSong extends Command {
    @Override
    public int getCommandLevel(IGuild guild) {
        return Main.MAX_USER_LEVEL-1;
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
