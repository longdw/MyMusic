/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.ldw.music.R;
import com.ldw.music.fragment.LeftFragment;
import com.ldw.music.fragment.MenuBackgroundFragment;
import com.ldw.music.fragment.MenuScanFragment;
import com.ldw.music.fragment.RightFragment;

/**
 * 扫描歌曲
 * @author longdw(longdawei1988@gmail.com)
 *
 */
public class MenuScanActivity extends FragmentActivity {
	
	public ViewPager mViewPager;
	private List<Fragment> mFragmentList = new ArrayList<Fragment>();
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.menu_scan);
		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		
		initViewPager();
	}

	private void initViewPager() {
		Fragment leftFragment = new LeftFragment();
		Fragment rightFragment = new RightFragment();
		Fragment menuFragment = new MenuScanFragment();

		mFragmentList.add(leftFragment);
		mFragmentList.add(menuFragment);
		mFragmentList.add(rightFragment);

		mViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(),
				mFragmentList));
		mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
		mViewPager.setCurrentItem(1, true);
		
		/*Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				mViewPager.setCurrentItem(1, true);
			}
		};
		handler.sendEmptyMessageDelayed(1, 1000);*/
	}
	
	private class MyPagerAdapter extends FragmentPagerAdapter {

		List<Fragment> fragmentList;

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		public MyPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
			super(fm);
			this.fragmentList = fragments;
		}

		@Override
		public Fragment getItem(int arg0) {
			return fragmentList.get(arg0);
		}

		@Override
		public int getCount() {
			return fragmentList.size();
		}
	}

	private class MyOnPageChangeListener implements OnPageChangeListener {

		int onPageScrolled = -1;

		// 当滑动状态改变时调用
		@Override
		public void onPageScrollStateChanged(int arg0) {
//			System.out.println("onPageScrollStateChanged--->" + arg0);
			if ((onPageScrolled == 0 || onPageScrolled == 2) && arg0 == 0) {
				setResult(1);
				finish();
			}
		}

		// 当当前页面被滑动时调用
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			onPageScrolled = arg0;
//			System.out.println("onPageScrolled--->" + "arg0=" + arg0 + " arg1="
//					+ arg1 + " arg2=" + arg2);
		}

		// 当新的页面被选中时调用
		@Override
		public void onPageSelected(int arg0) {
//			System.out.println("onPageSelected--->" + arg0);
		}
	}
	
	@Override
	public void onBackPressed() {
		if(mViewPager.isShown()) {
			mViewPager.setCurrentItem(0, true);
		}
	}
}
