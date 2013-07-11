package com.ldw.music.fragment;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Albums;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.ldw.music.R;
import com.ldw.music.activity.IConstants;
import com.ldw.music.activity.SlidingDrawerManager;
import com.ldw.music.activity.UIManager;
import com.ldw.music.adapter.MyAdapter;
import com.ldw.music.db.AlbumInfoDao;
import com.ldw.music.db.ArtistInfoDao;
import com.ldw.music.db.MusicInfoDao;
import com.ldw.music.interfaces.IOnServiceConnectComplete;
import com.ldw.music.interfaces.IQueryFinished;
import com.ldw.music.model.AlbumInfo;
import com.ldw.music.model.ArtistInfo;
import com.ldw.music.model.MusicInfo;
import com.ldw.music.service.ServiceManager;
import com.ldw.music.utils.MusicTimer;
import com.ldw.music.utils.MusicUtils;

public class MyMusicFragment extends Fragment implements
		IOnServiceConnectComplete, IConstants, OnTouchListener, IQueryFinished {

	private MyAdapter mAdapter;
	private ListView mListView;
	private ServiceManager mServiceManager = null;
	private SlidingDrawerManager mSdm;
	private UIManager mUIm;
	private MusicTimer mMusicTimer;
	private MusicPlayBroadcast mPlayBroadcast;

	private RelativeLayout mBottomLayout, mMainLayout;
	private Bitmap defaultArtwork;

	// 歌曲信息数据库
	private MusicInfoDao mMusicInfoDao;
	//专辑信息数据库
	private AlbumInfoDao mAlbumInfoDao;
	//歌手信息数据库
	private ArtistInfoDao mArtistInfoDao;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		defaultArtwork = BitmapFactory.decodeResource(getResources(),
				R.drawable.img_album_background);
		mMusicInfoDao = new MusicInfoDao(getActivity());
		mAlbumInfoDao = new AlbumInfoDao(getActivity());
		mArtistInfoDao = new ArtistInfoDao(getActivity());
	}

	// MusicControl 118行 退出时报错
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.layout_content1,
				container, false);

		mMainLayout = (RelativeLayout) view.findViewById(R.id.mainLayout);
		mBottomLayout = (RelativeLayout) view.findViewById(R.id.bottomLayout);
		mMainLayout.setOnTouchListener(this);

		mListView = (ListView) view.findViewById(R.id.music_listview);

		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

		mPlayBroadcast = new MusicPlayBroadcast();
		IntentFilter filter = new IntentFilter(BROADCAST_NAME);
		getActivity().registerReceiver(mPlayBroadcast, filter);

		mServiceManager = new ServiceManager(getActivity());
		initListView();
		mServiceManager.connectService();
		mServiceManager.setOnServiceConnectComplete(this);

		mSdm = new SlidingDrawerManager(getActivity(), mServiceManager, view);
		mUIm = new UIManager(getActivity(), mServiceManager, view);
		mMusicTimer = new MusicTimer(mSdm.mHandler, mUIm.mHandler);

		return view;
	}

	private void initListView() {
		mAdapter = new MyAdapter(getActivity(), mServiceManager);
		mListView.setAdapter(mAdapter);
		mAdapter.setQueryFinished(this);

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				mServiceManager
						.playById(mAdapter.getData().get(position).songId);
			}
		});
	}

	private class MusicPlayBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BROADCAST_NAME)) {
				MusicInfo music = new MusicInfo();
				int playState = intent.getIntExtra(PLAY_STATE_NAME, MPS_NOFILE);
				int curPlayIndex = intent.getIntExtra(PLAY_MUSIC_INDEX, -1);
				Bundle bundle = intent.getBundleExtra(MusicInfo.KEY_MUSIC);
				if (bundle != null) {
					music = bundle.getParcelable(MusicInfo.KEY_MUSIC);
				}

				switch (playState) {
				case MPS_INVALID:// 考虑后面加上如果文件不可播放直接跳到下一首
					mMusicTimer.stopTimer();
					mSdm.refreshUI(0, music.duration, music);
					mSdm.showPlay(true);

					mUIm.refreshUI(0, music.duration, music);
					mUIm.showPlay(true);
					mServiceManager.next();
					break;
				case MPS_PAUSE:
					mMusicTimer.stopTimer();
					mSdm.refreshUI(mServiceManager.position(), music.duration,
							music);
					mSdm.showPlay(true);

					mUIm.refreshUI(mServiceManager.position(), music.duration,
							music);
					mUIm.showPlay(true);

					mServiceManager.cancelNotification();
					break;
				case MPS_PLAYING:
					mMusicTimer.startTimer();
					mSdm.refreshUI(mServiceManager.position(), music.duration,
							music);
					mSdm.showPlay(false);

					mUIm.refreshUI(mServiceManager.position(), music.duration,
							music);
					mUIm.showPlay(false);
					mSdm.getMusicLrc(music);

					Bitmap bitmap = MusicUtils.getCachedArtwork(getActivity(),
							music.albumId, defaultArtwork);
					// Bitmap bitmap = MusicUtils.getArtwork(getActivity(),
					// music._id, music.albumId);
					// 更新顶部notification
					mServiceManager.updateNotification(bitmap, music.musicName,
							music.artist);

					break;
				case MPS_PREPARE:
					mMusicTimer.stopTimer();
					mSdm.refreshUI(0, music.duration, music);
					mSdm.showPlay(true);

					mUIm.refreshUI(0, music.duration, music);
					mUIm.showPlay(true);
					break;
				}
				mAdapter.setPlayState(playState, curPlayIndex);
				// mSdm.refreshUI(0, music.duration, music);
			}
		}
	}

	@Override
	public void onServiceConnectComplete() {
		boolean has = mMusicInfoDao.hasData();
		if (has) {
			List<MusicInfo> list = mMusicInfoDao.getMusicInfo();
			mAdapter.setData(list);
		} else {
			// service绑定成功会执行到这里
			MusicUtils.queryMusic(mAdapter.getQueryHandler());
			List<AlbumInfo> albumList = MusicUtils.queryAlbums(getActivity());
			List<ArtistInfo> artistList = MusicUtils.queryArtist(getActivity());
			mAlbumInfoDao.saveAlbumInfo(albumList);
			mArtistInfoDao.saveArtistInfo(artistList);
			
			Intent intent = new Intent(BROADCAST_QUERY_COMPLETE_NAME);
			intent.putExtra("album_num", albumList.size());
			intent.putExtra("artist_num", artistList.size());
			getActivity().sendBroadcast(intent);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMusicTimer.stopTimer();
		mServiceManager.exit();
		MusicUtils.clearCache();
		getActivity().unregisterReceiver(mPlayBroadcast);
	}

	int oldY = 0;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int bottomTop = mBottomLayout.getTop();
		System.out.println(bottomTop);
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			oldY = (int) event.getY();
			if (oldY > bottomTop) {
				mSdm.open();
			}
		}
		return true;
	}

	@Override
	public void onFinished(List<MusicInfo> list) {
		mMusicInfoDao.saveMusicInfo(list);
	}
}
