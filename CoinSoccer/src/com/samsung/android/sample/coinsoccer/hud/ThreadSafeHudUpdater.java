package com.samsung.android.sample.coinsoccer.hud;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.samsung.android.sample.coinsoccer.settings.Which;

public class ThreadSafeHudUpdater implements Handler.Callback {

	private static final int WHAT_TURN_END_COUNTER_SHOW = 1;
	private static final int WHAT_TURN_END_COUTNER_HIDE = 2;
	private static final int WHAT_TURN_END_COUNTER_UPDATE = 3;
	private static final int WHAT_SCORE_UPDATE = 4;
	private static final int TURN_END_COUNTER_UPDATE_INTERVAL_MILLIS = 100;

	private final Handler mHandler;
	private final HudViewWrapper mHudView;
	private int mLastUpdateMillis;

	public ThreadSafeHudUpdater(HudViewWrapper hudView) {
		this(hudView, null);
	}

	public ThreadSafeHudUpdater(HudViewWrapper hudView, Looper looper) {
		mHudView = hudView;
		mHandler = new Handler(looper == null ? Looper.getMainLooper() : looper, this);
	}

	public void onTurnStart(Which activePlayer, int turnEndMillis) {
		mLastUpdateMillis = turnEndMillis;
		mHandler.obtainMessage(WHAT_TURN_END_COUNTER_SHOW,
				turnEndMillis, activePlayer.ordinal()).sendToTarget();
	}

	public void updateTurnCounter(int turnEndMillis) {
		if (turnEndMillis <= mLastUpdateMillis - TURN_END_COUNTER_UPDATE_INTERVAL_MILLIS) {
			mLastUpdateMillis = turnEndMillis;
			mHandler.obtainMessage(WHAT_TURN_END_COUNTER_UPDATE,
					turnEndMillis, 0).sendToTarget();
		}
	}

	public void onTurnEnd(int currentTurnNo) {
		mHandler.obtainMessage(WHAT_TURN_END_COUTNER_HIDE, currentTurnNo, 0).sendToTarget();
	}

	public void updateScore(int firstPlayerScore, int secondPlayerScore) {
		mHandler.obtainMessage(WHAT_SCORE_UPDATE,
				firstPlayerScore, secondPlayerScore).sendToTarget();
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {

			case WHAT_TURN_END_COUNTER_SHOW:
				mHudView.markActivePlayer(Which.forOrdinal(msg.arg2));
				mHudView.showTurnEndCounter();
				mHudView.updateTurnEndCounter(msg.arg1 > 0 ? msg.arg1 : 0);
				break;

			case WHAT_TURN_END_COUNTER_UPDATE:
				mHudView.updateTurnEndCounter(msg.arg1 > 0 ? msg.arg1 : 0);
				break;

			case WHAT_TURN_END_COUTNER_HIDE:
				mHandler.removeMessages(WHAT_TURN_END_COUNTER_UPDATE);
				mHudView.hideTurnEndCounter(msg.arg1);
				break;

			case WHAT_SCORE_UPDATE:
				mHudView.updateScore(msg.arg1, msg.arg2);
				break;
		}
		return true;
	}
}