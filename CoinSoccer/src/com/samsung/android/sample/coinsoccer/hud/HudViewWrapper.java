package com.samsung.android.sample.coinsoccer.hud;

import java.util.Calendar;

import android.view.View;
import android.widget.TextView;

import com.samsung.android.sample.coinsoccer.R;
import com.samsung.android.sample.coinsoccer.settings.GameSettings;
import com.samsung.android.sample.coinsoccer.settings.PlayerSettings;
import com.samsung.android.sample.coinsoccer.settings.Which;

public class HudViewWrapper {

	private final Calendar mHelperDateObject = Calendar.getInstance();

	private final GameSettings mGameSettings;
	private final PlayerInfoView mFirstPlayerInfoDisplay;
	private final PlayerInfoView mSecondPlayerInfoDisplay;
	private final TextView mTurnEndCounter;
	private final TextView mGameEndCounter;

	public HudViewWrapper(View root, GameSettings gameSettings,
			PlayerSettings firstPlayerSettings, PlayerSettings secondPlayerSettings) {
		this(root, gameSettings);
		addPlayer(firstPlayerSettings);
		addPlayer(secondPlayerSettings);
	}

	public HudViewWrapper(View root, GameSettings gameSettings) {
		mFirstPlayerInfoDisplay = (PlayerInfoView) root.findViewById(R.id.first_player_info);
		mSecondPlayerInfoDisplay = (PlayerInfoView) root.findViewById(R.id.second_player_info);
		mTurnEndCounter = (TextView) root.findViewById(R.id.turn_end_counter);
		mGameEndCounter = (TextView) root.findViewById(R.id.game_end_counter);
		mGameSettings = gameSettings;
		if (mGameSettings.hasScoreLimit()) {
			updateGoalsTillGameEnd(mGameSettings.scoreLimit);
		}
		else if (mGameSettings.hasTurnLimit()) {
			updateTurnsTillGameEnd(mGameSettings.turnLimit);
		}
	}

	public void addPlayer(PlayerSettings playerSettings) {
		PlayerInfoView playerInfoDisplay = getPlayerInfoDisplay(playerSettings.which);
		playerInfoDisplay.setName(playerSettings.name);
		playerInfoDisplay.setColor(playerSettings.color);
		playerInfoDisplay.setScore(0);
	}

	public PlayerInfoView getPlayerInfoDisplay(Which which) {
		switch (which) {
			case FIRST:
				return mFirstPlayerInfoDisplay;
			case SECOND:
				return mSecondPlayerInfoDisplay;
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	public void showTurnEndCounter() {
		mTurnEndCounter.setVisibility(View.VISIBLE);
	}

	public void hideTurnEndCounter(int currentTurnNo) {
		mTurnEndCounter.setVisibility(View.GONE);
		if (mGameSettings.hasTurnLimit()) {
			updateTurnsTillGameEnd(mGameSettings.turnLimit - currentTurnNo);
		}
	}

	public void updateTurnEndCounter(long remainingTimeMillis) {
		mHelperDateObject.setTimeInMillis(remainingTimeMillis);
		mTurnEndCounter.setText(String.format("%01d:%02d:%01d",
				mHelperDateObject.get(Calendar.MINUTE),
				mHelperDateObject.get(Calendar.SECOND),
				mHelperDateObject.get(Calendar.MILLISECOND) / 100));
	}

	public void updateScore(int firstPlayerScore, int secondPlayerScore) {
		mFirstPlayerInfoDisplay.setScore(firstPlayerScore);
		mSecondPlayerInfoDisplay.setScore(secondPlayerScore);
		if (mGameSettings.hasScoreLimit()) {
			updateGoalsTillGameEnd(mGameSettings.scoreLimit -
					(firstPlayerScore + secondPlayerScore));
		}
	}

	public void markActivePlayer(Which activePlayer) {
		getPlayerInfoDisplay(activePlayer).markAsActive(true);
		getPlayerInfoDisplay(Which.getTheOppositeOne(activePlayer)).markAsActive(false);
	}

	private void updateGoalsTillGameEnd(int goalsTillGameEnd) {
		mGameEndCounter.setText(mGameEndCounter.getContext().getString(
				R.string.hud_goals_till_end, goalsTillGameEnd));
	}

	private void updateTurnsTillGameEnd(int turnsTillGameEnd) {
		mGameEndCounter.setText(mGameEndCounter.getContext().getString(
				R.string.hud_turns_till_end, turnsTillGameEnd));
	}
}
