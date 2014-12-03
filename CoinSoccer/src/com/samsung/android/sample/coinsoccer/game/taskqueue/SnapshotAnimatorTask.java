package com.samsung.android.sample.coinsoccer.game.taskqueue;

import com.badlogic.gdx.math.Interpolation;
import com.samsung.android.sample.coinsoccer.game.PawnStateSnapshot;
import com.samsung.android.sample.coinsoccer.game.StateSnapshot;

public abstract class SnapshotAnimatorTask extends AnimatorTask {

	protected StateSnapshot mInitialState;

	public SnapshotAnimatorTask(int timeMillis, Interpolation interpolation) {
		super(timeMillis, interpolation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onStart(long timestamp) {
		mInitialState = StateSnapshot.obtain(getSnapshot().getPawn(), true);
		super.onStart(timestamp);
	}

	@Override
	protected void cleanUp() {
		super.cleanUp();
		mInitialState.free();
		mInitialState = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onAnimationProgress(float progress) {
		updateSinglePawn(mInitialState, getSnapshot(), progress);
	}

	protected static void updateSinglePawn(StateSnapshot initialState,
			PawnStateSnapshot futureState, float progress) {
		futureState.getPawn().applyBodyState(
				calculateSingleValue(initialState.getCenterX(), futureState.getCenterX(), progress),
				calculateSingleValue(initialState.getCenterY(), futureState.getCenterY(), progress),
				calculateSingleValue(initialState.getAngle(), futureState.getAngle(), progress));
	}

	protected static float calculateSingleValue(float initialValue, float futureValue, float progress) {
		return initialValue + (futureValue - initialValue) * progress;
	}

	protected abstract PawnStateSnapshot getSnapshot();
}
