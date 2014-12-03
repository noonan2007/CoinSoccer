package com.samsung.android.sample.coinsoccer.settings;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.samsung.android.sample.coinsoccer.R;
import com.samsung.android.sample.coinsoccer.colorpicker.ColorPickerView;

public class RemoteGameSettingsViewTag implements OnClickListener {

	public interface OnItemSelectedListener {

		void onItemSelected(String nodeName);
	}

	private final View mColor;
	private final TextView mSummary;
	private final OnItemSelectedListener mListener;
	private String mNodeName;

	public RemoteGameSettingsViewTag(View v, OnItemSelectedListener listener) {
		mColor = v.findViewById(R.id.color_display);
		mSummary = (TextView) v.findViewById(R.id.text_target);
		mListener = listener;
		if (mListener != null) {
			v.findViewById(R.id.join_game_button).setOnClickListener(this);
		}
		v.setTag(this);
	}

	public void setEntry(String nodeName, GameSettings gameSettings,
			PlayerSettings playerSettings) {
		mNodeName = nodeName;
		ColorPickerView.updateDisplayedColor(mColor, playerSettings.color);
		String limitInfo;
		Context c = mSummary.getContext();
		if (gameSettings.hasScoreLimit()) {
			limitInfo = c.getString(R.string.goal_limit, gameSettings.scoreLimit);
		}
		else if (gameSettings.hasTurnLimit()) {
			limitInfo = c.getString(R.string.turn_limit, gameSettings.turnLimit);
		}
		else {
			limitInfo = c.getString(R.string.no_limit);
		}
		mSummary.setText(Html.fromHtml(c.getString(
				R.string.remote_game_settings_summary, playerSettings.name,
				gameSettings.timePerTurnMillis / 1000, limitInfo)));
	}

	@Override
	public void onClick(View v) {
		mListener.onItemSelected(mNodeName);
	}
}
