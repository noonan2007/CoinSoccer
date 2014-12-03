package com.samsung.android.sample.coinsoccer.game.taskqueue;

import com.badlogic.gdx.math.Interpolation;
import com.samsung.android.sample.coinsoccer.game.BaseCoinPawn;
import com.samsung.android.sample.coinsoccer.game.ChainableStateSnapshot;
import com.samsung.android.sample.coinsoccer.game.IHasElements;
import com.samsung.android.sample.coinsoccer.game.PawnStateSnapshot;

public class RevertPawnsToLastSavedPositionsTask extends TaskGroup {

	private static class InnerTask extends SnapshotAnimatorTask {

		private final BaseCoinPawn mPawn;
		private ChainableStateSnapshot mSnapshot;

		public InnerTask(BaseCoinPawn pawn, int timeMillis,
				Interpolation interpolation) {
			super(timeMillis, interpolation);
			mPawn = pawn;
		}

		@Override
		protected PawnStateSnapshot getSnapshot() {
			if (mSnapshot == null) {
				mSnapshot = mPawn.popLastSnapshot();
			}
			return mSnapshot;
		}

		@Override
		protected void cleanUp() {
			super.cleanUp();
			if (!mSnapshot.isInChain()) {
				mSnapshot.free();
			}
			mSnapshot = null;
		}
	}

	public RevertPawnsToLastSavedPositionsTask(IHasElements<BaseCoinPawn> hasPawns) {
		this(hasPawns, 750, 0, Interpolation.swingOut);
	}

	public RevertPawnsToLastSavedPositionsTask(IHasElements<BaseCoinPawn> hasPawns,
			int innerTaskMillis, int delayBetweenInnerTasksStart,
			Interpolation interpolation) {
		for (int i = 0; i < hasPawns.getCount(); i++) {
			addTask(new InnerTask(hasPawns.get(i), innerTaskMillis, interpolation),
					i * delayBetweenInnerTasksStart);
		}
	}
}
