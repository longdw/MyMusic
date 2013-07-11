/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ldw.music.model.MusicInfo;
import com.ldw.music.model.SentenceModel;

public class LrcUtil {
	public StringBuffer getUNICODE(String source) {
		StringBuffer sb = new StringBuffer();
		try {
			for (int i = 0; i < source.length(); i++) {
				Character c = Character.toLowerCase(source.charAt(i));
				if (c <= 256) {
					sb.append(Integer.toHexString(c).toUpperCase() + "00");
				} else {
					String s = URLEncoder.encode(c.toString(), "UTF-16LE")
							.replace("%", "");
					sb.append(s);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb;
	}

	private StringBuffer getUTF_8(String source) {
		StringBuffer sb = new StringBuffer();
		try {
			for (int i = 0; i < source.length(); i++) {
				Character c = source.charAt(i);
				if (c <= 256) {
					sb.append(Integer.toHexString(c).toUpperCase());
				} else {
					String s = URLEncoder.encode(c.toString(), "UTF-8")
							.replace("%", "");
					sb.append(s);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb;
	}

	public String createCode(String singer, String title, int lrcId)
			throws UnsupportedEncodingException {
		StringBuffer qqHexStr = getUTF_8(singer + title);
		int length = qqHexStr.length() / 2;
		int[] song = new int[length];
		for (int i = 0; i < length; i++) {
			song[i] = Integer
					.parseInt(qqHexStr.substring(2 * i, 2 * i + 2), 16);
		}
		int t1, t2, t3;
		t1 = t2 = t3 = 0;
		t1 = (lrcId & 0x0000FF00) >> 8;
		if ((lrcId & 0x00FF0000) == 0) {
			t3 = 0x000000FF & ~t1;
		} else {
			t3 = 0x000000FF & ((lrcId & 0x00FF0000) >> 16);
		}
		t3 = t3 | ((0x000000FF & lrcId) << 8);
		t3 = t3 << 8;
		t3 = t3 | (0x000000FF & t1);
		t3 = t3 << 8;
		if ((lrcId & 0xFF000000) == 0) {
			t3 = t3 | (0x000000FF & (~lrcId));
		} else {
			t3 = t3 | (0x000000FF & (lrcId >> 24));
		}
		int j = length - 1;
		while (j >= 0) {
			int c = song[j];
			if (c >= 0x80)
				c = c - 0x100;
			t1 = (int) ((c + t2) & 0x00000000FFFFFFFF);
			t2 = (int) ((t2 << (j % 2 + 4)) & 0x00000000FFFFFFFF);
			t2 = (int) ((t1 + t2) & 0x00000000FFFFFFFF);
			j -= 1;
		}
		j = 0;
		t1 = 0;
		while (j <= length - 1) {
			int c = song[j];
			if (c >= 128)
				c = c - 256;
			int t4 = (int) ((c + t1) & 0x00000000FFFFFFFF);
			t1 = (int) ((t1 << (j % 2 + 3)) & 0x00000000FFFFFFFF);
			t1 = (int) ((t1 + t4) & 0x00000000FFFFFFFF);
			j += 1;
		}
		int t5 = (int) conv(t2 ^ t3);
		t5 = (int) conv(t5 + (t1 | lrcId));
		t5 = (int) conv(t5 * (t1 | t3));
		t5 = (int) conv(t5 * (t2 ^ lrcId));
		long t6 = (long) t5;
		if (t6 > 2147483648L)
			t5 = (int) (t6 - 4294967296L);
		return String.valueOf(t5);
	}

	private long conv(int i) {
		long r = i % 4294967296L;
		if (i >= 0 && r > 2147483648L)
			r = r - 4294967296L;
		if (i < 0 && r < 2147483648L)
			r = r + 4294967296L;
		return r;
	}

	public List<SentenceModel> parseLrc(String line, MusicInfo music) {
		if (line.equals(""))
			return null;
		List<SentenceModel> list = new ArrayList<SentenceModel>();
		final Pattern pattern = Pattern.compile("(?<=\\[).*?(?=\\])");
		Matcher matcher = pattern.matcher(line);
		List<String> temp = new ArrayList<String>();
		int lastIndex = -1;// 最后一个时间标签的下标
		int lastLength = -1;// 最后一个时间标签的长度
		while (matcher.find()) {
			String s = matcher.group();
			int index = line.indexOf("[" + s + "]");
			if (lastIndex != -1 && index - lastIndex > lastLength + 2) {
				// 如果大于上次的大小，则中间夹了别的内容在里面
				// 这个时候就要分段了
				String content = line.substring(lastIndex + lastLength + 2,
						index);
				for (String str : temp) {
					long t = parseTime(str);
					if (t != -1) {
						System.out.println("content = " + content);
						System.out.println("t = " + t);
						list.add(new SentenceModel(content, t));
					}
				}
				temp.clear();
			}
			temp.add(s);
			lastIndex = index;
			lastLength = s.length();
		}
		// 如果列表为空，则表示本行没有分析出任何标签
		if (temp.isEmpty()) {
			return null;
		}
		Collections.sort(list, new Comparator<SentenceModel>() {

			public int compare(SentenceModel o1, SentenceModel o2) {
				return (int) (o1.getFromTime() - o2.getFromTime());
			}
		});
		// 处理第一句歌词的起始情况,无论怎么样,加上歌名做为第一句歌词,并把它的
		// 结尾为真正第一句歌词的开始
		if (list.size() == 0) {
			list.add(new SentenceModel(music.musicName, 0, Integer.MAX_VALUE));
			return list;
		} else {
			SentenceModel first = list.get(0);
			list.add(0,
					new SentenceModel(music.musicName, 0, first.getFromTime()));
		}
		int size = list.size();
		for (int i = 0; i < size; i++) {
			SentenceModel next = null;
			if (i + 1 < size) {
				next = list.get(i + 1);
			}
			SentenceModel now = list.get(i);
			if (next != null) {
				now.setToTime(next.getFromTime() - 1);
			}
		}
		// 如果就是没有怎么办,那就只显示一句歌名了
		if (list.size() == 1) {
			list.get(0).setToTime(Integer.MAX_VALUE);
		} else {
			SentenceModel last = list.get(list.size() - 1);
			last.setToTime(music == null ? Integer.MAX_VALUE
					: music.duration * 1000 + 1000);
		}
		return list;
	}

	/**
	 * 把如00:00.00这样的字符串转化成 毫秒数的时间，比如 01:10.34就是一分钟加上10秒再加上340毫秒 也就是返回70340毫秒
	 * 
	 * @param time
	 *            字符串的时间
	 * @return 此时间表示的毫秒
	 */
	private long parseTime(String time) {
		String[] ss = time.split("\\:|\\.");
		// 如果 是两位以后，就非法了
		if (ss.length < 2) {
			return -1;
		} else if (ss.length == 2) {// 如果正好两位，就算分秒
			try {
				int min = Integer.parseInt(ss[0]);
				int sec = Integer.parseInt(ss[1]);
				if (min < 0 || sec < 0 || sec >= 60) {
					throw new RuntimeException("数字不合法!");
				}
				// System.out.println("time" + (min * 60 + sec) * 1000L);
				return (min * 60 + sec) * 1000L;
			} catch (Exception exe) {
				return -1;
			}
		} else if (ss.length == 3) {// 如果正好三位，就算分秒，十毫秒
			try {
				int min = Integer.parseInt(ss[0]);
				int sec = Integer.parseInt(ss[1]);
				int mm = Integer.parseInt(ss[2]);
				if (min < 0 || sec < 0 || sec >= 60 || mm < 0 || mm > 99) {
					throw new RuntimeException("数字不合法!");
				}
				// System.out.println("time" + (min * 60 + sec) * 1000L + mm *
				// 10);
				return (min * 60 + sec) * 1000L + mm * 10;
			} catch (Exception exe) {
				return -1;
			}
		} else {// 否则也非法
			return -1;
		}
	}
}
