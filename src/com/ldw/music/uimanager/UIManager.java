/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.uimanager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ldw.music.R;
import com.ldw.music.activity.IConstants;
import com.ldw.music.activity.MainContentActivity;
import com.ldw.music.activity.MainContentActivity.OnBackListener;
import com.ldw.music.storage.SPStorage;

/**
 * 动态生成view并通过viewPager来显示
 * 
 * @author longdw
 * 
 */
public class UIManager implements IConstants, OnBackListener {

	public interface OnRefreshListener {
		public void onRefresh();
	}

	private Activity mActivity;
	private View mView;
	private LayoutInflater mInflater;
	/** mViewPager为第一层 mViewPagerSub为第二层（例如从文件夹或歌手进入列表，点击列表会进入第二层） */
	private ViewPager mViewPager, mViewPagerSub;
	private List<View> mListViews, mListViewsSub;

	private OnRefreshListener mRefreshListener;
	private MainContentActivity mMainActivity;
	
	private RelativeLayout mMainLayout;
	private ChangeBgReceiver mReceiver;
	private MainUIManager mMainUIManager;
//	private SPStorage mSp;
//	private String mDefaultBgPath;

	public UIManager(Activity activity, View view) {
		this.mActivity = activity;
		this.mView = view;
		mMainActivity = (MainContentActivity) activity;
		this.mInflater = LayoutInflater.from(activity);
		initBroadCast();
		initBg();
		init();
	}

	private void init() {

		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mViewPagerSub = (ViewPager) findViewById(R.id.viewPagerSub);
		
		mListViews = new ArrayList<View>();
		mListViewsSub = new ArrayList<View>();
		mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		mViewPagerSub.setOnPageChangeListener(new MyOnPageChangeListenerSub());
	}
	
	private void initBg() {
		SPStorage mSp = new SPStorage(mActivity);
		String mDefaultBgPath = mSp.getPath();
		mMainLayout = (RelativeLayout) findViewById(R.id.main_layout);
		Bitmap bitmap = getBitmapByPath(mDefaultBgPath);
		if(bitmap != null) {
			mMainLayout.setBackgroundDrawable(new BitmapDrawable(mActivity.getResources(), bitmap));
		}
		
		//如果第一次进来 SharedPreference中没有数据
		if(TextUtils.isEmpty(mDefaultBgPath)) {
			mSp.savePath("004.jpg");
		}
	}
	
	private void initBroadCast() {
		mReceiver = new ChangeBgReceiver();
		IntentFilter filter = new IntentFilter(BROADCAST_CHANGEBG);
		mActivity.registerReceiver(mReceiver, filter);
	}
	
