package me.mage.bot.backend;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.mage.bot.util.WLogger;

public class SocketPingListener extends SocketListener implements Runnable {

	private static Logger logger = Logger.getLogger(SocketPingListener.class + "");

	private PrintWriter out;

	public SocketPingListener(Socket conn) {
		super(conn);
		try {
			out = new PrintWriter(conn.getOutputStream(), true);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "There was an issue opening the outbut stream", e);
			WLogger.logError(e);
		}
	}

	public void run() {
		String message = null;
		try {
			System.out.println("Listening");
			do {
				System.out.println("Wating for message");
				message = in.readLine();
				System.out.println("Heard");
				System.out.println(message);
				Thread.sleep(500);
			} while (!message.equalsIgnoreCase("SET ping"));
			out.println("pong");
			System.out.println("Wrote");
		} catch (IOException | InterruptedException e) {
			logger.log(Level.SEVERE, "There was a problem reading from the client", e);
			WLogger.logError(e);
		}
	}

}
