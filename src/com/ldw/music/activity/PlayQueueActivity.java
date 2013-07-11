/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ldw.music.R;
import com.ldw.music.aidl.IMediaService;
import com.ldw.music.model.MusicInfo;
import com.ldw.music.service.ServiceManager;
import com.ldw.music.utils.MusicUtils;

/**
 * 播放队列
 * @author longdw(longdawei1988@gmail.com)
 *
 */
public class PlayQueueActivity extends Activity implements OnItemClickListener, IConstants {
	
	private ListView mListView;
	private TextView mTitleTv;
	private IMediaService mService;
	private List<MusicInfo> mMusicList = new ArrayList<MusicInfo>();
	public int mPlayingSongPosition;
	private int mCurMusicId;
	private ServiceConnection mServiceConnection;
	
	private ImageView mPlayModeIv;
	private LinearLayout mPlayModeLayout;
	
	private int mCurMode;
//	private ServiceManager mServiceManager;
	private static final String modeName[] = { "列表循环", "顺序播放", "随机播放", "单曲循环" };
	private int modeDrawable[] = { R.drawable.icon_list_reapeat,
			R.drawable.icon_sequence, R.drawable.icon_shuffle,
			R.drawable.icon_single_repeat };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.playqueue);
		initView();
		
		initConnection();
	}
	
	private void initConnection() {
		mServiceConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mService = IMediaService.Stub.asInterface(service);
				if(mService != null) {
					try {
						mCurMode = mService.getPlayMode();
						mPlayModeIv.setBackgroundResource(modeDrawable[mCurMode]);
						
						mService.getMusicList(mMusicList);
						mCurMusicId = mService.getCurMusicId();
						mPlayingSongPosition = MusicUtils.seekPosInListById(mMusicList, mCurMusicId);
						initListView();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		Intent intent = new Intent("com.ldw.music.service.MediaService");
		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private void initView() {
		mListView = (ListView) findViewById(R.id.listview_play_queue);
		mTitleTv = (TextView) findViewById(R.id.title_tv);
		mListView.setOnItemClickListener(this);
		mPlayModeIv = (ImageView) findViewById(R.id.playmode_iv);
		mPlayModeLayout = (LinearLayout) findViewById(R.id.playmode_layout);
		
		mPlayModeLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					changeMode();
					mService.setPlayMode(mCurMode);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void changeMode() {
		mCurMode++;
		if (mCurMode > MPM_SINGLE_LOOP_PLAY) {
			mCurMode = MPM_LIST_LOOP_PLAY;
		}
		mPlayModeIv.setBackgroundResource(modeDrawable[mCurMode]);
	}

	private void initListView() {
		if(mMusicList.size() == 0) {
			mTitleTv.setText("播放队列(0)");
		} else {
			mTitleTv.setText("播放队列("+mMusicList.size()+")");
		}
		List<String> list = new ArrayList<String>();
		int i = 0;
		for (MusicInfo m : mMusicList) {
			i++;
			list.add(i+"."+m.musicName);
		}
		MyAdapter adapter = new MyAdapter(this, android.R.layout.simple_list_item_1, list);
		mListView.setAdapter(adapter);
		mListView.setSelection(mPlayingSongPosition);
	}

	@Override
	public void onAttachedToWindow() {
		// 设置本Activity在父窗口的位置
		super.onAttachedToWindow();
		View view = getWindow().getDecorView();
		WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view
				.getLayoutParams();
		lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
		lp.x = getResources().getDimensionPixelSize(
				R.dimen.playqueue_dialog_marginright);
		lp.y = getResources().getDimensionPixelSize(
				R.dimen.playqueue_dialog_marginbottom);
		lp.width = getResources().getDimensionPixelSize(
				R.dimen.playqueue_dialog_width);
		lp.height = getResources().getDimensionPixelSize(
				R.dimen.playqueue_dialog_height);
		getWindowManager().updateViewLayout(view, lp);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		unbindService(mServiceConnection);
		mMusicList = null;
	}
	
	private class MyAdapter extends ArrayAdapter<String> {

		public MyAdapter(Context context, int textViewResourceId,
				List<String> objects) {
			super(context, textViewResourceId, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = (TextView) super.getView(position, convertView,
					parent);
			if (position == mPlayingSongPosition) {
				// 对正在播放的歌曲设置高亮
				tv.setTextColor(getResources().getColor(
						R.color.holo_orange_dark));
			} else {
				tv.setTextColor(getResources().getColor(R.color.black));
			}
			return tv;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		try {
			mService.play(position);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		finish();
	}
}
