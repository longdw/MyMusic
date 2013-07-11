/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.Log;

import com.ldw.music.activity.IConstants;
import com.ldw.music.model.MusicInfo;
import com.ldw.music.utils.MusicUtils;

/**
 * 歌曲控制
 * @author longdw(longdawei1988@gmail.com)
 *
 */
public class MusicControl implements IConstants, OnCompletionListener {
	
	private String TAG = MusicControl.class.getSimpleName();
	private MediaPlayer mMediaPlayer;
	private int mPlayMode;
	private List<MusicInfo> mMusicList = new ArrayList<MusicInfo>();
	private int mPlayState;
	private int mCurPlayIndex;
	private Context mContext;
	private Random mRandom;
	private int mCurMusicId;
	private MusicInfo mCurMusic;
	
	
	public MusicControl(Context context) {
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setOnCompletionListener(this);
		mPlayMode = MPM_LIST_LOOP_PLAY;
		mPlayState = MPS_NOFILE;
		mCurPlayIndex = -1;
		mCurMusicId = -1;
		this.mContext = context;
		mRandom = new Random();
		mRandom.setSeed(System.currentTimeMillis());
	}
	
	public boolean play(int pos) {
//		if(mPlayState == MPS_NOFILE || mPlayState == MPS_INVALID) {
//			return false;
//		}
		if(mCurPlayIndex == pos) {
			if(!mMediaPlayer.isPlaying()) {
				mMediaPlayer.start();
				mPlayState = MPS_PLAYING;
				sendBroadCast();
			} else {
				pause();
			}
			return true;
		}
		if(!prepare(pos)) {
			return false;
		}
		return replay();
	}
	
	/**
	 * 根据歌曲的id来播放
	 * @param id
	 * @return
	 */
	public boolean playById(int id) {
		
		int position = MusicUtils.seekPosInListById(mMusicList, id);
		mCurPlayIndex = position;
		if(mCurMusicId == id) {
			if(!mMediaPlayer.isPlaying()) {
				mMediaPlayer.start();
				mPlayState = MPS_PLAYING;
				sendBroadCast();
			} else {
				pause();
			}
			return true;
		}
		
		
		if(!prepare(position)) {
			return false;
		}
		return replay();
	}
	
	public boolean replay() {
		if(mPlayState == MPS_INVALID || mPlayState == MPS_NOFILE) {
			return false;
		}
			
		mMediaPlayer.start();
		mPlayState = MPS_PLAYING;
		sendBroadCast();
		return true;
	}
	
	public boolean pause() {
		if(mPlayState != MPS_PLAYING) {
			return false;
		}
		mMediaPlayer.pause();
		mPlayState = MPS_PAUSE;
		sendBroadCast();
		return true;
	}
	
	public boolean prev() {
		if(mPlayState == MPS_NOFILE) {
			return false;
		}
		mCurPlayIndex--;
		mCurPlayIndex = reviseIndex(mCurPlayIndex);
		if(!prepare(mCurPlayIndex)) {
			return false;
		}
		return replay();
	}
	
	public boolean next() {
		if(mPlayState == MPS_NOFILE) {
			return false;
		}
		mCurPlayIndex++;
		mCurPlayIndex = reviseIndex(mCurPlayIndex);
		if(!prepare(mCurPlayIndex)) {
			return false;
		}
		return replay();
	}
	
	private int reviseIndex(int index) {
		if(index < 0) {
			index = mMusicList.size() - 1;
		}
		if(index >= mMusicList.size()) {
			index = 0;
		}
		return index;
	}
	
	public int position() {
		if(mPlayState == MPS_PLAYING || mPlayState == MPS_PAUSE) {
			return mMediaPlayer.getCurrentPosition();
		}
		return 0;
	}
	
	/**
	 * 毫秒
	 * @return
	 */
	public int duration() {
		if(mPlayState == MPS_INVALID || mPlayState == MPS_NOFILE) {
			return 0;
		}
		return mMediaPlayer.getDuration();
	}
	
