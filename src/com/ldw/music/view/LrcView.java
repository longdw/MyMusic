/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import com.ldw.music.model.LyricModel;
import com.ldw.music.model.SentenceModel;

public class LrcView extends ScrollView{



	private LyricModel lyricModel;
	private List<SentenceModel> list;
	private List<TextView>	textList;
	private final LinearLayout.LayoutParams linerParams = new LinearLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT);
	private final LayoutParams textParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,DY);
	private Scroller mScroller;

	private int   index = 0;

	private float  midHeight;
	private static final int DY = 30; // 每一行的间隔
	private static final float fontSize = 16;

	/***
	 * 配合Scroller 进行动态滚动
	 */
	@Override
	public void computeScroll() {
		// TODO Auto-generated method stub
		super.computeScroll();
		if(mScroller.computeScrollOffset())
		{
			this.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			this.postInvalidate();
		}
	}


	public LrcView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	public LrcView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}

	public LrcView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public void setLyric(LyricModel model)
	{
		this.lyricModel = model;

		LayoutParams spaceParams = new LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, (int) midHeight);
		this.list = model.getSentenceList();
		LinearLayout layout = new LinearLayout(this.getContext());
		layout.setOrientation(LinearLayout.VERTICAL);

		layout.setLayoutParams(linerParams);

		View spaceView = new View(layout.getContext());
		spaceView.setLayoutParams(spaceParams);
		layout.addView(spaceView);

		for (SentenceModel sentence : this.list) {
			TextView textView = new TextView(layout.getContext());
			textView.setText(sentence.getContent());
			textView.setVisibility(VISIBLE);
			textView.setGravity(Gravity.CENTER);
			textView.setTextColor(Color.WHITE);
			textView.setLayoutParams(textParams);
			textList.add(textView);
			layout.addView(textView);
		}
		spaceView = new View(layout.getContext());
		spaceView.setLayoutParams(spaceParams);
		layout.addView(spaceView);
		layout.setVisibility(VISIBLE);
		this.addView(layout);

	}

	public void clear()
	{
		this.removeAllViews();
		textList.clear();
		this.list		= null;
		this.scrollTo(0, 0);
		index			= 0;
	}

	private void init()
	{
		textList		= new ArrayList<TextView>();
		mScroller		= new Scroller(this.getContext());
	}

	/**
	 * 更新歌词序列（From yoyoplayer）
	 * @param time
	 * @return
	 */
	public void updateIndex(long time) {
		if( lyricModel == null )return ;

		// 歌词序号
		int t  = lyricModel.getNowSentenceIndex(time);
		if (index == -1)
			return ;
		if( index != t )
		{
			TextView oldOne = textList.get(index);
			oldOne.setTextColor(Color.WHITE);
			TextView newOne = textList.get(t);
			newOne.setTextColor(Color.YELLOW);
			int oldHeight = oldOne.getTop();
			index = t;
			Log.v("oldheight", String.valueOf(oldHeight));
			mScroller.startScroll(this.getScrollX(), (int)(oldHeight + fontSize - midHeight + DY / 2), 0, 30, 800);

		}
		return ;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		midHeight = h * 0.5f;
	}

	/***
	 * 用于禁止手动滚动歌词
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return false;
	}



}
