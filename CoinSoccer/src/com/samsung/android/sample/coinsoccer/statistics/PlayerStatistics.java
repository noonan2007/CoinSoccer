package com.samsung.android.sample.coinsoccer.statistics;

import android.os.Bundle;

/**
 * Represents player statistics.
 */
public class PlayerStatistics {

	public static PlayerStatistics fromBundle(Bundle bundle) {
		PlayerStatistics object = new PlayerStatistics();
		object.mScore = bundle.getInt(KEY_SCORE);
		object.mOwnGoals = bundle.getInt(KEY_OWN_GOALS);
		object.mOffensiveFoulsCount = bundle.getInt(KEY_OFFENSIVE_FOULS_COUNT);
		object.mIllegalPawnsPositionFoulsCount = bundle.getInt(KEY_ILLEGAL_PAWNS_POSITION_FOULS_COUNT);
		object.mExpiredShotsCount = bundle.getInt(KEY_EXPIRED_SHOTS_COUNT);
		object.mMinTurnsPerGoal = bundle.getInt(KEY_MIN_TURNS_PER_GOAL);
		object.mMaxTurnsPerGoal = bundle.getInt(KEY_MAX_TURNS_PER_GOAL);
		object.mAggregatedUserTurnsInPastRounds = bundle.getInt(KEY_AGGREGATED_USER_TURNS_IN_PAST_ROUNDS);
		return object;
	}

	private static final String KEY_SCORE = "score";
	private static final String KEY_OWN_GOALS = "ownGoals";
	private static final String KEY_OFFENSIVE_FOULS_COUNT = "offensiveFoulsCount";
	private static final String KEY_ILLEGAL_PAWNS_POSITION_FOULS_COUNT = "illegalPawnsPositionFoulsCount";
	private static final String KEY_EXPIRED_SHOTS_COUNT = "expiredShotsCount";
	private static final String KEY_MIN_TURNS_PER_GOAL = "minTurnsPerGoal";
	private static final String KEY_MAX_TURNS_PER_GOAL = "maxTurnsPerGoal";
	private static final String KEY_AGGREGATED_USER_TURNS_IN_PAST_ROUNDS = "aggregatedUserTurnsInPastRounds";

	int mScore;
	int mOwnGoals;
	int mOffensiveFoulsCount;
	int mIllegalPawnsPositionFoulsCount;
	int mExpiredShotsCount;
	int mMinTurnsPerGoal;
	int mMaxTurnsPerGoal;
	int mAggregatedUserTurnsInPastRounds;

	public float getAvgTurnsPerGoal() {
		return mScore == 0 ? 0 : (float) mAggregatedUserTurnsInPastRounds / mScore;
	}

	public int getScore() {
		return mScore;
	}

	public int getOwnGoals() {
		return mOwnGoals;
	}

	public int getOffensiveFoulsCount() {
		return mOffensiveFoulsCount;
	}

	public int getIllegalPawnsPositionFoulsCount() {
		return mIllegalPawnsPositionFoulsCount;
	}

	public int getTotalFoulsCount() {
		return getOffensiveFoulsCount() + getIllegalPawnsPositionFoulsCount();
	}

	public int getExpiredShotsCount() {
		return mExpiredShotsCount;
	}

	public int getMinTurnsPerGoal() {
		return mMinTurnsPerGoal;
	}

	public int getMaxTurnsPerGoal() {
		return mMaxTurnsPerGoal;
	}

	public Bundle toBundle() {
		return writeToBundle(new Bundle());
	}

	public Bundle writeToBundle(Bundle bundle) {
		bundle.putInt(KEY_SCORE, mScore);
		bundle.putInt(KEY_OWN_GOALS, mOwnGoals);
		bundle.putInt(KEY_OFFENSIVE_FOULS_COUNT, mOffensiveFoulsCount);
		bundle.putInt(KEY_ILLEGAL_PAWNS_POSITION_FOULS_COUNT, mIllegalPawnsPositionFoulsCount);
		bundle.putInt(KEY_EXPIRED_SHOTS_COUNT, mExpiredShotsCount);
		bundle.putInt(KEY_MIN_TURNS_PER_GOAL, mMinTurnsPerGoal);
		bundle.putInt(KEY_MAX_TURNS_PER_GOAL, mMaxTurnsPerGoal);
		bundle.putInt(KEY_AGGREGATED_USER_TURNS_IN_PAST_ROUNDS, mAggregatedUserTurnsInPastRounds);
		return bundle;
	}
}
