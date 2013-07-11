/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ldw.music.R;
import com.ldw.music.aidl.IMediaService;
import com.ldw.music.model.MusicInfo;
import com.ldw.music.utils.MusicUtils;
import com.ldw.music.utils.StringHelper;
import com.ldw.music.view.KeyBoardKeyView;

/**
 * 歌曲搜索界面
 * @author longdw(longdawei1988@gmail.com)
 *
 */
public class MusicListSearchActivity extends Activity implements
		OnClickListener, IConstants, OnItemClickListener {

	private ImageView mKeyboardSwitcherIv;
	private EditText mSearchInputEt;
	private LinearLayout mKeyboardLayout;
	/** 弹出的搜索软键盘是否是自定义的T9键盘 */
	private boolean mIsT9Keyboard = true;
	private InputMethodManager mInputMethodManager;
	/** T9键盘各数字键对应的正则表达式 */
	private static String T9KEYS[] = { "", "", "[abc]", "[def]", "[ghi]",
			"[jkl]", "[mno]", "[pqrs]", "[tuv]", "[wxyz]" };
	/** 当前播放的Music对象 */
	// private Music mCurPlayingMusic;
	private List<MusicInfo> mShowData = new ArrayList<MusicInfo>();
	private ServiceConnection mServiceConnection;
	private IMediaService mService;
	private List<MusicInfo> mMusicList = new ArrayList<MusicInfo>();
	private int mCurMusicId = -1;

	private ListView mListView;
	protected int mPlayingSongPosition;
	private SearchAdapter mAdapter;

	private Animation mAnimIn, mAnimOut;
	private MusicPlayBroadcast mPlayBroadcast;
	private int mPlayState = MPS_PLAYING;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.music_search);

		mPlayBroadcast = new MusicPlayBroadcast();
		IntentFilter filter = new IntentFilter(BROADCAST_NAME);
		registerReceiver(mPlayBroadcast, filter);

		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mAnimIn = AnimationUtils.loadAnimation(this, R.anim.push_bottom_in);
		mAnimOut = AnimationUtils.loadAnimation(this, R.anim.push_bottom_out);

		mAnimIn.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mKeyboardLayout.setVisibility(View.VISIBLE);
			}
		});

		initConnection();
		initView();
	}

	private void initConnection() {
		mServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mService = IMediaService.Stub.asInterface(service);
				if (mService != null) {
					try {
						mService.getMusicList(mMusicList);
						mCurMusicId = mService.getCurMusicId();
						mPlayingSongPosition = MusicUtils.seekPosInListById(
								mMusicList, mCurMusicId);
						mPlayState = mService.getPlayState();
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

	protected void initListView() {
		mAdapter = new SearchAdapter(mMusicList);
		mAdapter.setPlayState(mPlayState, mPlayingSongPosition);
		mListView.setAdapter(mAdapter);
	}

	private void initView() {

		mListView = (ListView) findViewById(R.id.listview);
		mKeyboardLayout = (LinearLayout) findViewById(R.id.layout_keyboard);
		mKeyboardSwitcherIv = (ImageView) findViewById(R.id.keyboard_switcher);
		mSearchInputEt = (EditText) findViewById(R.id.search_input);
		mKeyboardSwitcherIv.setOnClickListener(this);
		mSearchInputEt.setOnClickListener(this);
		mListView.setOnItemClickListener(this);

		// 搜索输入框禁止输入法输入
		mSearchInputEt.setInputType(InputType.TYPE_NULL);
		mSearchInputEt.requestFocus();

		KeyBoardKeyView key2 = (KeyBoardKeyView) findViewById(R.id.t9_key_2);
		KeyBoardKeyView key3 = (KeyBoardKeyView) findViewById(R.id.t9_key_3);
		KeyBoardKeyView key4 = (KeyBoardKeyView) findViewById(R.id.t9_key_4);
		KeyBoardKeyView key5 = (KeyBoardKeyView) findViewById(R.id.t9_key_5);
		KeyBoardKeyView key6 = (KeyBoardKeyView) findViewById(R.id.t9_key_6);
		KeyBoardKeyView key7 = (KeyBoardKeyView) findViewById(R.id.t9_key_7);
		KeyBoardKeyView key8 = (KeyBoardKeyView) findViewById(R.id.t9_key_8);
		KeyBoardKeyView key9 = (KeyBoardKeyView) findViewById(R.id.t9_key_9);
		key2.init("ABC");
		key3.init("DEF");
		key4.init("GHI");
		key5.init("JKL");
		key6.init("MNO");
		key7.init("PQRS");
		key8.init("TUV");
		key9.init("WXYZ");
		key2.setOnClickListener(this);
		key3.setOnClickListener(this);
		key4.setOnClickListener(this);
		key5.setOnClickListener(this);
		key6.setOnClickListener(this);
		key7.setOnClickListener(this);
		key8.setOnClickListener(this);
		key9.setOnClickListener(this);
		findViewById(R.id.t9_exit).setOnClickListener(this);
		View deleteKey = findViewById(R.id.t9_delete);
		deleteKey.setOnClickListener(this);
		deleteKey.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// 长按删除，删除所有输入内容
				Editable et = mSearchInputEt.getText();
				if (!et.toString().equals("")) {
					et.clear();
					mSearchInputEt.setText(et);
					mSearchInputEt.setSelection(0);
					return true;
				}
				return false;
			}
		});
		// 搜索输入框文本变化监听
		mSearchInputEt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// 输入框文字改变时过滤歌曲列表
				if (TextUtils.isEmpty(s)) {
					// 这地方在按home键回到桌面再回来的时候会报空指针错误
					mAdapter.setData(mMusicList);
				} else if (mIsT9Keyboard) {
					// T9键盘开启，进行简拼全拼搜索
					pinyinSearch(s.toString());
				} else {
					// 普通的模糊搜索
					pinyinSearch(StringHelper.getPingYin(s.toString()));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				try {
					if (mCurMusicId != -1) {
						mAdapter.setPlayState(mService.getPlayState(),
								MusicUtils.seekPosInListById(
										mAdapter.getData(), mCurMusicId));
					} 
					/*else {
						mAdapter.setPlayState(mService.getPlayState(), 0);
					}*/
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private class MusicPlayBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BROADCAST_NAME)) {
				int playState = intent.getIntExtra(PLAY_STATE_NAME, MPS_NOFILE);
				int curPlayIndex = intent.getIntExtra(PLAY_MUSIC_INDEX, -1);

				mAdapter.setPlayState(playState, curPlayIndex);
			}
		}
	}

	private class SearchAdapter extends BaseAdapter {

		private int mPlayState, mCurPlayMusicIndex;
		private List<MusicInfo> musicList = new ArrayList<MusicInfo>();

		public SearchAdapter(List<MusicInfo> mMusicList) {
			musicList.addAll(mMusicList);
		}

		@Override
		public int getCount() {
			return musicList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void setPlayState(int playState, int playIndex) {
			mPlayState = playState;
			mCurPlayMusicIndex = playIndex;
			notifyDataSetChanged();
		}

		public void setData(List<MusicInfo> list) {
			musicList.clear();
			if (list != null) {
				musicList.addAll(list);
				notifyDataSetChanged();
			}
		}

		public List<MusicInfo> getData() {
			return musicList;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			MusicInfo music = musicList.get(position);

			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = getLayoutInflater().inflate(
						R.layout.searchlist_item, null);
				viewHolder.titleTv = (TextView) convertView
						.findViewById(R.id.item_musicname_tv);
				viewHolder.artistTv = (TextView) convertView
						.findViewById(R.id.item_artist_tv);
				viewHolder.playStateIv = (ImageView) convertView
						.findViewById(R.id.item_playstate_iv);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			if (position != mCurPlayMusicIndex) {
				viewHolder.playStateIv.setVisibility(View.GONE);
			} else {
				viewHolder.playStateIv.setVisibility(View.VISIBLE);
				if (mPlayState == MPS_PAUSE) {
					viewHolder.playStateIv
							.setBackgroundResource(R.drawable.list_pause_state);
				} else {
					viewHolder.playStateIv
							.setBackgroundResource(R.drawable.list_play_state);
				}
			}

			viewHolder.titleTv.setText((position + 1) + "." + music.musicName);
			viewHolder.artistTv.setText(music.artist);

			return convertView;
		}

		class ViewHolder {
			TextView titleTv, artistTv;
			ImageView playStateIv;
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.keyboard_switcher:
			mIsT9Keyboard = !mIsT9Keyboard;
			if (mIsT9Keyboard) {// 若T9隐藏 打开T9
				mKeyboardSwitcherIv
						.setImageResource(R.drawable.keyboard_switch);
				mSearchInputEt.setHint("请输入简拼或全拼");
				// 搜索输入框禁止输入法输入
				mSearchInputEt.setInputType(InputType.TYPE_NULL);

				// 隐藏输入法
				mInputMethodManager.hideSoftInputFromWindow(
						mSearchInputEt.getWindowToken(), 0);
				// 弹出T9键盘
				mKeyboardLayout.setVisibility(View.VISIBLE);
			} else {// 若T9显示 隐藏T9
				mKeyboardLayout.setVisibility(View.GONE);
				mKeyboardSwitcherIv
						.setImageResource(R.drawable.keyboard_switch_9);
				mSearchInputEt.setHint("请输入简拼或全拼");
				// 显示输入法
				mInputMethodManager.showSoftInput(mSearchInputEt, 0);
				// 搜索输入框允许输入法输入
				mSearchInputEt.setInputType(InputType.TYPE_CLASS_TEXT);
			}
			break;
		case R.id.search_input:
			// 隐藏输入法
			// mInputMethodManager.hideSoftInputFromWindow(
			// mSearchInputEt.getWindowToken(), 0);

			if (!mIsT9Keyboard) {
				// 显示输入法
				mInputMethodManager.showSoftInput(mSearchInputEt, 0);
				return;
			}

			if (!mKeyboardLayout.isShown() && mIsT9Keyboard) {
				mSearchInputEt.requestFocus();
				mKeyboardLayout.startAnimation(mAnimIn);
			}
			break;
		case R.id.t9_key_2:
			appendImageSpan(R.drawable.keyboard_edit_2, 2);
			break;
		case R.id.t9_key_3:
			appendImageSpan(R.drawable.keyboard_edit_3, 3);
			break;
		case R.id.t9_key_4:
			appendImageSpan(R.drawable.keyboard_edit_4, 4);
			break;
		case R.id.t9_key_5:
			appendImageSpan(R.drawable.keyboard_edit_5, 5);
			break;
		case R.id.t9_key_6:
			appendImageSpan(R.drawable.keyboard_edit_6, 6);
			break;
		case R.id.t9_key_7:
			appendImageSpan(R.drawable.keyboard_edit_7, 7);
			break;
		case R.id.t9_key_8:
			appendImageSpan(R.drawable.keyboard_edit_8, 8);
			break;
		case R.id.t9_key_9:
			appendImageSpan(R.drawable.keyboard_edit_9, 9);
			break;
		case R.id.t9_exit:
			mKeyboardLayout.startAnimation(mAnimOut);
			mKeyboardLayout.setVisibility(View.GONE);
			break;
		case R.id.t9_delete:
			backDeleteImageSpan();
			break;
		}
	}

	/** 在搜索输入框末尾追加T9键的输入 */
	private void appendImageSpan(int drawableResId, int keynum) {
		Drawable drawable = getResources().getDrawable(drawableResId);
		String insteadString = String.valueOf(keynum);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		// 需要处理的文本，[smile]是需要被替代的文本
		SpannableString spannable = new SpannableString(insteadString);
		// 要让图片替代指定的文字就要用ImageSpan
		ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
		// 开始替换，注意第2和第3个参数表示从哪里开始替换到哪里替换结束（start和end）
		// 最后一个参数类似数学中的集合,[5,12)表示从5到12，包括5但不包括12
		spannable.setSpan(span, 0, insteadString.length(),
				Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		Editable et = mSearchInputEt.getText();
		int start = mSearchInputEt.getSelectionStart();
		et.insert(start, spannable);
		mSearchInputEt.setText(et);
		mSearchInputEt.setSelection(start + spannable.length());
	}

	/** 回退删除搜索框的T9键的输入 */
	private void backDeleteImageSpan() {
		Editable et = mSearchInputEt.getText();
		if (!et.toString().equals("")) {
			et = et.delete(et.length() - 1, et.length());
			mSearchInputEt.setText(et);
			mSearchInputEt.setSelection(et.length());
		}
	}

	/**
	 * T9键盘简拼、全拼搜索
	 * 
	 * @param str
	 *            输入的字符串，均为2~9的数字
	 */
	private void pinyinSearch(String input) {
		mShowData.clear();
		StringBuffer sb = new StringBuffer();

		// XXX 对特殊非字母、非数字、非汉字的字符支持不理想，有待改进

		// 获取每一个数字对应的字母列表并以'-'隔开
		for (int i = 0; i < input.length(); i++) {
			if (mIsT9Keyboard && input.charAt(i) <= '9'
					&& input.charAt(i) >= '0') {
				sb.append(T9KEYS[input.charAt(i) - '0']);
			} else {
				sb.append(input.charAt(i));
			}
			if (i != input.length() - 1) {
				sb.append("-");
			}
		}

		// 遍历原始数据集合，寻找匹配的条目
		for (MusicInfo item : mMusicList) {
			if (contains(sb.toString(), item.musicNameKey, input)) {
				mShowData.add(item);
			} else if (contains(sb.toString(), item.artistKey, input)) {
				mShowData.add(item);
			}
		}
		mAdapter.setData(mShowData);
	}

	/**
	 * 检查所给的搜索索引值是否匹配给定正则表达式
	 * 
	 * @param regexp
	 *            正则表达式
	 * @param key
	 *            索引值
	 * @param input
	 *            搜索条件是否大于6个字符
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	private boolean contains(String regexp, String key, String input) {
		if (TextUtils.isEmpty(key)) {
			return false;
		}
		// 搜索条件大于6个字符将不按拼音首字母查询
		if (input.length() < 6) {
			// 根据首字母进行模糊查询
			String p = regexp.toUpperCase(Locale.CHINA).replace("-", "[*+a-z]*");
			Pattern pattern = Pattern.compile(p);
			Matcher matcher = pattern.matcher(key);

			if (matcher.find()) {
				return true;
			}
		}

		// 根据全拼查询
		Pattern pattern = Pattern.compile(regexp.replace("-", ""),
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(key);
		return matcher.find();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		try {
			mService.playById(mAdapter.getData().get(position).songId);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		finish();
	}
}
