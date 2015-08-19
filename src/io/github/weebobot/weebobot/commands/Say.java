/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.weebobot.weebobot.commands;

import io.github.weebobot.weebobot.Main;
import io.github.weebobot.weebobot.util.CLevel;

/**
 *
 * @author Hazard
 */
public class Say extends Command{
        @Override
	public CLevel getCommandLevel() {
		return CLevel.Mod;
	}
	
	@Override
	public String getCommandText() {
		return "say";
	}
	
	@Override
	public String execute(String channel, String sender, String...parameters) {
		if(channel.equalsIgnoreCase(Main.getBotChannel())) {
			StringBuilder sb = new StringBuilder();
                        for(int i = 0; i < parameters.length-1; i++){
                            sb.append(parameters[i] + " ");  
                        }
                        
                Main.getBot().sendMessage(parameters[parameters.length - 1], sb.toString());
                        
		}
		return null;
	}
}
