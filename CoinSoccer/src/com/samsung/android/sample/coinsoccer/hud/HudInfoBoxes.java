package com.samsung.android.sample.coinsoccer.hud;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.samsung.android.sample.coinsoccer.R;

public class HudInfoBoxes implements Handler.Callback {

	private static final int ARG_HIDE = 1;
	private static final int ARG_SHOW = 2;
	private static final float SHOT_CANCEL_LABEL_X_MARGIN_DIP = 15;
	private static final float SHOT_CANCEL_LABEL_Y_MARGIN_DIP = 30;

	private final Handler mUiHandler;
	private final Activity mActivity;
	private final Point mPointHelperObject;
	private final PointF mShotCancelLabelMarginPixels;

	public HudInfoBoxes(Activity activity) {
		this(activity, null);
	}

	public HudInfoBoxes(Activity activity, Looper looper) {
		mPointHelperObject = new Point();
		mActivity = activity;
		mUiHandler = new Handler(looper == null ? Looper.getMainLooper() : looper, this);
		DisplayMetrics metrics = mActivity.getResources().getDisplayMetrics();
		mShotCancelLabelMarginPixels = new PointF(
				TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
						SHOT_CANCEL_LABEL_X_MARGIN_DIP, metrics),
				TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
						SHOT_CANCEL_LABEL_Y_MARGIN_DIP, metrics));
	}

	public void showAssetsLoadingDialog() {
		mUiHandler.obtainMessage(R.id.info_box_assets_loading, ARG_SHOW, 0).sendToTarget();
	}

	public void hideAssetsLoadingDialog() {
		mUiHandler.obtainMessage(R.id.info_box_assets_loading, ARG_HIDE, 0).sendToTarget();
	}

	public void showGoalShotDialog(int... scores) {
		mUiHandler.obtainMessage(R.id.info_box_goal_shot, ARG_SHOW, 0, scores).sendToTarget();
	}

	public void hideGoalShotDialog() {
		mUiHandler.obtainMessage(R.id.info_box_goal_shot, ARG_HIDE, 0).sendToTarget();
	}

	public void showFoulCommitedDialog() {
		mUiHandler.obtainMessage(R.id.info_box_foul, ARG_SHOW, 0).sendToTarget();
	}

	public void hideFoulCommitedDialog() {
		mUiHandler.obtainMessage(R.id.info_box_foul, ARG_HIDE, 0).sendToTarget();
	}

	public void showTurnExpiredDialog() {
		mUiHandler.obtainMessage(R.id.info_box_turn_expired, ARG_SHOW, 0).sendToTarget();
	}

	public void hideTurnExpiredDialog() {
		mUiHandler.obtainMessage(R.id.info_box_turn_expired, ARG_HIDE, 0).sendToTarget();
	}

	public void showGoalAreaCleaningInfo() {
		mUiHandler.obtainMessage(R.id.info_box_cleaning_goal_area, ARG_SHOW, 0).sendToTarget();
	}

	public void hideGoalAreaCleaningInfo() {
		mUiHandler.obtainMessage(R.id.info_box_cleaning_goal_area, ARG_HIDE, 0).sendToTarget();
	}

	public void gamePauseInfo(boolean isGamePaused) {
		gamePauseInfo(isGamePaused, false);
	}

	public void gamePauseInfo(boolean isGamePaused, boolean remotePauseFlag) {
		mUiHandler.obtainMessage(R.id.info_box_pause,
				isGamePaused || remotePauseFlag ? ARG_SHOW : ARG_HIDE,
				(isGamePaused ? 1 : 0) | (remotePauseFlag ? 2 : 0)).sendToTarget();
	}

	public void remotePlayerPreparingShot(boolean show) {
		mUiHandler.obtainMessage(R.id.info_box_remote_player_preparing_shot,
				show ? ARG_SHOW : ARG_HIDE, 0).sendToTarget();
	}

	public void shotCancelInfo(boolean show, float... screenCoords) {
		mUiHandler.obtainMessage(R.id.info_box_shot_cancel,
				show ? ARG_SHOW : ARG_HIDE, 0, screenCoords).sendToTarget();
	}

	@Override
	public boolean handleMessage(Message msg) {
		final TextView infoBoxView = (TextView) mActivity.findViewById(msg.what);
		switch (msg.arg1) {
			case ARG_HIDE:
				infoBoxView.setVisibility(View.GONE);
				return true;
			case ARG_SHOW:
				infoBoxView.setVisibility(View.VISIBLE);
				onShowInfoBox(infoBoxView, msg);
				return true;
		}
		return false;
	}

	private void onShowInfoBox(TextView v, Message msg) {
		switch (msg.what) {

			case R.id.info_box_goal_shot:
				int[] scores = (int[]) msg.obj;
				v.setText(mActivity.getString(R.string.info_msg_goal, scores[0], scores[1]));
				break;

			case R.id.info_box_turn_expired:
				v.setText(R.string.info_msg_turn_expired);
				break;

			case R.id.info_box_pause:
				boolean local = (msg.arg2 & 1) == 1, remote = (msg.arg2 & 2) == 2;
				if (local && remote) {
					v.setText(R.string.info_msg_paused_locally_and_remote);
				}
				else if (local) {
					v.setText(R.string.info_msg_paused_locally);
				}
				else if (remote) {
					v.setText(R.string.info_msg_paused_remote);
				}
				break;

			case R.id.info_box_shot_cancel:
				float[] screenCoords = (float[]) msg.obj;
				mActivity.getWindowManager().getDefaultDisplay().getSize(mPointHelperObject);
				if (v.getWidth() > mPointHelperObject.x - mShotCancelLabelMarginPixels.x * 2) {
					v.setWidth(mPointHelperObject.x - (int) mShotCancelLabelMarginPixels.x * 2);
				}
				float x = screenCoords[0] - v.getWidth() / 2;
				if (x < 0) {
					x = mShotCancelLabelMarginPixels.x;
				}
				else if (x + v.getWidth() > mPointHelperObject.x) {
					x = mPointHelperObject.x - v.getWidth() - mShotCancelLabelMarginPixels.x;
				}
				v.setX(x);
				float y = screenCoords[1] + mShotCancelLabelMarginPixels.y;
				if (y + v.getHeight() > mPointHelperObject.y) {
					y = screenCoords[1] - v.getHeight() - mShotCancelLabelMarginPixels.y;
				}
				v.setY(y);
				break;
		}
	}
}