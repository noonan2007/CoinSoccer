package com.samsung.android.sample.coinsoccer.statistics;

import com.samsung.android.sample.coinsoccer.settings.Which;

public class GameStatisticsCollector extends GameStatistics {

	private static final class PlayerStatisticsCollector extends PlayerStatistics {

		void onGoalScored(int turnsCountInCurrentRound) {
			mScore++;
			if (mMinTurnsPerGoal == 0 || mMinTurnsPerGoal > turnsCountInCurrentRound) {
				mMinTurnsPerGoal = turnsCountInCurrentRound;
			}
			if (mMaxTurnsPerGoal == 0 || mMaxTurnsPerGoal < turnsCountInCurrentRound) {
				mMaxTurnsPerGoal = turnsCountInCurrentRound;
			}
			mAggregatedUserTurnsInPastRounds += turnsCountInCurrentRound;
		}
	}

	public GameStatisticsCollector() {
		super(new PlayerStatisticsCollector(), new PlayerStatisticsCollector());
	}

	public void onFoulCommitted(Which whichPlayer) {
		if (mGameEnd) {
			throw new IllegalStateException("Cannot be modified after game end!");
		}
		getPlayerStatistics(whichPlayer).mOffensiveFoulsCount++;
	}

	public void onIllegalPawnPositionsFixed(Which whichPlayer) {
		if (mGameEnd) {
			throw new IllegalStateException("Cannot be modified after game end!");
		}
		getPlayerStatistics(whichPlayer).mIllegalPawnsPositionFoulsCount++;
	}

	public void onTurnStart() {
		if (mGameEnd) {
			throw new IllegalStateException("Cannot be modified after game end!");
		}
		mTurnsCountInCurrentRound++;
	}

	public void onShotExpired(Which whichPlayer) {
		if (mGameEnd) {
			throw new IllegalStateException("Cannot be modified after game end!");
		}
		getPlayerStatistics(whichPlayer).mExpiredShotsCount++;
	}

	public void onGoalShot(Which attackingPlayer, Which defendingPlayer, Which currentPlayer) {
		if (mGameEnd) {
			throw new IllegalStateException("Cannot be modified after game end!");
		}
		((PlayerStatisticsCollector) getPlayerStatistics(
				attackingPlayer)).onGoalScored(mTurnsCountInCurrentRound);
		mTurnsCountInCurrentRound = 0;
		if (defendingPlayer == currentPlayer) {
			getPlayerStatistics(defendingPlayer).mOwnGoals++;
		}
	}

	public void setGameEnd() {
		mGameEnd = true;
	}
}