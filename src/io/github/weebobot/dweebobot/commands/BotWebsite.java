package io.github.weebobot.dweebobot.commands;

import io.github.weebobot.dweebobot.database.Database;
import sx.blah.discord.handle.obj.IGuild;

public class BotWebsite extends Command{

    @Override
    public int getCommandLevel(IGuild guild) {
        return Database.getPermissionLevel(getCommandText(), guild);
    }

    @Override
    public String getCommandText() {
        return "botwebsite";
    }

    @Override
    public String execute(String channel, String sender, String... parameters) {
        return "The website for DWeeboBot can be found here http://weebo.jameswolff.me/";
    }

}
