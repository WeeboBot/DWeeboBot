package io.github.weebobot.dweebobot.commands;

import io.github.weebobot.dweebobot.Main;
import io.github.weebobot.dweebobot.database.Database;
import io.github.weebobot.dweebobot.external.DiscordListener;
import sx.blah.discord.handle.obj.IGuild;

/**
 * Created by James Wolff on 10/12/2016.
 */
public class TestWelcome extends Command {
    @Override
    public int getCommandLevel(IGuild guild) {
        return Main.MAX_USER_LEVEL-1;
    }

    @Override
    public String getCommandText() {
        return "testwelcome";
    }

    @Override
    public String execute(String channel, String sender, String... parameters) {
        if(DiscordListener.welcomeUser(Main.getBot().getChannelByID(channel).getGuild(), Main.getBot().getUserByID(sender))) {
            return String.format("Welcome test sent in channel #%s", Main.getBot().getChannelByID(Database.getWelcomeInfo(Main.getBot().getChannelByID(channel).getGuild().getID())[0]));
        }
        return "Welcome messages in your guild are either temporarily disable or you have not set up a welcome message type `!help welcomemessage` for more info!";
    }
}