	private class ChangeBgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String path = intent.getStringExtra("path");
			Bitmap bitmap = getBitmapByPath(path);
			if(bitmap != null) {
				mMainLayout.setBackgroundDrawable(new BitmapDrawable(mActivity.getResources(), bitmap));
			}
			if(mMainUIManager != null) {
				mMainUIManager.setBgByPath(path);
			}
		}
	}
	
	public Bitmap getBitmapByPath(String path) {
		AssetManager am = mActivity.getAssets();
		Bitmap bitmap = null;
		try {
			InputStream is = am.open("bkgs/" + path);
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	private View findViewById(int id) {
		return mView.findViewById(id);
	}

	public void setOnRefreshListener(OnRefreshListener listener) {
		mRefreshListener = listener;
	}

	public void setContentType(int type) {
		// 此处可以根据传递过来的view和type分开来处理
		setContentType(type, null);
	}

	public void setCurrentItem() {
		if (mViewPagerSub.getChildCount() > 0) {
			mViewPagerSub.setCurrentItem(0, true);
		} else {
			mViewPager.setCurrentItem(0, true);
		}
	}

	public void setContentType(int type, Object obj) {
		// 注册监听返回按钮
		mMainActivity.registerBackListener(this);
		switch (type) {
		case START_FROM_LOCAL:
			mMainUIManager = new MyMusicManager(mActivity, this);
			View transView1 = mInflater.inflate(
					R.layout.viewpager_trans_layout, null);
			View contentView1 = mMainUIManager.getView(START_FROM_LOCAL);
			mViewPager.setVisibility(View.VISIBLE);
			mListViews.clear();
			mViewPager.removeAllViews();

			mListViews.add(transView1);
			mListViews.add(contentView1);
			mViewPager.setAdapter(new MyPagerAdapter(mListViews));
			mViewPager.setCurrentItem(1, true);
			break;
		case START_FROM_FAVORITE:
			mMainUIManager = new MyMusicManager(mActivity, this);
			View transView2 = mInflater.inflate(
					R.layout.viewpager_trans_layout, null);
			View contentView2 = mMainUIManager.getView(START_FROM_FAVORITE);
			mViewPager.setVisibility(View.VISIBLE);
			mListViews.clear();
			mViewPager.removeAllViews();

			mListViews.add(transView2);
			mListViews.add(contentView2);
			mViewPager.setAdapter(new MyPagerAdapter(mListViews));
			mViewPager.setCurrentItem(1, true);
			break;
		case START_FROM_FOLDER:
			mMainUIManager = new FolderBrowserManager(
					mActivity, this);
			View transView3 = mInflater.inflate(
					R.layout.viewpager_trans_layout, null);
			View folderView = mMainUIManager.getView();
			mViewPager.setVisibility(View.VISIBLE);
			mListViews.clear();
			mViewPager.removeAllViews();

			mListViews.add(transView3);
			mListViews.add(folderView);
			mViewPager.setAdapter(new MyPagerAdapter(mListViews));
			mViewPager.setCurrentItem(1, true);
			break;
		case START_FROM_ARTIST:
			mMainUIManager = new ArtistBrowserManager(
					mActivity, this);
			View transView4 = mInflater.inflate(
					R.layout.viewpager_trans_layout, null);
			View artistView = mMainUIManager.getView();
			mViewPager.setVisibility(View.VISIBLE);
			mListViews.clear();
			mViewPager.removeAllViews();

			mListViews.add(transView4);
			mListViews.add(artistView);
			mViewPager.setAdapter(new MyPagerAdapter(mListViews));
			mViewPager.setCurrentItem(1, true);
			break;
		case START_FROM_ALBUM:
			mMainUIManager = new AlbumBrowserManager(
					mActivity, this);
			View transView5 = mInflater.inflate(
					R.layout.viewpager_trans_layout, null);
			View albumView = mMainUIManager.getView();
			mViewPager.setVisibility(View.VISIBLE);
			mListViews.clear();
			mViewPager.removeAllViews();

			mListViews.add(transView5);
			mListViews.add(albumView);
			mViewPager.setAdapter(new MyPagerAdapter(mListViews));
			mViewPager.setCurrentItem(1, true);
			break;
		case FOLDER_TO_MYMUSIC:
			mMainUIManager = new MyMusicManager(mActivity, this);
			View transViewSub1 = mInflater.inflate(
					R.layout.viewpager_trans_layout, null);
			View contentViewSub1 = mMainUIManager.getView(START_FROM_FOLDER, obj);
			mViewPagerSub.setVisibility(View.VISIBLE);
			mListViewsSub.clear();
			mViewPagerSub.removeAllViews();

			mListViewsSub.add(transViewSub1);
			mListViewsSub.add(contentViewSub1);
			mViewPagerSub.setAdapter(new MyPagerAdapter(mListViewsSub));
			mViewPagerSub.setCurrentItem(1, true);
			break;
		case ARTIST_TO_MYMUSIC:
			mMainUIManager = new MyMusicManager(mActivity, this);
			View transViewSub2 = mInflater.inflate(
					R.layout.viewpager_trans_layout, null);
			View contentViewSub2 = mMainUIManager.getView(START_FROM_ARTIST, obj);
			mViewPagerSub.setVisibility(View.VISIBLE);
			mListViewsSub.clear();
			mViewPagerSub.removeAllViews();

			mListViewsSub.add(transViewSub2);
			mListViewsSub.add(contentViewSub2);
			mViewPagerSub.setAdapter(new MyPagerAdapter(mListViewsSub));
			mViewPagerSub.setCurrentItem(1, true);
			break;
		case ALBUM_TO_MYMUSIC:
			mMainUIManager = new MyMusicManager(mActivity, this);
			View transViewSub3 = mInflater.inflate(
					R.layout.viewpager_trans_layout, null);
			View contentViewSub3 = mMainUIManager.getView(START_FROM_ALBUM, obj);
			mViewPagerSub.setVisibility(View.VISIBLE);
			mListViewsSub.clear();
			mViewPagerSub.removeAllViews();

			mListViewsSub.add(transViewSub3);
			mListViewsSub.add(contentViewSub3);
			mViewPagerSub.setAdapter(new MyPagerAdapter(mListViewsSub));
			mViewPagerSub.setCurrentItem(1, true);
			break;
		}
	}

	private class MyPagerAdapter extends PagerAdapter {

		private List<View> listViews;

		public MyPagerAdapter(List<View> views) {
			this.listViews = views;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(listViews.get(position));// 删除页卡
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {// 这个方法用来实例化页卡
			container.addView(listViews.get(position));// 添加页卡
			return listViews.get(position);
		}

		@Override
		public int getCount() {
			return listViews.size();// 返回页卡的数量
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;// 官方提示这样写
		}
	}

	private class MyOnPageChangeListener implements OnPageChangeListener {

		int onPageScrolled = -1;

		// 当滑动状态改变时调用
		@Override
		public void onPageScrollStateChanged(int arg0) {
			System.out.println("onPageScrollStateChanged--->" + arg0);
			if (onPageScrolled == 0 && arg0 == 0) {
				mMainActivity.unRegisterBackListener(UIManager.this);
				mViewPager.removeAllViews();
				mViewPager.setVisibility(View.INVISIBLE);
				if (mRefreshListener != null) {
					mRefreshListener.onRefresh();
				}
			}
		}

		// 当当前页面被滑动时调用
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			onPageScrolled = arg0;
			// System.out.println("onPageScrolled--->" + "arg0=" + arg0 +
			// " arg1="
			// + arg1 + " arg2=" + arg2);
		}

		// 当新的页面被选中时调用
		@Override
		public void onPageSelected(int arg0) {
			// System.out.println("onPageSelected--->" + arg0);
		}
	}

	private class MyOnPageChangeListenerSub implements OnPageChangeListener {

		int onPageScrolled = -1;

		// 当滑动状态改变时调用
		@Override
		public void onPageScrollStateChanged(int arg0) {
			if (onPageScrolled == 0 && arg0 == 0) {
				mViewPagerSub.removeAllViews();
				mViewPagerSub.setVisibility(View.INVISIBLE);
			}
		}

		// 当当前页面被滑动时调用
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			onPageScrolled = arg0;
		}

		// 当新的页面被选中时调用
		@Override
		public void onPageSelected(int arg0) {
		}
	}

	@Override
	public void onBack() {
		if (mViewPagerSub.isShown()) {
			mViewPagerSub.setCurrentItem(0, true);
		} else if (mViewPager.isShown()) {
			mViewPager.setCurrentItem(0, true);
		}
	}

}
