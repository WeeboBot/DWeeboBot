package io.github.weebobot.dweebobot.commands;

import io.github.weebobot.dweebobot.database.Database;
import sx.blah.discord.handle.obj.IGuild;

public class Google extends Command {

    @Override
    public int getCommandLevel(IGuild guild) {
        return Database.getPermissionLevel(getCommandText(), guild);
    }

    @Override
    public String getCommandText() {
        return "google";
    }

    @Override
    public String execute(String channel, String sender, String... parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://google.com/search?q=");
        for(int i=0;i<parameters.length-1;i++) {
            sb.append(parameters[i]+"+");
        }
        sb.append(parameters[parameters.length-1]);
        return sb.toString();
    }

}
