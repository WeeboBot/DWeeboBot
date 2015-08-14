package me.mage.bot.backend;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketPingListener extends SocketListener implements Runnable{
	
	private PrintWriter out;
	
	public SocketPingListener(Socket conn) {
		super(conn);
		try {
			out = new PrintWriter(conn.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
		String message = null;
		try {
			System.out.print("Listening");
			message = in.readLine();
			System.out.print("Heard");
			if(message.equalsIgnoreCase("ping")){
				out.println("pong");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
