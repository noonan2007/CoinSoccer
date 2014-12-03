package com.samsung.android.sample.coinsoccer.colorpicker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.samsung.android.sample.coinsoccer.R;

public class ColorPickerView extends FrameLayout implements OnSeekBarChangeListener {

	public static void updateDisplayedColor(View view, int color) {
		Drawable drawable = null;
		if (view instanceof ImageView) {
			drawable = ((ImageView) view).getDrawable();
		}
		if (drawable == null) {
			drawable = view.getBackground();
		}
		if (drawable == null) {
			view.setBackgroundColor(color);
		}
		else {
			if (drawable instanceof LayerDrawable) {
				drawable = ((LayerDrawable) drawable).getDrawable(0);
			}
			drawable.mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
		}
	}

	private SeekBar mRedBar;
	private SeekBar mBlueBar;
	private SeekBar mGreenBar;
	private View mColorDisplay;
	private OnColorChangeListener mListener;
	private int mColor;

	public ColorPickerView(Context context) {
		super(context);
		initUi();
	}

	public ColorPickerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initUi();
	}

	public ColorPickerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initUi();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser) {
			mColor = createColor(mRedBar.getProgress(), mGreenBar.getProgress(), 
					mBlueBar.getProgress());
			updateDisplayedColor(mColorDisplay, mColor);
			if (mListener != null) {
				mListener.onColorChanged(mColor);
			}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {}

	public void setOnColorChangeListener(OnColorChangeListener listener) {
		mListener = listener;
	}

	/**
	 * Sets RGB bars to represent given color.
	 * 
	 * @param color
	 *            - Color to present on dialog.
	 */
	public void setColor(int color) {
		mRedBar.setProgress(Color.red(color));
		mGreenBar.setProgress(Color.green(color));
		mBlueBar.setProgress(Color.blue(color));
		updateDisplayedColor(mColorDisplay, mColor = color);
	}

	public int getColor() {
		return mColor;
	}

	private void initUi() {
		LayoutInflater.from(getContext()).inflate(R.layout.color_picker_view, this);
		mColorDisplay = findViewById(R.id.color_display);
		mRedBar = (SeekBar) findViewById(R.id.red_bar);
		mRedBar.setMax(255);
		mRedBar.setOnSeekBarChangeListener(this);
		mGreenBar = (SeekBar) findViewById(R.id.green_bar);
		mGreenBar.setMax(255);
		mGreenBar.setOnSeekBarChangeListener(this);
		mBlueBar = (SeekBar) findViewById(R.id.blue_bar);
		mBlueBar.setMax(255);
		mBlueBar.setOnSeekBarChangeListener(this);
	}

	/**
	 * Creates color integer from given RGB values.
	 * 
	 * @param red
	 * @param green
	 * @param blue
	 * @return
	 */
	private int createColor(int red, int green, int blue) {
		return Color.rgb(red, green, blue);
	}
}
