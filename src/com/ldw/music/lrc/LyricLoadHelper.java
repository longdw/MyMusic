/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.lrc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.util.Log;

import com.ldw.music.model.LyricSentence;

/**
 * 歌词的显示控制
 * @author longdw(longdawei1988@gmail.com)
 *
 */
public class LyricLoadHelper {
	/** 用于向外通知歌词载入、变化的监听器 */
	public interface LyricListener {

		/**
		 * 歌词载入时调用
		 * 
		 * @param lyricSentences
		 *            歌词文本处理后的所有歌词句子
		 * @param indexOfCurSentence
		 *            正在播放的句子在句子集合中的索引号
		 */
		public abstract void onLyricLoaded(List<LyricSentence> lyricSentences,
				int indexOfCurSentence);

		/**
		 * 歌词变化时调用
		 * 
		 * @param indexOfCurSentence
		 *            正在播放的句子在句子集合中的索引号
		 * @param currentTime
		 *            已经播放的毫秒数
		 * */
		public abstract void onLyricSentenceChanged(int indexOfCurSentence);
	}

	private static final String TAG = LyricLoadHelper.class.getSimpleName();

	/** 句子集合 */
	private ArrayList<LyricSentence> mLyricSentences = new ArrayList<LyricSentence>();

	private LyricListener mLyricListener = null;

	private boolean mHasLyric = false;

	/** 当前正在播放的歌词句子的在句子集合中的索引号 */
	private int mIndexOfCurrentSentence = -1;

	/** 用于缓存的一个正则表达式对象,识别[]中的内容，不包括中括号 */
	private final Pattern mBracketPattern = Pattern
			.compile("(?<=\\[).*?(?=\\])");
	private final Pattern mTimePattern = Pattern
			.compile("(?<=\\[)(\\d{2}:\\d{2}\\.?\\d{0,3})(?=\\])");

	private final String mEncoding = "utf-8";

	public List<LyricSentence> getLyricSentences() {
		return mLyricSentences;
	}

	public void setLyricListener(LyricListener listener) {
		this.mLyricListener = listener;
	}

	public void setIndexOfCurrentSentence(int index) {
		mIndexOfCurrentSentence = index;
	}

	public int getIndexOfCurrentSentence() {
		return mIndexOfCurrentSentence;
	}

	/**
	 * 根据歌词文件的路径，读取出歌词文本并解析
	 * 
	 * @param lyricPath
	 *            歌词文件路径
	 * @return true表示存在歌词，false表示不存在歌词
	 */
	public boolean loadLyric(String lyricPath) {
		Log.i(TAG, "LoadLyric begin,path is:" + lyricPath);
		mHasLyric = false;
		mLyricSentences.clear();

		if (lyricPath != null) {
			File file = new File(lyricPath);
			if (file.exists()) {
				Log.i(TAG, "歌词文件存在");
				mHasLyric = true;
				try {
					FileInputStream fr = new FileInputStream(file);
					InputStreamReader isr = new InputStreamReader(fr, mEncoding);
					BufferedReader br = new BufferedReader(isr);

					String line = null;

					// 逐行分析歌词文本
					while ((line = br.readLine()) != null) {
						Log.i(TAG, "lyric line:" + line);
						parseLine(line);
					}

					// 按时间排序句子集合
					Collections.sort(mLyricSentences,
							new Comparator<LyricSentence>() {
								// 内嵌，匿名的compare类
								public int compare(LyricSentence object1,
										LyricSentence object2) {
									if (object1.getStartTime() > object2
											.getStartTime()) {
										return 1;
									} else if (object1.getStartTime() < object2
											.getStartTime()) {
										return -1;
									} else {
										return 0;
									}
								}
							});

					for (int i = 0; i < mLyricSentences.size() - 1; i++) {
						mLyricSentences.get(i).setDuringTime(
								mLyricSentences.get(i + 1).getStartTime());
					}
					mLyricSentences.get(mLyricSentences.size() - 1)
							.setDuringTime(Integer.MAX_VALUE);
					fr.close();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				}
			} else {
				Log.i(TAG, "歌词文件不存在");
			}
		}
		// 如果有谁在监听，通知它歌词载入完啦，并把载入的句子集合也传递过去
		if (mLyricListener != null) {
			mLyricListener.onLyricLoaded(mLyricSentences,
					mIndexOfCurrentSentence);
		}
		if (mHasLyric) {
			Log.i(TAG, "Lyric file existed.Lyric has " + mLyricSentences.size()
					+ " Sentences");
		} else {
			Log.i(TAG, "Lyric file does not existed");
		}
		return mHasLyric;
	}

	/**
	 * 根据传递过来的已播放的毫秒数，计算应当对应到句子集合中的哪一句，再通知监听者播放到的位置。
	 * 
	 * @param millisecond
	 *            已播放的毫秒数
	 */
	public void notifyTime(long millisecond) {
		// Log.i(TAG, "notifyTime");
		if (mHasLyric && mLyricSentences != null && mLyricSentences.size() != 0) {
			int newLyricIndex = seekSentenceIndex(millisecond);
			if (newLyricIndex != -1 && newLyricIndex != mIndexOfCurrentSentence) {// 如果找到的歌词和现在的不是一句。
				if (mLyricListener != null) {
					// 告诉一声，歌词已经变成另外一句啦！
					mLyricListener.onLyricSentenceChanged(newLyricIndex);
				}
				mIndexOfCurrentSentence = newLyricIndex;
			}
		}
	}

