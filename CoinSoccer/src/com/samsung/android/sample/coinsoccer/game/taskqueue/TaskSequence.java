package com.samsung.android.sample.coinsoccer.game.taskqueue;

import com.samsung.android.sample.coinsoccer.game.taskqueue.TaskQueue.Task;

/**
 * 
 *
 */
public class TaskSequence implements TaskQueue.Task {

	private final TaskQueue.Task[] mSubtasks;
	private int mCurrentTaskIndex;

	public TaskSequence(TaskQueue.Task... subtasks) {
		mSubtasks = subtasks;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean fire(long timestamp) {
		Task currentTask = mSubtasks[mCurrentTaskIndex];
		if (currentTask.fire(timestamp)) {
			mCurrentTaskIndex++;
			if (mCurrentTaskIndex == mSubtasks.length) {
				onFinished();
				return true;
			}
		}
		return false;
	}

	@Override
	public void onCanceled() {
		if (mCurrentTaskIndex > 0) {
			for (int i = mCurrentTaskIndex; i < mSubtasks.length; i++) {
				mSubtasks[i].onCanceled();
			}
		}
		cleanUp();
	}

	protected void onFinished() {
		cleanUp();
	}

	protected void cleanUp() {
		mCurrentTaskIndex = 0;
	}
}
