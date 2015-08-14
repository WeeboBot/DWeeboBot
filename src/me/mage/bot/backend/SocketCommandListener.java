package me.mage.bot.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import me.mage.bot.Main;
import me.mage.bot.commands.CommandParser;

public class SocketCommandListener implements Runnable{
	
	private BufferedReader in;
	private Socket connection;
	
	public SocketCommandListener(Socket conn) {
		connection = conn;
		try {
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
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
