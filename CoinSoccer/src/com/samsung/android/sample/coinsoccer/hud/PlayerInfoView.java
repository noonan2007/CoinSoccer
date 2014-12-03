package com.samsung.android.sample.coinsoccer.hud;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.samsung.android.sample.coinsoccer.R;

public abstract class PlayerInfoView extends RelativeLayout {

	public static class LeftSideImpl extends PlayerInfoView {

		public LeftSideImpl(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public LeftSideImpl(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}

		@Override
		int getLayoutResource() {
			return R.layout.hud_score_shield_left;
		}
	}

	public static class RightSideImpl extends PlayerInfoView {

		public RightSideImpl(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public RightSideImpl(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}

		@Override
		int getLayoutResource() {
			return R.layout.hud_score_shield_right;
		}
	}

	private final TextView mNameView;
	private final TextView mScoreView;
	private final View mMarkerView;

	protected PlayerInfoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	protected PlayerInfoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		inflate(context, getLayoutResource(), this);
		mNameView = (TextView) findViewById(R.id.player_name);
		mScoreView = (TextView) findViewById(R.id.player_score);
		mMarkerView = findViewById(R.id.active_player_marker);
	}

	public void setName(CharSequence name) {
		mNameView.setText(name);
	}

	public void setColor(int color) {
		mMarkerView.setBackgroundColor(color);
	}

	public void setScore(int score) {
		mScoreView.setText(String.valueOf(score));
	}

	public void markAsActive(boolean flag) {
		mMarkerView.setAlpha(flag ? 1f : 0.5f);
	}

	public void hide() {
		setVisibility(GONE);
	}

	public void show() {
		setVisibility(VISIBLE);
	}

	abstract int getLayoutResource();
}