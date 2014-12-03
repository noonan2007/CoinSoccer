package com.samsung.android.sample.coinsoccer.game.taskqueue;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Pool;
import com.samsung.android.sample.coinsoccer.game.IHasElements;
import com.samsung.android.sample.coinsoccer.game.PawnStateSnapshot;

public class ApplyFuturePositionsTask extends TaskGroup {

	private class InnerTask extends SnapshotAnimatorTask {

		private PawnStateSnapshot mSnapshot;

		public InnerTask() {
			super(mTimeMillis, mInterpolation);
		}

		@Override
		protected PawnStateSnapshot getSnapshot() {
			return mSnapshot;
		}

		InnerTask setSnapshot(PawnStateSnapshot snapshot) {
			mSnapshot = snapshot;
			return this;
		}
	}

	private final Pool<InnerTask> mInnerTasksPool = new Pool<InnerTask>() {

		@Override
		protected InnerTask newObject() {
			return new InnerTask();
		}
	};
	final int mTimeMillis;
	final Interpolation mInterpolation;

	public ApplyFuturePositionsTask() {
		this(1150, Interpolation.swingOut);
	}

	public ApplyFuturePositionsTask(int timeMillis, Interpolation interpolation) {
		mTimeMillis = timeMillis;
		mInterpolation = interpolation;
	}

	public void setTargetSnapshots(IHasElements<PawnStateSnapshot> targetSnapshots) {
		for (TaskQueue.Task subtask : mSubtasks) {
			mInnerTasksPool.free((InnerTask) subtask);
		}
		mSubtasks.clear();
		for (int i = 0; i < targetSnapshots.getCount(); i++) {
			addTask(mInnerTasksPool.obtain().setSnapshot(targetSnapshots.get(i)));
		}
	}
}
