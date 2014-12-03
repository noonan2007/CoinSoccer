package com.samsung.android.sample.coinsoccer.game.taskqueue;

public class DelayedTaskWrapperTask implements TaskQueue.Task {

	private final TaskQueue.Task mWrappedTask;
	private final int mStartDelayMillis;
	private long mDelayStartTimestamp;

	public DelayedTaskWrapperTask(TaskQueue.Task wrappedTask, int startDelayMillis) {
		mWrappedTask = wrappedTask;
		mStartDelayMillis = startDelayMillis;
	}

	@Override
	public boolean fire(long timestamp) {
		if (mDelayStartTimestamp == 0) {
			onStart(timestamp);
		}
		if (mDelayStartTimestamp + mStartDelayMillis > timestamp) {
			return false;
		}
		boolean status = mWrappedTask.fire(timestamp);
		if (status) {
			onFinished();
		}
		return status;
	}

	public TaskQueue.Task getWrappedTask() {
		return mWrappedTask;
	}

	@Override
	public void onCanceled() {
		mWrappedTask.onCanceled();
		cleanUp();
	}

	protected void onStart(long timestamp) {
		mDelayStartTimestamp = timestamp;
	}

	protected void onFinished() {
		cleanUp();
	}

	protected void cleanUp() {
		mDelayStartTimestamp = 0;
	}
}
