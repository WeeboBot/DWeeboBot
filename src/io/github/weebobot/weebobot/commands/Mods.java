/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.weebobot.weebobot.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.github.weebobot.weebobot.database.Database;
import io.github.weebobot.weebobot.util.CLevel;

public class Mods extends Command{
	
	private static final Logger logger = Logger.getLogger(Mods.class + "");
    
    @Override
	public CLevel getCommandLevel() {
		return CLevel.Normal;
	}
	
	@Override
	public String getCommandText() {
		return "mods";
	}
	
	@Override
	public String execute(String channel, String sender, String... parameters) {
		StringBuilder sb = new StringBuilder();
		ResultSet rs=Database.getMods(channel.substring(1));
		try {
			while(rs.next()){
				sb.append(rs.getString(1) + ", ");
			}
		} catch (SQLException e) {
			logger.log(Level.WARNING, "There was an issue getting the current mods in the chat", e);
		}
        return "The present mods of this channel are " + sb.toString().substring(0, sb.toString().length()-1);
            
	}
    
}
