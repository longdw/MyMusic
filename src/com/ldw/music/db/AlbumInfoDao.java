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

import com.ldw.music.model.AlbumInfo;

public class AlbumInfoDao {

	private static final String TABLE_ALBUM = "album_info";
	private Context mContext;
	
	public AlbumInfoDao(Context context) {
		this.mContext = context;
	}
	
	public void saveAlbumInfo(List<AlbumInfo> list) {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		for (AlbumInfo info : list) {
			ContentValues cv = new ContentValues();
			cv.put("album_name", info.album_name);
			cv.put("album_id", info.album_id);
			cv.put("number_of_songs", info.number_of_songs);
			cv.put("album_art", info.album_art);
			db.insert(TABLE_ALBUM, null, cv);
		}
	}
	
	public List<AlbumInfo> getAlbumInfo() {
		SQLiteDatabase db = DatabaseHelper.getInstance(mContext);
		List<AlbumInfo> list = new ArrayList<AlbumInfo>();
		String sql = "select * from " + TABLE_ALBUM;
		Cursor cursor = db.rawQuery(sql, null);
		while(cursor.moveToNext()) {
			AlbumInfo info = new AlbumInfo();
			info.album_name = cursor.getString(cursor.getColumnIndex("album_name"));
			info.album_art = cursor.getString(cursor.getColumnIndex("album_art"));
			info.album_id = cursor.getInt(cursor.getColumnIndex("album_id"));
			info.number_of_songs = cursor.getInt(cursor.getColumnIndex("number_of_songs"));
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
		String sql = "select count(*) from " + TABLE_ALBUM;
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
		String sql = "select count(*) from " + TABLE_ALBUM;
		Cursor cursor = db.rawQuery(sql, null);
		int count = 0;
		if(cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}
		return count;
	}
}
