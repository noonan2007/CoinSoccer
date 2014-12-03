package com.samsung.android.sample.coinsoccer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;

public class WindowSizeHelper {

	public static final float DEFAULT_PADDING_DIP = 15;

	public static void adjustWindowSize(Activity activity) {
		adjustWindowSize(activity, DEFAULT_PADDING_DIP);
	}

	public static void adjustWindowSize(Activity activity, float paddingDip) {
		int paddingPixels = (int) dipToPixels(activity, paddingDip);
		Point winSize = new Point();
		activity.getWindowManager().getDefaultDisplay().getSize(winSize);
		WindowManager.LayoutParams attr = activity.getWindow().getAttributes();
		int barHeight = getStatusBarHeight(activity.getResources());
		attr.width = winSize.x - 2 * paddingPixels;
		attr.height = winSize.y - 2 * paddingPixels - barHeight;
		attr.y = paddingPixels + barHeight;
		attr.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
	}

	public static float dipToPixels(Context context, float dip) {
		return TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, dip,
				context.getResources().getDisplayMetrics());
	}

	public static int getStatusBarHeight(Resources r) {
		int result = 0;
		int resourceId = r.getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = r.getDimensionPixelSize(resourceId);
		}
		return result;
	}

}