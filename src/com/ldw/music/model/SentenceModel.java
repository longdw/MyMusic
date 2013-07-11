/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.model;

import java.io.Serializable;

public class SentenceModel implements Serializable {
	private static final long serialVersionUID = 20071125L;
	private long fromTime;// 这句的起始时间,时间是以毫秒为单位
	private long toTime;// 这一句的结束时间
	private String content;// 这一句的内容

	public SentenceModel(String content, long fromTime, long toTime) {
		this.content = content;
		this.fromTime = fromTime;
		this.toTime = toTime;
	}

	public SentenceModel(String content, long fromTime) {
		this(content, fromTime, 0);
	}

	public SentenceModel(String content) {
		this(content, 0, 0);
	}

	public long getFromTime() {
		return fromTime;
	}

	public void setFromTime(long fromTime) {
		this.fromTime = fromTime;
	}

	public long getToTime() {
		return toTime;
	}

	public void setToTime(long toTime) {
		this.toTime = toTime;
	}

	/**
	 * 检查某个时间是否包含在某句中间
	 * 
	 * @param time 时间
	 * @return 是否包含了
	 */
	public boolean isInTime(long time) {
		return time >= fromTime && time <= toTime;
	}

	/**
	 * 得到这一句的内容
	 * 
	 * @return 内容
	 */
	public String getContent() {
		return content;
	}

	/**
	 * 得到这个句子的时间长度,毫秒为单位
	 * 
	 * @return 长度
	 */
	public long getDuring() {
		return toTime - fromTime;
	}

	public String toString() {
		return "{" + fromTime + "(" + content + ")" + toTime + "}";
	}
}
