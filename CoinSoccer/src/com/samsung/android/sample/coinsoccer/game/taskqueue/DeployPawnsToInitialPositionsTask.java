package com.samsung.android.sample.coinsoccer.game.taskqueue;

import com.badlogic.gdx.math.Interpolation;
import com.samsung.android.sample.coinsoccer.game.BaseCoinPawn;
import com.samsung.android.sample.coinsoccer.game.IHasElements;
import com.samsung.android.sample.coinsoccer.game.PawnStateSnapshot;
import com.samsung.android.sample.coinsoccer.game.taskqueue.TaskQueue.Task;

public class DeployPawnsToInitialPositionsTask extends TaskGroup {

	public DeployPawnsToInitialPositionsTask(IHasElements<BaseCoinPawn> hasPawns) {
		this(hasPawns, 950, 75, Interpolation.swingOut);
	}

	public DeployPawnsToInitialPositionsTask(IHasElements<BaseCoinPawn> hasPawns,
			int innerTaskMillis, int delayBetweenInnerTasksStart,
			Interpolation interpolation) {
		for (int i = 0; i < hasPawns.getCount(); i++) {
			addTask(createTask(hasPawns.get(i).getSnapshot().getInitialSnapshot(),
					innerTaskMillis, interpolation), i * delayBetweenInnerTasksStart);
		}
	}

	protected Task createTask(final PawnStateSnapshot snapshot,
			int innerTaskMillis, Interpolation interpolation) {
		return new SnapshotAnimatorTask(innerTaskMillis, interpolation) {

			@Override
			protected PawnStateSnapshot getSnapshot() {
				return snapshot;
			}
		};
	}
}