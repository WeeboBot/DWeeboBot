package io.github.weebobot.dweebobot.commands;

import io.github.weebobot.dweebobot.database.Database;
import sx.blah.discord.handle.obj.IGuild;

public class BotSubReddit extends Command{

    @Override
    public int getCommandLevel(IGuild guild) {
        return Database.getPermissionLevel(getCommandText(), guild);
    }

    @Override
    public String getCommandText() {
        return "botsubreddit";
    }

    @Override
    public String execute(String channel, String sender, String... parameters) {
        return "The SubReddit associated with WeeboBot can be found here https://www.reddit.com/r/dweebobot/";
    }

}
