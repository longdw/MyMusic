/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ldw.music.R;
import com.ldw.music.activity.IConstants;
import com.ldw.music.db.FavoriteInfoDao;
import com.ldw.music.db.MusicInfoDao;
import com.ldw.music.interfaces.IQueryFinished;
import com.ldw.music.model.MusicInfo;
import com.ldw.music.service.ServiceManager;
import com.ldw.music.uimanager.SlidingDrawerManager;
import com.ldw.music.utils.MusicUtils;
import com.ldw.music.utils.StringHelper;

public class MyAdapter extends BaseAdapter implements IConstants {

	private LayoutInflater mLayoutInflater;
	private ArrayList<MusicInfo> mMusicList;
	private ServiceManager mServiceManager;
	private SlidingDrawerManager mSdm;

	private int mPlayState, mCurPlayMusicIndex = -1;
//	private IQueryFinished mIQueryFinished;
	private FavoriteInfoDao mFavoriteDao;
	private MusicInfoDao mMusicDao;
	private int mFrom;

	class ViewHolder {
		TextView musicNameTv, artistTv, durationTv;
		ImageView playStateIconIv, favoriteIv;
	}

	public MyAdapter(Context context, ServiceManager sm, SlidingDrawerManager sdm) {
		mLayoutInflater = LayoutInflater.from(context);
		mMusicList = new ArrayList<MusicInfo>();
		this.mServiceManager = sm;
		this.mSdm = sdm;
		mFavoriteDao = new FavoriteInfoDao(context);
		mMusicDao = new MusicInfoDao(context);
	}
	
	public void setData(List<MusicInfo> list, int from) {
		setData(list);
		this.mFrom = from;
	}

	/**
	 * 当数据库中有数据的时候会调用该方法来更新列表
	 * 
	 * @param list
	 */
	public void setData(List<MusicInfo> list) {
		mMusicList.clear();
		if (list != null && list.size() > 0) {
			mMusicList.addAll(list);
			// 为list排序
			Collections.sort(mMusicList, comparator);
			notifyDataSetChanged();
		}
	}
	
	public void refreshPlayingList() {
		if(mMusicList.size() > 0) {
			mServiceManager.refreshMusicList(mMusicList);
		}
	}
	
	public void refreshFavoriteById(int id, int favorite) {
		int position = MusicUtils.seekPosInListById(mMusicList, id);
		mMusicList.get(position).favorite = favorite;
		notifyDataSetChanged();
	}

	public List<MusicInfo> getData() {
		return mMusicList;
	}

	public void setQueryFinished(IQueryFinished finish) {
//		mIQueryFinished = finish;
	}

	Comparator<MusicInfo> comparator = new Comparator<MusicInfo>() {

		char first_l, first_r;

		@Override
		public int compare(MusicInfo lhs, MusicInfo rhs) {
			first_l = lhs.musicName.charAt(0);
			first_r = rhs.musicName.charAt(0);
			if (StringHelper.checkType(first_l) == StringHelper.CharType.CHINESE) {
				first_l = StringHelper.getPinyinFirstLetter(first_l);
			}
			if (StringHelper.checkType(first_r) == StringHelper.CharType.CHINESE) {
				first_r = StringHelper.getPinyinFirstLetter(first_r);
			}
			if (first_l > first_r) {
				return 1;
			} else if (first_l < first_r) {
				return -1;
			} else {
				return 0;
			}
		}
	};

	public void setPlayState(int playState, int playIndex) {
		mPlayState = playState;
		mCurPlayMusicIndex = playIndex;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mMusicList.size();
	}

	@Override
	public MusicInfo getItem(int position) {
		return mMusicList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		final MusicInfo music = getItem(position);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mLayoutInflater
					.inflate(R.layout.musiclist_item, null);
			viewHolder.musicNameTv = (TextView) convertView
					.findViewById(R.id.musicname_tv);
			viewHolder.artistTv = (TextView) convertView
					.findViewById(R.id.artist_tv);
			viewHolder.durationTv = (TextView) convertView
					.findViewById(R.id.duration_tv);
			viewHolder.playStateIconIv = (ImageView) convertView
					.findViewById(R.id.playstate_iv);
			viewHolder.favoriteIv = (ImageView) convertView
					.findViewById(R.id.favorite_iv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (position != mCurPlayMusicIndex) {
			viewHolder.playStateIconIv.setVisibility(View.GONE);
		} else {
			viewHolder.playStateIconIv.setVisibility(View.VISIBLE);
			if (mPlayState == MPS_PAUSE) {
				viewHolder.playStateIconIv
						.setBackgroundResource(R.drawable.list_pause_state);
			} else {
				viewHolder.playStateIconIv
						.setBackgroundResource(R.drawable.list_play_state);
			}
		}
		
		viewHolder.favoriteIv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(music.favorite == 1) {
					if(mFrom == START_FROM_FAVORITE) {
						mMusicList.remove(position);
						notifyDataSetChanged();
					}
//					music.favorite = 0;
					mFavoriteDao.deleteById(music._id);
					mMusicDao.setFavoriteStateById(music._id, 0);
					viewHolder.favoriteIv.setImageResource(R.drawable.icon_favourite_normal);
					mMusicList.get(position).favorite = 0;
					mSdm.refreshFavorite(0);
				} else {
//					music.favorite = 1;
					mFavoriteDao.saveMusicInfo(music);
					mMusicDao.setFavoriteStateById(music._id, 1);
					viewHolder.favoriteIv.setImageResource(R.drawable.icon_favourite_checked);
					mMusicList.get(position).favorite = 1;
					mSdm.refreshFavorite(1);
				}
			}
		});
		
		if(music.favorite == 1) {
			viewHolder.favoriteIv.setImageResource(R.drawable.icon_favourite_checked);
		} else {
			viewHolder.favoriteIv.setImageResource(R.drawable.icon_favourite_normal);
		}

		viewHolder.musicNameTv.setText((position + 1) + "." + music.musicName);
		viewHolder.artistTv.setText(music.artist);
		viewHolder.durationTv
				.setText(MusicUtils.makeTimeString(music.duration));

		return convertView;
	}
}
