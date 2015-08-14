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
	
	public static int commandPort = 6668;
	
	private boolean running;
	private int listeningPort;
	private int type;
	private ArrayList<SocketCommandListener> connections;
	
	public Backend(int port){
		if(port == commandPort){
			listeningPort = port;
			running = false;
			type=0;
		}else{
			
		}
	}
	
	public void run(){
		running = true;
		connections = new ArrayList<>();
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
					connections.add(new SocketCommandListener(conn));
					break;
			}
			connections.get(connections.size()-1).run();
			
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
	}
}