	public boolean seekTo(int progress) {
		if(mPlayState == MPS_INVALID || mPlayState == MPS_NOFILE) {
			return false;
		}
		int pro = reviseSeekValue(progress);
		int time = mMediaPlayer.getDuration();
		int curTime = (int)((float)pro / 100 * time);
		mMediaPlayer.seekTo(curTime);
		return true;
	}
	
	private int reviseSeekValue(int progress) {
		if(progress < 0) {
			progress = 0;
		} else if(progress > 100) {
			progress = 100;
		}
		return progress;
	}
	
	public void refreshMusicList(List<MusicInfo> musicList) {
		mMusicList.clear();
		mMusicList.addAll(musicList);
		if(mMusicList.size() == 0) {
			mPlayState = MPS_NOFILE;
			mCurPlayIndex = -1;
			return;
		}
//		sendBroadCast();
//		switch(mPlayState) {
//		case MPS_INVALID:
//		case MPS_NOFILE:
//		case MPS_PREPARE:
//			prepare(0);
//			break;
//		}
	}
	
	private boolean prepare(int pos) {
		mCurPlayIndex = pos;
		mMediaPlayer.reset();
		String path = mMusicList.get(pos).data;
		try {
			mMediaPlayer.setDataSource(path);
			mMediaPlayer.prepare();
			mPlayState = MPS_PREPARE;
		} catch (Exception e) {
			Log.e(TAG, "", e);
			mPlayState = MPS_INVALID;
			if(pos < mMusicList.size()) {
				pos++;
				playById(mMusicList.get(pos).songId);
			}
//			sendBroadCast();
			return false;
		}
		sendBroadCast();
		return true;
	}
	
	public void sendBroadCast() {
		Intent intent = new Intent(BROADCAST_NAME);
		intent.putExtra(PLAY_STATE_NAME, mPlayState);
		intent.putExtra(PLAY_MUSIC_INDEX, mCurPlayIndex);
		intent.putExtra("music_num", mMusicList.size());
		if(mPlayState != MPS_NOFILE && mMusicList.size() > 0) {
			Bundle bundle = new Bundle();
			mCurMusic = mMusicList.get(mCurPlayIndex);
			mCurMusicId = mCurMusic.songId;
			bundle.putParcelable(MusicInfo.KEY_MUSIC, mCurMusic);
			intent.putExtra(MusicInfo.KEY_MUSIC, bundle);
		}
		mContext.sendBroadcast(intent);
	}
	
	public int getCurMusicId() {
		return mCurMusicId;
	}
	
	public MusicInfo getCurMusic() {
		return mCurMusic;
	}
	
	public int getPlayState() {
		return mPlayState;
	}
	
	public int getPlayMode() {
		return mPlayMode;
	}
	
	public void setPlayMode(int mode) {
		switch(mode) {
		case MPM_LIST_LOOP_PLAY:
		case MPM_ORDER_PLAY:
		case MPM_RANDOM_PLAY:
		case MPM_SINGLE_LOOP_PLAY:
			mPlayMode = mode;
			break;
		}
	}
	
	public List<MusicInfo> getMusicList() {
		return mMusicList;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		switch(mPlayMode) {
		case MPM_LIST_LOOP_PLAY:
			next();
			break;
		case MPM_ORDER_PLAY:
			if(mCurPlayIndex != mMusicList.size() - 1) {
				next();
			} else {
				prepare(mCurPlayIndex);
			}
			break;
		case MPM_RANDOM_PLAY:
			int index = getRandomIndex();
			if(index != -1) {
				mCurPlayIndex = index;
			} else {
				mCurPlayIndex = 0;
			}
			if(prepare(mCurPlayIndex)) {
				replay();
			}
			break;
		case MPM_SINGLE_LOOP_PLAY:
			play(mCurPlayIndex);
			break;
		}
	}
	
	private int getRandomIndex() {
		int size = mMusicList.size();
		if(size == 0) {
			return -1;
		}
		return Math.abs(mRandom.nextInt() % size);
	}
	
	public void exit() {
		mMediaPlayer.stop();
		mMediaPlayer.release();
		mCurPlayIndex = -1;
		mMusicList.clear();
	}

}
