package io.github.weebobot.dweebobot.backend;

import io.github.weebobot.dweebobot.commands.CommandParser;
import io.github.weebobot.dweebobot.util.WLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

public class SocketCommandListener extends SocketListener implements Runnable{
	
	private static Logger logger = LoggerFactory.getLogger(SocketCommandListener.class);
	
	public SocketCommandListener(Socket conn) {
		super(conn);
	}
	
	public void run(){
		String message = null;
		try {
			message = in.readLine();
		} catch (IOException e) {
			logger.warn("There was an issue reading the line from the client", e);
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
		logger.info(CommandParser.parse(command, params));
	}

}
