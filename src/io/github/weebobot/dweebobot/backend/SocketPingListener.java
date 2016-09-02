package io.github.weebobot.dweebobot.backend;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.weebobot.dweebobot.util.WLogger;

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
			do {
				message = in.readLine();
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
