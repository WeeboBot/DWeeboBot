/*      It's a Twitch bot, because we can.
 *    Copyright (C) 2015  Timothy Chandler, James Wolff
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.weebobot.dweebobot.commands;

import io.github.weebobot.dweebobot.Main;
import io.github.weebobot.dweebobot.database.Database;
import io.github.weebobot.dweebobot.external.DiscordListener;
import io.github.weebobot.dweebobot.util.WLogger;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandParser {
    private static HashMap<String, Command> commands;
    private static final Logger logger=Logger.getLogger(CommandParser.class+"");

    /**
     * Gets and stores all of the commands in a HashMap for easier use.
     * @author Jared314
     */
    public static void init() {
        commands=new HashMap<>();
        Reflections r = new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forJavaClassPath()));

        // Find all commands
        Set<Class<? extends Command>> commandClassList = r.getSubTypesOf(Command.class);

        // Add command instances to the commands hash map
        for(Class<? extends Command> cClass: commandClassList){

            try {
                Command c = cClass.newInstance();
                commands.put(c.getCommandText(), c);
            } catch (Exception e) {
                logger.log(Level.WARNING, "An error occurred initializing a command", e);
                WLogger.logError(e);
            }
        }
    }

    /**
     * ONLY TO BE CALLED BY COMMANDS SENT FROM THE COMMAND LINE
     *
     * @param command - command that was sent without the leading !
     * @param parameters - parameters sent along with the command
     * @return {@link Command#execute(String, String, String[])} or null if the command does not exist
     */
    public static String parse(String command, String[] parameters) {
        command = command.toLowerCase();
        Command c=commands.get(command);
        return c.execute(Main.MAX_USER_LEVEL, toStringArray(parseParameters(parameters)));
    }

    /**
     * @param message - IMessage object
     * @return {@link Command#execute(IMessage, String...)} or null if the command does not exist
     */
    public static String parse(IMessage message, String command, String[] parameters) {
        IUser sender = message.getAuthor();
        IGuild guild = message.getGuild();
        command = command.toLowerCase();
        Command c = commands.get(command);
        if(c != null && hasAccess(c, sender, guild)) {
            return c.execute(message, toStringArray(parseParameters(parameters)));
        }
        return null;
    }

    public static ArrayList<String> parseParameters(String[] parameters) {
        ArrayList<String> passed = new ArrayList<>();
        int i=0;
        while(i < parameters.length) {
            if(parameters[i].startsWith("\"")) {
                String temp=parameters[i].replace("\"", "") + " ";
                if(parameters[i].endsWith("\"")) {
                    passed.add(parameters[i].replace("\"", ""));
                    continue;
                }
                i++;
                boolean endQuote = true;
                while(!parameters[i].endsWith("\"")) {
                    temp+=parameters[i] + " ";
                    i++;
                    if(i >= parameters.length) {
                        endQuote = false;
                        break;
                    }
                }
                if(endQuote) {
                    temp+=parameters[i].replace("\"", "");
                }
                i++;
                passed.add(temp);
                continue;
            }
            passed.add(parameters[i]);
            i++;
        }
        return passed;
    }

    /**
     * @param c - Command object that matches what was passed
     * @param sender - user who sent the command
     * @param guild - guild the command was sent in
     * @return true if the user has valid access
     */
    private static boolean hasAccess(Command c, IUser sender, IGuild guild) {
        if(DiscordListener.isOwner(sender, guild)) {
            return true;
        }
        return c.getCommandLevel(guild) <= Database.getUserPermissionLevel(sender, guild);
    }

    private static String[] toStringArray(ArrayList<String> passed) {
        String[] result = new String[passed.size()];
        for(int i=0;i<passed.size();i++) {
            result[i] = passed.get(i);
        }
        return result;
    }
}
