/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.uimanager;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ldw.music.R;
import com.ldw.music.activity.IConstants;
import com.ldw.music.model.FolderInfo;
import com.ldw.music.storage.SPStorage;
import com.ldw.music.utils.MusicUtils;

/**
 * 文件夹列表
 * @author longdw(longdawei1988@gmail.com)
 *
 */
public class FolderBrowserManager extends MainUIManager implements IConstants,
		OnItemClickListener, OnClickListener {

	private Activity mActivity;
	private LayoutInflater mInflater;

	private ListView mListView;
	private MyAdapter mAdapter;
	private List<FolderInfo> list = new ArrayList<FolderInfo>();
	private ImageButton mBackBtn;

	private UIManager mUIManager;

	private RelativeLayout mFolderLayout;

	public FolderBrowserManager(Activity activity, UIManager manager) {
		this.mActivity = activity;
		this.mInflater = LayoutInflater.from(activity);
		this.mUIManager = manager;
	}

	public View getView() {
		View folderView = mInflater.inflate(R.layout.folderbrower, null);
		initBg(folderView);
		initView(folderView);
		return folderView;
	}

	private void initView(View view) {

		mBackBtn = (ImageButton) view.findViewById(R.id.backBtn);
		mBackBtn.setOnClickListener(this);

		mListView = (ListView) view.findViewById(R.id.folder_listview);
		mListView.setOnItemClickListener(this);
		mAdapter = new MyAdapter();
		list = MusicUtils.queryFolder(mActivity);
		mListView.setAdapter(mAdapter);
	}

	private void initBg(View view) {
		mFolderLayout = (RelativeLayout) view
				.findViewById(R.id.main_folder_layout);
		SPStorage mSp = new SPStorage(mActivity);
		String mDefaultBgPath = mSp.getPath();
		Bitmap bitmap = mUIManager.getBitmapByPath(mDefaultBgPath);
		if (bitmap != null) {
			mFolderLayout.setBackgroundDrawable(new BitmapDrawable(mActivity
					.getResources(), bitmap));
		}
	}

	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public FolderInfo getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FolderInfo folder = getItem(position);
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = mActivity.getLayoutInflater().inflate(
						R.layout.folderbrower_listitem, null);
				viewHolder.folderNameTv = (TextView) convertView
						.findViewById(R.id.folder_name_tv);
				viewHolder.folderPathTv = (TextView) convertView
						.findViewById(R.id.folder_path_tv);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.folderNameTv.setText(folder.folder_name);
			viewHolder.folderPathTv.setText(folder.folder_path);

			return convertView;
		}

		private class ViewHolder {
			TextView folderNameTv, folderPathTv;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mUIManager
				.setContentType(FOLDER_TO_MYMUSIC, mAdapter.getItem(position));
	}

	@Override
	public void onClick(View v) {
		mUIManager.setCurrentItem();
	}

	@Override
	protected void setBgByPath(String path) {
		Bitmap bitmap = mUIManager.getBitmapByPath(path);
		if (bitmap != null) {
			mFolderLayout.setBackgroundDrawable(new BitmapDrawable(mActivity
					.getResources(), bitmap));
		}
	}

	@Override
	public View getView(int from) {
		return null;
	}

	@Override
	public View getView(int from, Object obj) {
		return null;
	}

}
