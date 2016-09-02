package io.github.weebobot.dweebobot.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

public class WLogger {
	
	private static final File LOG_FILE = new File("bot.log");
	private static final File ERROR_FILE = new File("bot.err");
	
	public static void init() {
		if(!LOG_FILE.exists()) {
			try {
				LOG_FILE.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!ERROR_FILE.exists()) {
			try {
				ERROR_FILE.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void log(String info) {
		TFileWriter.writeFile(LOG_FILE, info);
	}
	
	public static void logError(Level errorLevel, String errorMsg) {
		TFileWriter.writeFile(ERROR_FILE, String.format("%s: %s:\n%s", new Date().toString(), errorLevel.toString(), errorMsg));
	}
	
	public static void logError(Exception e) {
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement stackTraceElement : e.getStackTrace()) {
			sb.append("\t");
			sb.append(stackTraceElement.toString());
			sb.append("\n");
		}
		sb.append("----------");
		TFileWriter.writeFile(ERROR_FILE, String.format("%s: %s:\n%s", new Date().toString(), e.toString(), sb.toString()));
	}
}
