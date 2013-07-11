/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music;

import java.io.File;

import com.ldw.music.service.ServiceManager;

import android.app.Application;
import android.os.Environment;

public class MusicApp extends Application {
	
	public static boolean mIsSleepClockSetting = false;
	public static ServiceManager mServiceManager = null;
	private static String rootPath = "/mymusic";
	public static String lrcPath = "/lrc";
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mServiceManager = new ServiceManager(this);
		initPath();
	}
	
	private void initPath() {
		String ROOT = "";
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			ROOT = Environment.getExternalStorageDirectory().getPath();
		}
		rootPath = ROOT + rootPath;
		lrcPath = rootPath + lrcPath;
		File lrcFile = new File(lrcPath);
		if(lrcFile.exists()) {
			lrcFile.mkdirs();
		}
	}
}
