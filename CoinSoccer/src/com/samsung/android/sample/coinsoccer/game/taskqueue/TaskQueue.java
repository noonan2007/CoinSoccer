package com.samsung.android.sample.coinsoccer.game.taskqueue;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class TaskQueue {
	
	/**
	 * Single task to be executed in queue
	 */
	public interface Task {

		/**
		 * 
		 * @param timestamp
		 *            - current timestamp when the method is executed. It can be used for deciding what to do or if to
		 *            remove the task from the queue.
		 * @return if true then the task is removed from queue and will not be executed in future passes. If false then
		 *         the task remains in queue.
		 */
		boolean fire(long timestamp);

		/**
		 * 
		 */
		void onCanceled();
	}

	private final List<Task> mTasks;
	private final List<Task> mTmp;

	public TaskQueue() {
		mTasks = new ArrayList<Task>();
		mTmp = new ArrayList<Task>();
	}

	/**
	 * Schedule task for execution.
	 * 
	 * @param task
	 */
	public void schedule(Task task) {
		// prevent task to be added twice
		unschedule(task);
		mTasks.add(task);
	}

	/**
	 * remove
	 * 
	 * @param task
	 * @return
	 */
	public boolean unschedule(Task task) {
		if (mTasks.remove(task)) {
			task.onCanceled();
			return true;
		}
		return false;
	}

	/**
	 * It triggers...
	 */
	public void pulse() {
		if (mTasks.size() > 0) {
			long timestamp = System.currentTimeMillis();
			mTmp.addAll(mTasks);
			for (Task task : mTmp) {
				if (task.fire(timestamp)) {
					mTasks.remove(task);
				}
			}
			mTmp.clear();
		}
	}
}
