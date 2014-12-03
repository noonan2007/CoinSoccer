package com.samsung.android.sample.coinsoccer.game;

import com.samsung.android.sample.coinsoccer.settings.GameSettings;
import com.samsung.android.sample.coinsoccer.settings.Which;

public class GameSettingsHelper {

	public interface EndGameChecker {

		boolean isGameEnd(CoinSoccerGame game);
	}

	private class EndGameCheckerTurnLimitImpl implements EndGameChecker {

		@Override
		public boolean isGameEnd(CoinSoccerGame game) {
			return game.getStatistics().getCurrentTurnInGame() >= mGameSettings.turnLimit;
		}
	}

	private class EndGameCheckerScoreLimitImpl implements EndGameChecker {

		@Override
		public boolean isGameEnd(CoinSoccerGame game) {
			if (game.getPlayers().getPlayer(Which.FIRST).getStatistics().getScore() + 
					game.getPlayers().getPlayer(Which.SECOND).getStatistics().getScore() >=
					mGameSettings.scoreLimit) {
				return true;
			}
			return false;
		}
	}

	private class EndGameCheckerEmptyImpl implements EndGameChecker {

		@Override
		public boolean isGameEnd(CoinSoccerGame game) {
			return false;
		}
	}

	public final EndGameChecker endGameChecker;
	private final GameSettings mGameSettings;
	private int mTurnTimeMillis;

	public GameSettingsHelper(GameSettings gameSettings) {
		if (gameSettings.timePerTurnMillis <= 0) {
			throw new IllegalArgumentException("Time per turn must be greater than 0");
		}
		mGameSettings = gameSettings;
		endGameChecker = createEndGameChecker();
	}

	public void resetExpirationTimer() {
		mTurnTimeMillis = 0;
	}

	public boolean isTurnExpired() {
		return mTurnTimeMillis >= mGameSettings.timePerTurnMillis;
	}

	public void pulse(int deltaTime) {
		mTurnTimeMillis += deltaTime;
	}

	public int getTurnExpirationMillis() {
		return mGameSettings.timePerTurnMillis - mTurnTimeMillis;
	}

	public GameSettings getSettings() {
		return mGameSettings;
	}

	private EndGameChecker createEndGameChecker() {
		if (mGameSettings.scoreLimit > 0) {
			return new EndGameCheckerScoreLimitImpl();
		}
		if (mGameSettings.turnLimit > 0) {
			return new EndGameCheckerTurnLimitImpl();
		}
		return new EndGameCheckerEmptyImpl();
	}
}