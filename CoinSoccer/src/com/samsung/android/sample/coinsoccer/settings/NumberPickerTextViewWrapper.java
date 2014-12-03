package com.samsung.android.sample.coinsoccer.settings;

import android.content.DialogInterface;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

public class NumberPickerTextViewWrapper implements View.OnClickListener, DialogInterface.OnClickListener {

	public interface OnValuePickedListener {

		void onValuePicked(NumberPickerTextViewWrapper source,
				int index, int value, String label);
	}

	private final TextView mView;
	private final String[] mLabels;
	private final int[] mValues;
	private final CharSequence mTitle;
	private NumberPickerDialog mPickerDialog;
	private int mSelectedIndex;
	private OnValuePickedListener mListener;

	public NumberPickerTextViewWrapper(TextView view, int[] values) {
		this(view, values, null);
	}

	public NumberPickerTextViewWrapper(TextView view, int[] values, int selectedIndex) {
		this(view, values, null, selectedIndex);
	}

	public NumberPickerTextViewWrapper(TextView view, int[] values, CharSequence title) {
		this(view, values, title, values.length / 2);
	}

	public NumberPickerTextViewWrapper(TextView view, int[] values, CharSequence title,
			int selectedIndex) {
		mValues = values;
		mLabels = createDefaultLabels(values);
		mView = view;
		mTitle = title;
		mView.setOnClickListener(this);
		selectIndex(selectedIndex);
	}

	public void setOnValuePickedListener(OnValuePickedListener listener) {
		mListener = listener;
	}

	public void selectIndex(int value) {
		if (mPickerDialog != null && mPickerDialog.isShowing()) {
			mPickerDialog.getNumberPicker().setValue(mSelectedIndex);
		}
		mSelectedIndex = value;
		updateLabel();
	}

	public void selectValue(int value) {
		for (int i = 0; i < mValues.length; i++) {
			if (value == mValues[i]) {
				selectIndex(i);
				return;
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (mPickerDialog == null) {
			mPickerDialog = new NumberPickerDialog(v.getContext(), mTitle);
			mPickerDialog.setOnClickListener(this);
			NumberPicker numberPicker = mPickerDialog.getNumberPicker();
			numberPicker.setMinValue(0);
			numberPicker.setMaxValue(mLabels.length - 1);
			numberPicker.setDisplayedValues(mLabels);
			numberPicker.setValue(mSelectedIndex);
		}
		else {
			mPickerDialog.getNumberPicker().setValue(mSelectedIndex);
		}
		mPickerDialog.show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			mSelectedIndex = mPickerDialog.getNumberPicker().getValue();
			updateLabel();
			if (mListener != null) {
				mListener.onValuePicked(this, getSelectedIndex(), getValue(), getLabel());
			}
		}
	}

	public TextView getView() {
		return mView;
	}
	
	public int getSelectedIndex() {
		return mSelectedIndex;
	}

	public int getValue() {
		return mValues[mSelectedIndex];
	}

	public String getLabel() {
		return mLabels[mSelectedIndex];
	}

	public void onDestroy() {
		if (mPickerDialog != null) {
			mPickerDialog.dismiss();
			mPickerDialog = null;
		}
	}

	protected void updateLabel() {
		mView.setText(getLabel());
	}

	protected String[] createDefaultLabels(int[] values) {
		String[] labels = new String[values.length];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = String.valueOf(values[i]);
		}
		return labels;
	}
}
