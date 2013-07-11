/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ldw.music.MusicApp;
import com.ldw.music.R;
import com.ldw.music.db.MusicInfoDao;
import com.ldw.music.fragment.MainFragment;
import com.ldw.music.fragment.MenuFragment;
import com.ldw.music.slidemenu.SlidingMenu;
import com.ldw.music.utils.MusicUtils;
import com.ldw.music.utils.SplashScreen;

/**
 * 主类，首次进入应用会到这里
 * 该类提供了首页MainFragment的显示和侧滑MenuFragment的显示
 * @author longdw(longdawei1988@gmail.com)
 *
 */
@SuppressLint("HandlerLeak")
public class MainContentActivity extends FragmentActivity implements IConstants {

	public static final String ALARM_CLOCK_BROADCAST = "alarm_clock_broadcast";
	public SlidingMenu mSlidingMenu;
	private List<OnBackListener> mBackListeners = new ArrayList<OnBackListener>();
	public MainFragment mMainFragment;

	private Handler mHandler;
	private MusicInfoDao mMusicDao;
	private SplashScreen mSplashScreen;
	private int mScreenWidth;
	
	public interface OnBackListener {
		public abstract void onBack();
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		mScreenWidth = metric.widthPixels;

		initSDCard();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(ALARM_CLOCK_BROADCAST);
		registerReceiver(mAlarmReceiver, filter);

		setContentView(R.layout.frame_main);
		mSplashScreen = new SplashScreen(this);
		mSplashScreen.show(R.drawable.image_splash_background,
				SplashScreen.SLIDE_LEFT);
		// set the Above View
		mMainFragment = new MainFragment();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.frame_main, mMainFragment).commit();

