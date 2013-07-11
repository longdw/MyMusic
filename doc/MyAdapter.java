package com.ldw.music.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ldw.music.R;
import com.ldw.music.activity.IConstants;
import com.ldw.music.interfaces.IQueryFinished;
import com.ldw.music.model.MusicInfo;
import com.ldw.music.service.ServiceManager;
import com.ldw.music.utils.MusicUtils;
import com.ldw.music.utils.StringHelper;

public class MyAdapter extends BaseAdapter implements IConstants {

	private LayoutInflater mLayoutInflater;
	private QueryHandler queryHandler;
	private ArrayList<MusicInfo> mMusicList;
	private ServiceManager mServiceManager;

	private int mPlayState, mCurPlayMusicIndex;
	private IQueryFinished mIQueryFinished;

	class ViewHolder {
		TextView musicNameTv, artistTv, durationTv;
		ImageView mPlayStateIconIv;
	}

	public MyAdapter(Context context, ServiceManager sm) {
		mLayoutInflater = LayoutInflater.from(context);
		queryHandler = new QueryHandler(context.getContentResolver());
		mMusicList = new ArrayList<MusicInfo>();
		this.mServiceManager = sm;
	}

	public QueryHandler getQueryHandler() {
		return queryHandler;
	}

	/**
	 * 当数据库中有数据的时候会调用该方法来更新列表
	 * @param list
	 */
	public void setData(List<MusicInfo> list) {
		mMusicList.clear();
		if (list != null) {
			mMusicList.addAll(list);
			// 为list排序
			Collections.sort(mMusicList, comparator);
			mServiceManager.refreshMusicList(mMusicList);
			notifyDataSetChanged();
		}
	}

	public List<MusicInfo> getData() {
		return mMusicList;
	}

	class QueryHandler extends AsyncQueryHandler {

		public QueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			mMusicList = MusicUtils.getMusicList(cursor);
			// 为list排序
			Collections.sort(mMusicList, comparator);

			mServiceManager.refreshMusicList(mMusicList);
			if (mIQueryFinished != null) {
				mIQueryFinished.onFinished(mMusicList);
			}
			notifyDataSetChanged();
		}
	}

	public void setQueryFinished(IQueryFinished finish) {
		mIQueryFinished = finish;
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
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		MusicInfo music = getItem(position);
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
			viewHolder.mPlayStateIconIv = (ImageView) convertView
					.findViewById(R.id.playstate_iv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (position != mCurPlayMusicIndex) {
			viewHolder.mPlayStateIconIv.setVisibility(View.GONE);
		} else {
			viewHolder.mPlayStateIconIv.setVisibility(View.VISIBLE);
			if (mPlayState == MPS_PAUSE) {
				viewHolder.mPlayStateIconIv
						.setBackgroundResource(R.drawable.list_pause_state);
			} else {
				viewHolder.mPlayStateIconIv
						.setBackgroundResource(R.drawable.list_play_state);
			}
		}

		viewHolder.musicNameTv.setText((position + 1) + "." + music.musicName);
		viewHolder.artistTv.setText(music.artist);
		viewHolder.durationTv
				.setText(MusicUtils.makeTimeString(music.duration));

		return convertView;
	}
}
