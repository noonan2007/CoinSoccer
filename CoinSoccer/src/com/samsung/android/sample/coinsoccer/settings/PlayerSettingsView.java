package com.samsung.android.sample.coinsoccer.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.samsung.android.sample.coinsoccer.R;
import com.samsung.android.sample.coinsoccer.colorpicker.ColorPickerDialog;
import com.samsung.android.sample.coinsoccer.colorpicker.ColorPickerView;
import com.samsung.android.sample.coinsoccer.colorpicker.OnColorChangeListener;

public class PlayerSettingsView extends RelativeLayout implements OnClickListener, OnColorChangeListener {

	private ColorPickerDialog mColorPickerDialog;

	protected TextView mPlayerName;
	protected View mColorView;
	protected int mColor = 0xFFFFFFFF;

	private View mEditIcon;

	public PlayerSettingsView(Context context) {
		super(context);
		initUi();
	}

	public PlayerSettingsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initUi();
	}

	public PlayerSettingsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initUi();
	}

	@Override
	public void onClick(View v) {
		if (mColorPickerDialog == null) {
			mColorPickerDialog = new ColorPickerDialog(getContext());
			mColorPickerDialog.setColor(mColor);
			mColorPickerDialog.setOnColorChangeListener(this);
		}
		else {
			mColorPickerDialog.setColor(mColor);
		}
		mColorPickerDialog.show();
	}

	@Override
	public void onColorChanged(int color) {
		setColor(color);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		mPlayerName.setEnabled(enabled);
		mColorView.setEnabled(enabled);
		mEditIcon.setVisibility(enabled ? VISIBLE : GONE);
	}

	public void setColor(int color) {
		ColorPickerView.updateDisplayedColor(mColorView, mColor = color);
	}

	public void setName(String name) {
		mPlayerName.setText(name);
	}

	public int getColor() {
		return mColor;
	}

	public String getName() {
		return mPlayerName.getText().toString();
	}

	public void destroyDialog() {
		if (mColorPickerDialog != null) {
			mColorPickerDialog.dismiss();
			mColorPickerDialog = null;
		}
	}

	private void initUi() {
		inflate(getContext(), R.layout.player_settings_view, this);
		mPlayerName = (TextView) findViewById(R.id.player_name);
		mColorView = findViewById(R.id.player_color);
		mColorView.setOnClickListener(this);
		mEditIcon = findViewById(R.id.edit_icon);
	}
}
