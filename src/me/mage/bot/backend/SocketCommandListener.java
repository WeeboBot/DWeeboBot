package me.mage.bot.backend;

import java.io.IOException;
import java.net.Socket;

import me.mage.bot.Main;
import me.mage.bot.commands.CommandParser;

public class SocketCommandListener extends SocketListener implements Runnable{
	
	public SocketCommandListener(Socket conn) {
		super(conn);
	}
	
	public void run(){
		String message = null;
		try {
			message = in.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
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
		CommandParser.parse(command, Main.getBotChannel().substring(1), Main.getBotChannel(), params);
	}

}