		// configure the SlidingMenu
		mSlidingMenu = new SlidingMenu(this);
		mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		mSlidingMenu.setMode(SlidingMenu.RIGHT);
		mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
		mSlidingMenu.setShadowDrawable(R.drawable.shadow);
		mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		mSlidingMenu.setFadeDegree(0.35f);
		mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		mSlidingMenu.setMenu(R.layout.frame_menu);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.frame_menu, new MenuFragment()).commit();

		mMusicDao = new MusicInfoDao(this);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				mSplashScreen.removeSplashScreen();
			}
		};

		getData();
	}

	private void initSDCard() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.setPriority(1000);// 设置最高优先级
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);// sd卡被插入，且已经挂载
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// sd卡存在，但还没有挂载
		intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);// sd卡被移除
		intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);// sd卡作为
															// USB大容量存储被共享，挂载被解除
		intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);// sd卡已经从sd卡插槽拔出，但是挂载点还没解除
		// intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);// 开始扫描
		// intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);// 扫描完成
		intentFilter.addDataScheme("file");
		registerReceiver(sdCardReceiver, intentFilter);// 注册监听函数
	}

	private void getData() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (mMusicDao.hasData()) {
					// 如果有数据就等三秒跳转
					mHandler.sendMessageDelayed(mHandler.obtainMessage(), 3000);
				} else {
					MusicUtils.queryMusic(MainContentActivity.this,
							START_FROM_LOCAL);
					MusicUtils.queryAlbums(MainContentActivity.this);
					MusicUtils.queryArtist(MainContentActivity.this);
					MusicUtils.queryFolder(MainContentActivity.this);
					mHandler.sendEmptyMessage(1);
				}
			}
		}).start();
	}

	public void registerBackListener(OnBackListener listener) {
		if (!mBackListeners.contains(listener)) {
			mBackListeners.add(listener);
		}
	}

	public void unRegisterBackListener(OnBackListener listener) {
		mBackListeners.remove(listener);
	}

	@Override
	public void onBackPressed() {
		if (mSlidingMenu.isMenuShowing()) {
			mSlidingMenu.showContent();
		} else {
			if (mBackListeners.size() == 0) {
				// super.onBackPressed();
				// 在activity中调用 moveTaskToBack (boolean nonRoot)方法即可将activity
				// 退到后台，注意不是finish()退出。
				// 参数为false代表只有当前activity是task根，指应用启动的第一个activity时，才有效;
				moveTaskToBack(true);
			}
			for (OnBackListener listener : mBackListeners) {
				listener.onBack();
			}
		}
	}

	private final BroadcastReceiver sdCardReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("android.intent.action.MEDIA_REMOVED")// 各种未挂载状态
					|| action.equals("android.intent.action.MEDIA_UNMOUNTED")
					|| action.equals("android.intent.action.MEDIA_BAD_REMOVAL")
					|| action.equals("android.intent.action.MEDIA_SHARED")) {
				finish();
				Toast.makeText(MainContentActivity.this, "SD卡以外拔出，本地数据没法初始化!",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	public void showSleepDialog() {

		if (MusicApp.mIsSleepClockSetting) {
			cancleSleepClock();
			Toast.makeText(getApplicationContext(), "已取睡眠模式！",
					Toast.LENGTH_SHORT).show();
			return;
		}

		View view = View.inflate(this, R.layout.sleep_time, null);
		final Dialog dialog = new Dialog(this, R.style.lrc_dialog);
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(false);

		Window dialogWindow = dialog.getWindow();
		WindowManager.LayoutParams lp = dialogWindow.getAttributes();
		dialogWindow.setGravity(Gravity.CENTER);
		// lp.x = 100; // 新位置X坐标
		// lp.y = 100; // 新位置Y坐标
		lp.width = (int) (mScreenWidth * 0.7); // 宽度
		// lp.height = 400; // 高度

		// 当Window的Attributes改变时系统会调用此函数,可以直接调用以应用上面对窗口参数的更改,也可以用setAttributes
		// dialog.onWindowAttributesChanged(lp);
		dialogWindow.setAttributes(lp);

		dialog.show();

		final Button cancleBtn = (Button) view.findViewById(R.id.cancle_btn);
		final Button okBtn = (Button) view.findViewById(R.id.ok_btn);
		final EditText timeEt = (EditText) view.findViewById(R.id.time_et);
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == cancleBtn) {
					dialog.dismiss();
				} else if (v == okBtn) {
					String timeS = timeEt.getText().toString();
					if (TextUtils.isEmpty(timeS)
							|| Integer.parseInt(timeS) == 0) {
						Toast.makeText(getApplicationContext(), "输入无效！",
								Toast.LENGTH_SHORT).show();
						return;
					}
					setSleepClock(timeS);
					dialog.dismiss();
				}
			}
		};

		cancleBtn.setOnClickListener(listener);
		okBtn.setOnClickListener(listener);
	}

	/**
	 * 设置睡眠闹钟
	 * 
	 * @param timeS
	 */
	private void setSleepClock(String timeS) {
		Intent intent = new Intent(ALARM_CLOCK_BROADCAST);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				MainContentActivity.this, 0, intent, 0);
		// 设置time时间之后退出程序
		int time = Integer.parseInt(timeS);
		long longTime = time * 60 * 1000L;
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC, System.currentTimeMillis() + longTime,
				pendingIntent);
		MusicApp.mIsSleepClockSetting = true;
		Toast.makeText(getApplicationContext(), "将在"+timeS+"分钟后退出软件", Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * 取消睡眠闹钟
	 */
	private void cancleSleepClock() {
		Intent intent = new Intent(ALARM_CLOCK_BROADCAST);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				MainContentActivity.this, 0, intent, 0);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.cancel(pendingIntent);
		MusicApp.mIsSleepClockSetting = false;
	}
	
	private BroadcastReceiver mAlarmReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			//退出程序
			finish();
		}

	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(sdCardReceiver);
		unregisterReceiver(mAlarmReceiver);
		MusicApp.mServiceManager.exit();
		MusicApp.mServiceManager = null;
		MusicUtils.clearCache();
		cancleSleepClock();
		System.exit(0);
	}

}
