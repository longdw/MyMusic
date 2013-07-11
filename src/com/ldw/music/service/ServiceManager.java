/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.service;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;

import com.ldw.music.activity.IConstants;
import com.ldw.music.aidl.IMediaService;
import com.ldw.music.interfaces.IOnServiceConnectComplete;
import com.ldw.music.model.MusicInfo;

/**
 * 控制Service
 * @author longdw(longdawei1988@gmail.com)
 *
 */
public class ServiceManager implements IConstants {

	public IMediaService mService;
	private Context mContext;
	private ServiceConnection mConn;
	private IOnServiceConnectComplete mIOnServiceConnectComplete;

	public ServiceManager(Context context) {
		this.mContext = context;
		initConn();
	}

	private void initConn() {
		mConn = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mService = IMediaService.Stub.asInterface(service);
				if (mService != null) {
					mIOnServiceConnectComplete.onServiceConnectComplete(mService);
				}
			}
		};
	}

	public void connectService() {
		Intent intent = new Intent("com.ldw.music.service.MediaService");
		mContext.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
	}
	
	public void disConnectService() {
		mContext.unbindService(mConn);
		mContext.stopService(new Intent("com.ldw.music.service.MediaService"));
	}
	
	public void refreshMusicList(List<MusicInfo> musicList) {
		if(musicList != null && mService != null) {
			try {
				mService.refreshMusicList(musicList);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<MusicInfo> getMusicList() {
		List<MusicInfo> musicList = new ArrayList<MusicInfo>();
		try {
			if(mService != null) {
				mService.getMusicList(musicList);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return musicList;
	}
	
	public boolean play(int pos) {
		if(mService != null) {
			try {
				return mService.play(pos);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean playById(int id) {
		if(mService != null) {
			try {
				return mService.playById(id);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean rePlay() {
		if(mService != null) {
			try {
				return mService.rePlay();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean pause() {
		if(mService != null) {
			try {
				return mService.pause();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean prev() {
		if(mService != null) {
			try {
				return mService.prev();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean next() {
		if(mService != null) {
			try {
				return mService.next();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public boolean seekTo(int progress) {
		if(mService != null) {
			try {
				return mService.seekTo(progress);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public int position() {
		if(mService != null) {
			try {
				return mService.position();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	public int duration() {
		if(mService != null) {
			try {
				return mService.duration();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	public int getPlayState() {
		if(mService != null) {
			try {
				int mode = mService.getPlayState();
				return mService.getPlayState();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	public void setPlayMode(int mode) {
		if(mService != null) {
			try {
				mService.setPlayMode(mode);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	public int getPlayMode() {
		if(mService != null) {
			try {
				return mService.getPlayMode();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	public int getCurMusicId() {
		if(mService != null) {
			try {
				return mService.getCurMusicId();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return -1;
	}
	
	public MusicInfo getCurMusic() {
		if(mService != null) {
			try {
				return mService.getCurMusic();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public void sendBroadcast() {
		if(mService != null) {
			try {
				mService.sendPlayStateBrocast();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void exit() {
		if(mService != null) {
			try {
				mService.exit();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		mContext.unbindService(mConn);
		mContext.stopService(new Intent(SERVICE_NAME));
	}
	
	public void updateNotification(Bitmap bitmap, String title, String name) {
		try {
			mService.updateNotification(bitmap, title, name);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void cancelNotification() {
		try {
			mService.cancelNotification();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void setOnServiceConnectComplete(
			IOnServiceConnectComplete IServiceConnect) {
		mIOnServiceConnectComplete = IServiceConnect;
	}

}
