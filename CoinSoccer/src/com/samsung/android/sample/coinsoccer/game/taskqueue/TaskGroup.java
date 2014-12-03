package com.samsung.android.sample.coinsoccer.game.taskqueue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.samsung.android.sample.coinsoccer.game.taskqueue.TaskQueue.Task;

public class TaskGroup implements TaskQueue.Task {

	protected final List<TaskQueue.Task> mSubtasks;
	protected final List<TaskQueue.Task> mRunningSubtasks;
	
	public TaskGroup() {
		mSubtasks = new ArrayList<TaskQueue.Task>();
		mRunningSubtasks = new ArrayList<TaskQueue.Task>();
	}

	public void addTask(TaskQueue.Task task) {
		mSubtasks.add(task);
	}

	public void addTask(TaskQueue.Task task, int startDelayMillis) {
		addTask(startDelayMillis > 0 ? new DelayedTaskWrapperTask(task, startDelayMillis) : task);
	}

	@Override
	public boolean fire(long timestamp) {
		if (mRunningSubtasks.size() == 0) {
			onStart(timestamp);
		}
		Iterator<Task> it = mRunningSubtasks.iterator();
		while (it.hasNext()) {
			if (it.next().fire(timestamp)) {
				it.remove();
			}
		}
		if (mRunningSubtasks.size() == 0) {
			onFinished();
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public void onCanceled() {
		for (TaskQueue.Task task : mRunningSubtasks) {
			task.onCanceled();
		}
		mRunningSubtasks.clear();
	}

	protected void onStart(long timestamp) {
		for (Task mSubtask : mSubtasks) {
			mRunningSubtasks.add(mSubtask);
		}
	}
	
	protected void onFinished() {}
}
