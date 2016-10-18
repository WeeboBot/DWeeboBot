package io.github.weebobot.dweebobot.commands;

import sx.blah.discord.handle.obj.IGuild;

/**
 * Created by James on 10/17/2016.
 */
public class EditCommand extends Command {
    @Override
    public int getCommandLevel(IGuild guild) {
        return 0;
    }

    @Override
    public String getCommandText() {
        return null;
    }

    @Override
    public String execute(String channel, String sender, String... parameters) {
        return null;
    }
}
