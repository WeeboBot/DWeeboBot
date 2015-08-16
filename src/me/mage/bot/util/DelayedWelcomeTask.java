package me.mage.bot.util;

import java.util.Timer;
import java.util.TimerTask;

import me.mage.bot.Main;

public class DelayedWelcomeTask extends TimerTask {

	private static final Timer timer = new Timer();
	
	private String channel;
	private String user;
	
	/**
	 * @param channel - the channel we are delaying the welcome for
	 * @param user - the user we are delaying the welcome for
	 */
	public DelayedWelcomeTask(String channel, String user) {
		this.channel = channel;
		this.user = user;
		
		timer.schedule(this, 7200000);
	}
	
	/**
	 * allows the user to be welcomed in the channel again
	 */
	@Override
	public void run() {
		Main.getBot().removeWelcome(channel, user);
	}

}
