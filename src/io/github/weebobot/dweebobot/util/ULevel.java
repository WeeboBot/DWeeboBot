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

package io.github.weebobot.dweebobot.util;

public enum ULevel {
	Normal("Normal"),Follower("Follower"),Subscriber("Subscriber"),Regular("Regular"),Moderator("Moderator"),Owner("Owner");
	
	private String name;
	
	ULevel(String n){
		name=n;
	}
	/**
	 * @param level - User Level as String
	 * @return User Level Object
	 */
	public static ULevel getTypeFromString(String level) {
		level=level.toLowerCase();
		if(level.endsWith("s")) {
			level=level.substring(0, level.length()-1);
		}
		switch(level.toLowerCase()) {
		case "normal":
		case "everyone":
		case "all":
			return Normal;
		case "follower":
			return Follower;
		case "regular":
		case "reg":
			return Regular;
		case "subscriber":
		case "sub":
			return Subscriber;
		case "moderator":
		case "mod":
			return Moderator;
		default :
			return Normal;
		}
	}
	
	public String getName(){
		return name;
	}
}
