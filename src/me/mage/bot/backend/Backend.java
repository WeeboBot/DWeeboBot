/*	  It's a Twitch bot, because we can.
 *    Copyright (C) 2015  Timothy Chandler, James Wolff
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package me.mage.bot.backend;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Backend implements Runnable{
	
	public static int pingPort =6668;
	public static int commandPort = 6669;
	
	private boolean running;
	private int listeningPort;
	private int type;
	private ArrayList<SocketListener> connections;
	private ArrayList<Thread> threads;
	
	public Backend(int port){
		if(port == pingPort){
			listeningPort = port;
			running = false;
			type=0;
		}else if(port == commandPort){
			listeningPort = port;
			running = false;
			type=1;
		}
		connections = new ArrayList<>();
		threads = new ArrayList<>();
	}
	
	public void run(){
		running = true;
		ServerSocket server = null;
		Socket conn = null;
		try {
			server = new ServerSocket(listeningPort);
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(running){
			try {
				conn = server.accept();
			} catch (IOException e){
				e.printStackTrace();
			}
			switch(type){
				case 0:
					connections.add(new SocketPingListener(conn));
					break;
 				case 1:
					connections.add(new SocketCommandListener(conn));
					break;
			}
			threads.add(new Thread(connections.get(connections.size()-1)));
			threads.get(threads.size()-1).start();
			
		}
	}
	
	public void stop(){
		running = false;
		try {
			Socket s = new Socket("localhost", listeningPort);
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(SocketListener s: connections){
			s.stop();
		}
	}
}
