/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ldw.music.model.FolderInfo;

public class FolderInfoDao {

	private static final String TABLE_FOLDER = "folder_info";
	private Context mContext;
	
	public FolderInfoDao(Context context) {
		this.mContext = context;
	}
	
	public void saveFolderInfo(List<FolderInfo> list) {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		for (FolderInfo info : list) {
			ContentValues cv = new ContentValues();
			cv.put("folder_name", info.folder_name);
			cv.put("folder_path", info.folder_path);
			db.insert(TABLE_FOLDER, null, cv);
		}
	}
	
	public List<FolderInfo> getFolderInfo() {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		List<FolderInfo> list = new ArrayList<FolderInfo>();
		String sql = "select * from " + TABLE_FOLDER;
		Cursor cursor = db.rawQuery(sql, null);
		while(cursor.moveToNext()) {
			FolderInfo info = new FolderInfo();
			info.folder_name = cursor.getString(cursor.getColumnIndex("folder_name"));
			info.folder_path = cursor.getString(cursor.getColumnIndex("folder_path"));
			list.add(info);
		}
		cursor.close();
		return list;
	}
	
	/**
	 * 数据库中是否有数据
	 * @return
	 */
	public boolean hasData() {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		String sql = "select count(*) from " + TABLE_FOLDER;
		Cursor cursor = db.rawQuery(sql, null);
		boolean has = false;
		if(cursor.moveToFirst()) {
			int count = cursor.getInt(0);
			if(count > 0) {
				has = true;
			}
		}
		cursor.close();
		return has;
	}
	
	public int getDataCount() {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		String sql = "select count(*) from " + TABLE_FOLDER;
		Cursor cursor = db.rawQuery(sql, null);
		int count = 0;
		if(cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}
		return count;
	}
}
