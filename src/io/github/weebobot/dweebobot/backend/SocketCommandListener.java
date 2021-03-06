package io.github.weebobot.dweebobot.backend;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.weebobot.dweebobot.Main;
import io.github.weebobot.dweebobot.commands.CommandParser;
import io.github.weebobot.dweebobot.util.WLogger;

public class SocketCommandListener extends SocketListener implements Runnable{
	
	private static Logger logger = Logger.getLogger(SocketCommandListener.class + "");
	
	public SocketCommandListener(Socket conn) {
		super(conn);
	}
	
	public void run(){
		String message = null;
		try {
			message = in.readLine();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "There was an issue reading the line from the client", e);
			WLogger.logError(e);
		}
		String[] params = message.substring(message.indexOf(' ') + 1).split(" ");
		String command;
		try {
			command = message.substring(1, message.indexOf(' '));
		} catch(StringIndexOutOfBoundsException e) {
			command = message.substring(1, message.length());
		}
		if(command.equalsIgnoreCase(params[0].substring(1))) {
			params = new String[0];
		}
		Main.getBot().sendMessage(Main.getBotChannel(), CommandParser.parse(command, Main.getBotChannel().substring(1), Main.getBotChannel(), params));
	}

}
