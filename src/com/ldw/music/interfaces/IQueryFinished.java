/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.interfaces;

import java.util.List;

import com.ldw.music.model.MusicInfo;

public interface IQueryFinished {
	
	public void onFinished(List<MusicInfo> list);

}
