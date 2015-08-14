package me.mage.bot.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public abstract class SocketListener implements Runnable{
	
	protected BufferedReader in;
	private Socket connection;
	
	public SocketListener(Socket conn) {
		connection = conn;
		try {
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
