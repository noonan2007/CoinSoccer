package com.samsung.android.sample.coinsoccer.game.taskqueue;

/**
 *
 */
public class DelayedSingleShotTask implements TaskQueue.Task {

	private long mExpiresTimestamp;
	private final int mDelayMillis;

	public DelayedSingleShotTask(int delayMillis) {
		mDelayMillis = delayMillis;
	}

	@Override
	public boolean fire(long timestamp) {
		if (mExpiresTimestamp == 0) {
			onStart(timestamp);
		}
		if (mExpiresTimestamp <= timestamp) {
			onFinished();
			return true;
		}
		return false;
	}

	@Override
	public void onCanceled() {
		cleanUp();
	}

	protected void onFinished() {
		cleanUp();
	}

	protected void cleanUp() {
		mExpiresTimestamp = 0;
	}

	protected void onStart(long timestamp) {
		mExpiresTimestamp = timestamp + mDelayMillis;
	}
}