package com.samsung.android.sample.coinsoccer.settings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.NumberPicker;

import com.samsung.android.sample.coinsoccer.R;

public class NumberPickerDialog extends Dialog implements View.OnClickListener {

	private final NumberPicker mNumberPicker;
	private OnClickListener mListener;

	public NumberPickerDialog(Context context) {
		this(context, 0, null);
	}

	public NumberPickerDialog(Context context, CharSequence title) {
		this(context, 0, title);
	}

	public NumberPickerDialog(Context context, int theme, CharSequence title) {
		super(context, theme);
		if (TextUtils.isEmpty(title)) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		else {
			setTitle(title);
		}
		setContentView(R.layout.number_picker_dialog);
		setCanceledOnTouchOutside(true);
		findViewById(android.R.id.button1).setOnClickListener(this);
		findViewById(android.R.id.button2).setOnClickListener(this);
		mNumberPicker = (NumberPicker) findViewById(R.id.number_picker);
	}

	@Override
	public void onClick(View v) {
		if (mListener != null) {
			switch (v.getId()) {
				case android.R.id.button1:
					mListener.onClick(this, BUTTON_POSITIVE);
					break;
				case android.R.id.button2:
					mListener.onClick(this, BUTTON_NEGATIVE);
					break;
			}
		}
		dismiss();
	}

	public void setOnClickListener(DialogInterface.OnClickListener listener) {
		mListener = listener;
	}

	public NumberPicker getNumberPicker() {
		return mNumberPicker;
	}
}
