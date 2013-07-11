/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.storage;

import com.ldw.music.activity.IConstants;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

@SuppressLint({ "WorldWriteableFiles", "CommitPrefEdits" })
public class SPStorage implements IConstants {
	
	private SharedPreferences mSp;
	private Editor mEditor;
	
	public SPStorage(Context context) {
		mSp = context.getSharedPreferences(SP_NAME,
				Context.MODE_WORLD_WRITEABLE);
		mEditor = mSp.edit();
	}
	
	/**
	 * 保存背景图片的地址
	 */
	public void savePath(String path) {
		mEditor.putString(SP_BG_PATH, path);
		mEditor.commit();
	}
	
	/**
	 * 获取背景图片的地址
	 * @return
	 */
	public String getPath() {
		return mSp.getString(SP_BG_PATH, null);
	}
	
	public void saveShake(boolean shake) {
		mEditor.putBoolean(SP_SHAKE_CHANGE_SONG, shake);
		mEditor.commit();
	}
	
	public boolean getShake() {
		return mSp.getBoolean(SP_SHAKE_CHANGE_SONG, false);
	}
	
	public void saveAutoLyric(boolean auto) {
		mEditor.putBoolean(SP_AUTO_DOWNLOAD_LYRIC, auto);
		mEditor.commit();
	}
	
	public boolean getAutoLyric() {
		return mSp.getBoolean(SP_AUTO_DOWNLOAD_LYRIC, false);
	}
	
	public void saveFilterSize(boolean size) {
		mEditor.putBoolean(SP_FILTER_SIZE, size);
		mEditor.commit();
	}
	
	public boolean getFilterSize() {
		return mSp.getBoolean(SP_FILTER_SIZE, false);
	}
	
	public void saveFilterTime(boolean time) {
		mEditor.putBoolean(SP_FILTER_TIME, time);
		mEditor.commit();
	}
	
	public boolean getFilterTime() {
		return mSp.getBoolean(SP_FILTER_TIME, false);
	}

}