	private int seekSentenceIndex(long millisecond) {
		int findStart = 0;
		if (mIndexOfCurrentSentence >= 0) {
			// 如果已经指定了歌词，则现在位置开始
			findStart = mIndexOfCurrentSentence;
		}

		try {
			long lyricTime = mLyricSentences.get(findStart).getStartTime();

			if (millisecond > lyricTime) { // 如果想要查找的时间在现在字幕的时间之后
				// 如果开始位置经是最后一句了，直接返回最后一句。
				if (findStart == (mLyricSentences.size() - 1)) {
					return findStart;
				}
				int new_index = findStart + 1;
				// 找到第一句开始时间大于输入时间的歌词
				while (new_index < mLyricSentences.size()
						&& mLyricSentences.get(new_index).getStartTime() <= millisecond) {
					++new_index;
				}
				// 这句歌词的前一句就是我们要找的了。
				return new_index - 1;
			} else if (millisecond < lyricTime) { // 如果想要查找的时间在现在字幕的时间之前
				// 如果开始位置经是第一句了，直接返回第一句。
				if (findStart == 0)
					return 0;

				int new_index = findStart - 1;
				// 找到开始时间小于输入时间的歌词
				while (new_index > 0
						&& mLyricSentences.get(new_index).getStartTime() > millisecond) {
					--new_index;
				}
				// 就是它了。
				return new_index;
			} else {
				// 不用找了
				return findStart;
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			Log.i(TAG, "新的歌词载入了，所以产生了越界错误，不用理会，返回0");
			return 0;
		}
	}

	/** 解析每行歌词文本,一行文本歌词可能对应多个时间戳 */
	private void parseLine(String line) {
		if (line.equals("")) {
			return;
		}
		String content = null;
		int timeLength = 0;
		int index = 0;
		Matcher matcher = mTimePattern.matcher(line);
		int lastIndex = -1;// 最后一个时间标签的下标
		int lastLength = -1;// 最后一个时间标签的长度

		// 一行文本歌词可能对应多个时间戳，如“[01:02.3][01:11:22.33]在这阳光明媚的春天里”
		// 一行也可能包含多个句子，如“[01:02.3]在这阳光明媚的春天里[01:02:22.33]我的眼泪忍不住流淌”
		List<String> times = new ArrayList<String>();

		// 寻找出本行所有时间戳，存入times中
		while (matcher.find()) {
			// 匹配的是中括号里的字符串，如01:02.3，01:11:22.33

			String s = matcher.group();
			index = line.indexOf("[" + s + "]");
			if (lastIndex != -1 && index - lastIndex > lastLength + 2) {
				// 如果大于上次的大小，则中间夹了别的内容在里面
				// 这个时候就要分段了
				content = trimBracket(line.substring(
						lastIndex + lastLength + 2, index));
				for (String string : times) {
					// 将每个时间戳对应的一份句子存入句子集合
					long t = parseTime(string);
					if (t != -1) {
						Log.i(TAG, "line content match-->" + content);
						mLyricSentences.add(new LyricSentence(t, content));
					}
				}
				times.clear();
			}
			times.add(s);
			lastIndex = index;
			lastLength = s.length();

			Log.i(TAG, "time match--->" + s);
		}
		// 如果列表为空，则表示本行没有分析出任何标签
		if (times.isEmpty()) {
			return;
		}

		timeLength = lastLength + 2 + lastIndex;
		if (timeLength > line.length()) {
			content = trimBracket(line.substring(line.length()));
		} else {
			content = trimBracket(line.substring(timeLength));
		}
		Log.i(TAG, "line content match-->" + content);
		// 将每个时间戳对应的一份句子存入句子集合
		for (String s : times) {
			long t = parseTime(s);
			if (t != -1) {
				mLyricSentences.add(new LyricSentence(t, content));
			}
		}
	}

	/** 去除指定字符串中包含[XXX]形式的字符串 */
	private String trimBracket(String content) {
		String s = null;
		String result = content;
		Matcher matcher = mBracketPattern.matcher(content);
		while (matcher.find()) {
			s = matcher.group();
			result = result.replace("[" + s + "]", "");
		}
		return result;
	}

	/** 将歌词的时间字符串转化成毫秒数，如果参数是00:01:23.45 */
	@SuppressLint("DefaultLocale")
	private long parseTime(String strTime) {
		String beforeDot = new String("00:00:00");
		String afterDot = new String("0");

		// 将字符串按小数点拆分成整秒部分和小数部分。
		int dotIndex = strTime.indexOf(".");
		if (dotIndex < 0) {
			beforeDot = strTime;
		} else if (dotIndex == 0) {
			afterDot = strTime.substring(1);
		} else {
			beforeDot = strTime.substring(0, dotIndex);// 00:01:23
			afterDot = strTime.substring(dotIndex + 1); // 45
		}

		long intSeconds = 0;
		int counter = 0;
		while (beforeDot.length() > 0) {
			int colonPos = beforeDot.indexOf(":");
			try {
				if (colonPos > 0) {// 找到冒号了。
					intSeconds *= 60;
					intSeconds += Integer.valueOf(beforeDot.substring(0,
							colonPos));
					beforeDot = beforeDot.substring(colonPos + 1);
				} else if (colonPos < 0) {// 没找到，剩下都当一个数处理了。
					intSeconds *= 60;
					intSeconds += Integer.valueOf(beforeDot);
					beforeDot = "";
				} else {// 第一个就是冒号，不可能！
					return -1;
				}
			} catch (NumberFormatException e) {
				return -1;
			}
			++counter;
			if (counter > 3) {// 不会超过小时，分，秒吧。
				return -1;
			}
		}
		// intSeconds=83

		String totalTime = String.format("%d.%s", intSeconds, afterDot);// totaoTimer
		// =
		// "83.45"
		Double doubleSeconds = Double.valueOf(totalTime); // 转成小数83.45
		return (long) (doubleSeconds * 1000);// 转成毫秒8345
	}

}
