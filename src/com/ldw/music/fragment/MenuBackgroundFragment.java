/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.ldw.music.R;
import com.ldw.music.activity.IConstants;
import com.ldw.music.activity.MenuBackgroundActivity;
import com.ldw.music.storage.SPStorage;

/**
 * 
 * @author longdw(longdawei1988@gmail.com)
 *
 */
public class MenuBackgroundFragment extends Fragment implements
		OnItemClickListener, IConstants, OnClickListener {

	private ImageButton mBackBtn;
	private GridView mGridView;
	private List<BgEntity> mBgList;
	private MyAdapter mAdapter;
	private SPStorage mSp;
	private String mDefaultBgPath;

	private class BgEntity {
		Bitmap bitmap;
		String path;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.menu_background_fragment,
				container, false);
		mSp = new SPStorage(getActivity());
		mDefaultBgPath = mSp.getPath();

		getData();
		initView(view);

		return view;
	}

	private void getData() {
		AssetManager am = getActivity().getAssets();
		try {
			String[] drawableList = am.list("bkgs");
			mBgList = new ArrayList<BgEntity>();
			for (String path : drawableList) {
				BgEntity bg = new BgEntity();
				InputStream is = am.open("bkgs/" + path);
				Bitmap bitmap = BitmapFactory.decodeStream(is);
				bg.path = path;
				bg.bitmap = bitmap;
				mBgList.add(bg);
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initView(View view) {
		mBackBtn = (ImageButton) view.findViewById(R.id.backBtn);
		mBackBtn.setOnClickListener(this);
		mGridView = (GridView) view.findViewById(R.id.grid_content);
		mAdapter = new MyAdapter(mBgList);

		mGridView.setOnItemClickListener(this);
		mGridView.setAdapter(mAdapter);
	}

	private class MyAdapter extends BaseAdapter {

		private List<BgEntity> bgList;
		private Resources resources;

		public MyAdapter(List<BgEntity> list) {
			this.bgList = list;
			this.resources = getActivity().getResources();
		}

		@Override
		public int getCount() {
			return bgList.size();
		}

		@Override
		public BgEntity getItem(int arg0) {
			return bgList.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = getActivity().getLayoutInflater().inflate(
						R.layout.background_gridview_item, null);
				viewHolder.backgroundIv = (ImageView) convertView
						.findViewById(R.id.gridview_item_iv);
				viewHolder.checkedIv = (ImageView) convertView
						.findViewById(R.id.gridview_item_checked_iv);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.backgroundIv.setBackgroundDrawable(new BitmapDrawable(
					resources, getItem(position).bitmap));
			if (getItem(position).path.equals(mDefaultBgPath)) {
				viewHolder.checkedIv.setVisibility(View.VISIBLE);
			} else {
				viewHolder.checkedIv.setVisibility(View.GONE);
			}

			return convertView;
		}

		private class ViewHolder {
			ImageView checkedIv, backgroundIv;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String path = mAdapter.getItem(arg2).path;
		mSp.savePath(path);

		mDefaultBgPath = path;
		mAdapter.notifyDataSetChanged();

		Intent intent = new Intent(BROADCAST_CHANGEBG);
		intent.putExtra("path", path);
		getActivity().sendBroadcast(intent);

		((MenuBackgroundActivity) getActivity()).mViewPager.setCurrentItem(0,
				true);
	}

	@Override
	public void onClick(View v) {
		if (v == mBackBtn) {
			((MenuBackgroundActivity) getActivity()).mViewPager.setCurrentItem(
					0, true);
		}
	}
}
