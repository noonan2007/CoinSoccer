/*
 ********************************************************************************
 * Copyright (c) 2013 Samsung Electronics, Inc.
 * All rights reserved.
 *
 * This software is a confidential and proprietary information of Samsung
 * Electronics, Inc. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Samsung Electronics.
 ********************************************************************************
 */
package com.samsung.android.sample.coinsoccer.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;

import com.samsung.android.sample.coinsoccer.R;

public class ColorPickerDialog extends Dialog implements View.OnClickListener {
	
	private final ColorPickerView mColorPickerView;
	private OnColorChangeListener mListener;

	public ColorPickerDialog(Context context) {
		this(context, 0);
	}

	public ColorPickerDialog(Context context, int theme) {
		super(context, theme);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setCanceledOnTouchOutside(true);
		setContentView(R.layout.color_picker_dialog);
		findViewById(android.R.id.button1).setOnClickListener(this);
		findViewById(android.R.id.button2).setOnClickListener(this);
		mColorPickerView = (ColorPickerView) findViewById(R.id.color_picker_view);
	}

	public void setColor(int color) {
		mColorPickerView.setColor(color);
	}

	public void setOnColorChangeListener(OnColorChangeListener listener) {
		mListener = listener;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == android.R.id.button1 && mListener != null) {
			mListener.onColorChanged(mColorPickerView.getColor());
		}
		dismiss();
	}
}
