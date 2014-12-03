package com.samsung.android.sample.coinsoccer.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.samsung.android.sample.coinsoccer.R;
import com.samsung.android.sample.coinsoccer.settings.GamePreferences.LimitType;
import com.samsung.android.sample.coinsoccer.settings.NumberPickerTextViewWrapper.OnValuePickedListener;

public class GameSettingsView extends LinearLayout implements OnClickListener,
		OnCheckedChangeListener, OnValuePickedListener {

	private NumberPickerTextViewWrapper mTimePerTurnPicker;
	private NumberPickerTextViewWrapper mTurnLimitPicker;
	private NumberPickerTextViewWrapper mGoalLimitPicker;
	private CheckBox mTurnLimitCheckbox;
	private CheckBox mGoalLimitCheckbox;

	public GameSettingsView(Context context) {
		super(context);
		initUi();
	}

	public GameSettingsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initUi();
	}

	public GameSettingsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initUi();
	}

	public void destroyDialogs() {
		mTimePerTurnPicker.onDestroy();
		mTurnLimitPicker.onDestroy();
		mGoalLimitPicker.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.turns_limit_checkbox:
				mGoalLimitCheckbox.setChecked(!mTurnLimitCheckbox.isChecked());
				break;
			case R.id.goals_limit_checkbox:
				mTurnLimitCheckbox.setChecked(!mGoalLimitCheckbox.isChecked());
				break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
			case R.id.turns_limit_checkbox:
				mTurnLimitPicker.getView().setEnabled(isChecked);
				mGoalLimitPicker.getView().setEnabled(!isChecked);
				break;
			case R.id.goals_limit_checkbox:
				mGoalLimitPicker.getView().setEnabled(isChecked);
				mTurnLimitPicker.getView().setEnabled(!isChecked);
				break;
		}
	}

	@Override
	public void onValuePicked(NumberPickerTextViewWrapper source, int index, int value, String label) {
		switch (source.getView().getId()) {
			case R.id.turns_limit_picker:

				break;
			case R.id.goals_limit_picker:

				break;
			case R.id.time_per_turn_picker:

				break;
		}
	}

	public void setGameSettings(int timePerTurn, int turnLimit, int goalLimit, LimitType limitType) {
		mTimePerTurnPicker.selectValue(timePerTurn);
		mTurnLimitPicker.selectValue(turnLimit);
		mGoalLimitPicker.selectValue(goalLimit);
		switch (limitType) {
			case TURN_LIMIT:
				mTurnLimitCheckbox.setChecked(true);
				mGoalLimitCheckbox.setChecked(false);
				break;
			case SCORE_LIMIT:
				mTurnLimitCheckbox.setChecked(false);
				mGoalLimitCheckbox.setChecked(true);
				break;
		}
	}

	public int getTimePerTurnMillis() {
		return mTimePerTurnPicker.getValue();
	}

	public int getTurnLimit() {
		return mTurnLimitPicker.getValue();
	}

	public int getScoreLimit() {
		return mGoalLimitPicker.getValue();
	}

	public LimitType getLimitType() {
		if (mTurnLimitCheckbox.isChecked()) {
			return LimitType.TURN_LIMIT;
		}
		else if (mGoalLimitCheckbox.isChecked()) {
			return LimitType.SCORE_LIMIT;
		}
		return null;
	}

	private void initUi() {
		inflate(getContext(), R.layout.game_settings_view, this);

		mTurnLimitCheckbox = (CheckBox) findViewById(R.id.turns_limit_checkbox);
		mTurnLimitCheckbox.setOnClickListener(this);
		mTurnLimitCheckbox.setOnCheckedChangeListener(this);

		mGoalLimitCheckbox = (CheckBox) findViewById(R.id.goals_limit_checkbox);
		mGoalLimitCheckbox.setOnClickListener(this);
		mGoalLimitCheckbox.setOnCheckedChangeListener(this);

		mTimePerTurnPicker = new NumberPickerTextViewWrapper(
				(TextView) findViewById(R.id.time_per_turn_picker),
				GamePreferences.TIME_PER_TURN_MILLIS, "Time per turn") {

			@Override
			protected String[] createDefaultLabels(int[] values) {
				String[] labels = new String[values.length];
				for (int i = 0; i < values.length; i++) {
					labels[i] = String.valueOf(values[i] / 1000);
				}
				return labels;
			}
		};
		mTimePerTurnPicker.setOnValuePickedListener(this);

		mTurnLimitPicker = new NumberPickerTextViewWrapper(
				(TextView) findViewById(R.id.turns_limit_picker),
				GamePreferences.TURN_LIMIT_VALUES, "Turn limit");
		mTurnLimitPicker.setOnValuePickedListener(this);

		mGoalLimitPicker = new NumberPickerTextViewWrapper(
				(TextView) findViewById(R.id.goals_limit_picker),
				GamePreferences.GOAL_LIMIT_VALUES, "Goal limit");
		mGoalLimitPicker.setOnValuePickedListener(this);
	}
}
