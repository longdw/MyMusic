/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.activity;

/**
 * 常量
 * @author longdw(longdawei1988@gmail.com)
 *
 */
public interface IConstants {
	
	public static final String BROADCAST_NAME = "com.ldw.music.broadcast";
	public static final String SERVICE_NAME = "com.ldw.music.service.MediaService";
	public static final String BROADCAST_QUERY_COMPLETE_NAME = "com.ldw.music.querycomplete.broadcast";
	public static final String BROADCAST_CHANGEBG = "com.ldw.music.changebg";
	public static final String BROADCAST_SHAKE = "com.ldw.music.shake";
	
	//是否开启了振动模式
	public static final String SHAKE_ON_OFF = "SHAKE_ON_OFF";
	
	public static final String SP_NAME = "com.ldw.music_preference";
	public static final String SP_BG_PATH = "bg_path";
	public static final String SP_SHAKE_CHANGE_SONG = "shake_change_song";
	public static final String SP_AUTO_DOWNLOAD_LYRIC = "auto_download_lyric";
	public static final String SP_FILTER_SIZE = "filter_size";
	public static final String SP_FILTER_TIME = "filter_time";
	
	public final static int REFRESH_PROGRESS_EVENT = 0x100;

	// 播放状态
	public static final int MPS_NOFILE = -1; // 无音乐文件
	public static final int MPS_INVALID = 0; // 当前音乐文件无效
	public static final int MPS_PREPARE = 1; // 准备就绪
	public static final int MPS_PLAYING = 2; // 播放中
	public static final int MPS_PAUSE = 3; // 暂停

	// 播放模式
	public static final int MPM_LIST_LOOP_PLAY = 0; // 列表循环
	public static final int MPM_ORDER_PLAY = 1; // 顺序播放
	public static final int MPM_RANDOM_PLAY = 2; // 随机播放
	public static final int MPM_SINGLE_LOOP_PLAY = 3; // 单曲循环
	
	public static final String PLAY_STATE_NAME = "PLAY_STATE_NAME";
	public static final String PLAY_MUSIC_INDEX = "PLAY_MUSIC_INDEX";
	
	//歌手和专辑列表点击都会进入MyMusic 此时要传递参数表明是从哪里进入的
	public static final String FROM = "from";
	public static final int START_FROM_ARTIST = 1;
	public static final int START_FROM_ALBUM = 2;
	public static final int START_FROM_LOCAL = 3;
	public static final int START_FROM_FOLDER = 4;
	public static final int START_FROM_FAVORITE = 5;
	
	public static final int FOLDER_TO_MYMUSIC = 6;
	public static final int ALBUM_TO_MYMUSIC = 7;
	public static final int ARTIST_TO_MYMUSIC = 8;
	
	public static final int MENU_BACKGROUND = 9;
	
}
