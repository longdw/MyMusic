/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.uimanager;

import android.view.View;

public abstract class MainUIManager {
	
	protected abstract void setBgByPath(String path);
	public abstract View getView();
	public abstract View getView(int from);
	public abstract View getView(int from, Object obj);
}
