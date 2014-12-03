package com.samsung.android.sample.coinsoccer.game.taskqueue;

import com.badlogic.gdx.math.Interpolation;

/**
 *
 */
public abstract class AnimatorTask implements TaskQueue.Task {

	private final int mTimeMillis;
	private final Interpolation mInterpolation;
	private long mAnimationStartTimestamp;

	public AnimatorTask(int timeMillis, Interpolation interpolation) {
		mTimeMillis = timeMillis;
		mInterpolation = interpolation;
	}

	@Override
	public boolean fire(long timestamp) {
		if (mAnimationStartTimestamp == 0) {
			onStart(timestamp);
			return false;
		}
		float progress = (float) (timestamp - mAnimationStartTimestamp) / mTimeMillis;
		if (progress < 1) {
			onAnimationProgress(mInterpolation.apply(progress));
			return false;
		}
		else {
			onFinished();
			return true;
		}
	}

	@Override
	public void onCanceled() {
		cleanUp();
	}

	protected void onStart(long timestamp) {
		mAnimationStartTimestamp = timestamp;
		onAnimationProgress(0);
	}

	protected void onFinished() {
		onAnimationProgress(1);
		cleanUp();
	}

	protected void cleanUp() {
		mAnimationStartTimestamp = 0;
	}

	protected abstract void onAnimationProgress(float progress);
}