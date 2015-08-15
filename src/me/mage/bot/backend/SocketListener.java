package me.mage.bot.backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.mage.bot.util.WLogger;

public abstract class SocketListener implements Runnable{
	
	protected BufferedReader in;
	private Socket connection;
	
	private static Logger logger = Logger.getLogger(SocketListener.class + "");
	
	public SocketListener(Socket conn) {
		connection = conn;
		try {
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		} catch (IOException e) {
			logger.log(Level.SEVERE, "There was an issue openning the input stream", e);
			WLogger.logError(e);
		}
	}

	public void stop() {
		try {
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, "There was a problem closing the connection", e);
			WLogger.logError(e);
		}
	}

}
