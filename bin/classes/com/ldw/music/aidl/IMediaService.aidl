/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.aidl;
import com.ldw.music.model.MusicInfo;
import android.graphics.Bitmap;

interface IMediaService {
    boolean play(int pos);
    boolean playById(int id);
    boolean rePlay();
	boolean pause();
	boolean prev();
	boolean next();
	int duration();
    int position();
    boolean seekTo(int progress);
    void refreshMusicList(in List<MusicInfo> musicList);
    void getMusicList(out List<MusicInfo> musicList);
    
    int getPlayState();
    int getPlayMode();
    void setPlayMode(int mode);
    void sendPlayStateBrocast();
    void exit();
    int getCurMusicId();
    void updateNotification(in Bitmap bitmap, String title, String name);
    void cancelNotification();
    MusicInfo getCurMusic();
}