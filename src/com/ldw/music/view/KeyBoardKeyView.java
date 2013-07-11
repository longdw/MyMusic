/**
 * Copyright (c) www.longdw.com
 */
package com.ldw.music.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ImageView;

/**
 * 自定义搜索页的键盘
 * @author longdw(longdawei1988@gmail.com)
 *
 */
public class KeyBoardKeyView extends ImageView {

	private static TextPaint mTextPaint;
	private static int cent1;

	static {
		mTextPaint = new TextPaint();
		mTextPaint.setTypeface(Typeface.SANS_SERIF);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		mTextPaint.setAntiAlias(true);
	}

	private int cent2;
	private String content="";

	public KeyBoardKeyView(Context context) {
		super(context);
		init(context);
	}

	public KeyBoardKeyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public KeyBoardKeyView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		DisplayMetrics displayMetrics = context.getResources()
				.getDisplayMetrics();
		float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
				17.0F, displayMetrics);
		mTextPaint.setTextSize(textSize);
		Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
		cent1 = (int) (fontMetrics.descent - fontMetrics.ascent);
		cent2 = (int) fontMetrics.descent + 1 >> 1;
	}

	public final void init(String text) {
		content = text;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (TextUtils.isEmpty(content)) {
			return;
		}
		TextPaint paint = mTextPaint;
		if (isPressed()) {
			paint.setColor(Color.WHITE);
		} else {
			paint.setColor(0xff8f8f8f);
		}
		float x = getWidth() >> 1;
		int n = getHeight() - cent1 >> 1;
		int i1 = getHeight() - n;
		int i2 = cent2;
		float y = i1 - i2;
		canvas.drawText(content, x, y, paint);
	}

}
