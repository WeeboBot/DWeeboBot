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
			System.out.println("Listening");
			do{
				System.out.println("Wating for message");
				message = in.readLine();
				System.out.println("Heard");
				System.out.println(message);
				Thread.sleep(500);
			}while(!message.equalsIgnoreCase("SET ping"));
			out.println("pong");
			System.out.println("Wrote");
		} catch (IOException | InterruptedException e1) {
			e1.printStackTrace();
		}
	}

}
