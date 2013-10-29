package org.mrchen.commlib.config;

public class DevConfig {
	
	public static final boolean DEBUG_ON = true;
	public static final boolean DEBUG_OFF = false;
	
	/**
	 * 开发者调试模式
	 */
	public static boolean debug = DEBUG_ON;
	
	public static boolean isDebug(){
		return debug;
	}
	
	public static void setDebug(boolean devDebug){
		debug = devDebug;
	}
	
}
