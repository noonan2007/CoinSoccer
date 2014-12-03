package com.samsung.android.sample.coinsoccer.statistics;

import android.os.Bundle;

import com.samsung.android.sample.coinsoccer.settings.Which;

public class GameStatistics {

	private static final String KEY_TURNS_COUNT_IN_CURRENT_ROUND = "turnsCountInCurrentRound";
	private static final String KEY_GAME_END = "gameEnd";
	private static final String KEY_FIRST_PLAYER_STATISTICS = "firstPlayerStatistics";
	private static final String KEY_SECOND_PLAYER_STATISTICS = "secondPlayerStatistics";

	public static GameStatistics fromBundle(Bundle bundle) {
		GameStatistics gameStatistics = new GameStatistics(
				PlayerStatistics.fromBundle(bundle.getBundle(KEY_FIRST_PLAYER_STATISTICS)),
				PlayerStatistics.fromBundle(bundle.getBundle(KEY_SECOND_PLAYER_STATISTICS)));
		gameStatistics.mTurnsCountInCurrentRound = bundle.getInt(KEY_TURNS_COUNT_IN_CURRENT_ROUND);
		gameStatistics.mGameEnd = bundle.getBoolean(KEY_GAME_END);
		return gameStatistics;
	}

	final PlayerStatistics mFirstPlayerStatistics;
	final PlayerStatistics mSecondPlayerStatistics;
	int mTurnsCountInCurrentRound;
	boolean mGameEnd;

	GameStatistics(PlayerStatistics firstPlayerStatistics,
			PlayerStatistics secondPlayerStatistics) {
		mFirstPlayerStatistics = firstPlayerStatistics;
		mSecondPlayerStatistics = secondPlayerStatistics;
	}

	public int getCurrentTurnInRound() {
		return mTurnsCountInCurrentRound;
	}

	public int getCurrentTurnInGame() {
		return mFirstPlayerStatistics.mAggregatedUserTurnsInPastRounds +
				mSecondPlayerStatistics.mAggregatedUserTurnsInPastRounds +
				getCurrentTurnInRound();
	}

	public boolean isGameEnd() {
		return mGameEnd;
	}

	public PlayerStatistics getPlayerStatistics(Which whichPlayer) {
		switch (whichPlayer) {
			case FIRST:
				return mFirstPlayerStatistics;
			case SECOND:
				return mSecondPlayerStatistics;
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	public Bundle toBundle() {
		return writeToBundle(new Bundle());
	}

	public Bundle writeToBundle(Bundle bundle) {
		bundle.putBundle(KEY_FIRST_PLAYER_STATISTICS, mFirstPlayerStatistics.toBundle());
		bundle.putBundle(KEY_SECOND_PLAYER_STATISTICS, mSecondPlayerStatistics.toBundle());
		bundle.putBoolean(KEY_GAME_END, mGameEnd);
		bundle.putInt(KEY_TURNS_COUNT_IN_CURRENT_ROUND, mTurnsCountInCurrentRound);
		return bundle;
	}
}