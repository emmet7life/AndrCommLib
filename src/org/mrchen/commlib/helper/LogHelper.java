package org.mrchen.commlib.helper;

import org.mrchen.commlib.config.DevConfig;

import android.util.Log;

public class LogHelper {
	
	public static void d(String tag, String msg){
		if(DevConfig.isDebug()){
			Log.d(tag, msg);
		}
	}
	
	public static void e(String tag, String msg){
		if(DevConfig.isDebug()){
			Log.e(tag, msg);
		}
	}
	
	public static void i(String tag, String msg){
		if(DevConfig.isDebug()){
			Log.i(tag, msg);
		}
	}
	
	public static void v(String tag, String msg){
		if(DevConfig.isDebug()){
			Log.v(tag, msg);
		}
	}
	
	public static void w(String tag, String msg){
		if(DevConfig.isDebug()){
			Log.w(tag, msg);
		}
	}
	
}
