package com.samsung.android.sample.coinsoccer.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class GamePreferences {

	public enum LimitType {

		TURN_LIMIT, SCORE_LIMIT
	}

	public static final int[] GOAL_LIMIT_VALUES = new int[] {
			1, 2, 3, 5, 7, 10, 15, 20, 25, 30, 35, 40, 50
	};
	public static final int[] TURN_LIMIT_VALUES = new int[] {
			10, 20, 30, 40, 50, 75, 100, 150, 200, 300, 500, 1000
	};
	public static final int[] TIME_PER_TURN_MILLIS = new int[] {
			5 * 1000, 10 * 1000, 15 * 1000, 20 * 1000, 30 * 1000, 45 * 1000, 60 * 1000
	};

	private static final String PREF_NAME = GamePreferences.class.getName();

	private static final String KEY_TURN_LIMIT = "turnLimit";
	private static final String KEY_SCORE_LIMIT = "scoreLimit";
	private static final String KEY_TIME_PER_TURN = "timePerTurn";
	private static final String KEY_LIMIT_TYPE = "limitType";
	private static final String KEY_FIRST_PLAYER_COLOR = "firstPlayerColor";
	private static final String KEY_SECOND_PLAYER_COLOR = "secondPlayerColor";
	private static final String KEY_FIRST_PLAYER_NAME = "firstPlayerName";
	private static final String KEY_SECOND_PLAYER_NAME = "secondPlayerName";
	private static final int DEFAULT_TURN_LIMIT = TURN_LIMIT_VALUES[TURN_LIMIT_VALUES.length / 2];
	private static final int DEFAULT_GOAL_LIMIT = GOAL_LIMIT_VALUES[GOAL_LIMIT_VALUES.length / 2];
	private static final int DEFAULT_TIME_PER_TURN = TIME_PER_TURN_MILLIS[TIME_PER_TURN_MILLIS.length / 2];
	private static final int DEFAULT_LIMIT_TYPE = LimitType.TURN_LIMIT.ordinal();
	private static final int DEFAULT_FIRST_PLAYER_COLOR = 0xFF00CCCC;
	private static final int DEFAULT_SECOND_PLAYER_COLOR = 0xFFFF2500;
	private static final String DEFAULT_FIRST_PLAYER_NAME = "First player";
	private static final String DEFAULT_SECOND_PLAYER_NAME = "Second player";

	private final SharedPreferences mPrefs;

	public GamePreferences(Context context) {
		mPrefs = context.getSharedPreferences(
				PREF_NAME, Context.MODE_PRIVATE);
	}

	public int getTurnLimit() {
		return mPrefs.getInt(KEY_TURN_LIMIT, DEFAULT_TURN_LIMIT);
	}

	public int getScoreLimit() {
		return mPrefs.getInt(KEY_SCORE_LIMIT, DEFAULT_GOAL_LIMIT);
	}

	public int getTimePerTurnMillis() {
		return mPrefs.getInt(KEY_TIME_PER_TURN, DEFAULT_TIME_PER_TURN);
	}

	public LimitType getLimitType() {
		return LimitType.values()[mPrefs.getInt(KEY_LIMIT_TYPE, DEFAULT_LIMIT_TYPE)];
	}

	public int getFirstPlayerColor() {
		return mPrefs.getInt(KEY_FIRST_PLAYER_COLOR, DEFAULT_FIRST_PLAYER_COLOR);
	}

	public int getSecondPlayerColor() {
		return mPrefs.getInt(KEY_SECOND_PLAYER_COLOR, DEFAULT_SECOND_PLAYER_COLOR);
	}

	public String getFirstPlayerName() {
		return mPrefs.getString(KEY_FIRST_PLAYER_NAME, DEFAULT_FIRST_PLAYER_NAME);
	}

	public String getSecondPlayerName() {
		return mPrefs.getString(KEY_SECOND_PLAYER_NAME, DEFAULT_SECOND_PLAYER_NAME);
	}

	public void save(GameSettingsView gameSettings,
			PlayerSettingsView firstPlayerSettings,
			PlayerSettingsView secondPlayerSettings) {
		mPrefs.edit()
				.putInt(KEY_TURN_LIMIT, gameSettings.getTurnLimit())
				.putInt(KEY_SCORE_LIMIT, gameSettings.getScoreLimit())
				.putInt(KEY_TIME_PER_TURN, gameSettings.getTimePerTurnMillis())
				.putInt(KEY_LIMIT_TYPE, gameSettings.getLimitType().ordinal())
				.putString(KEY_FIRST_PLAYER_NAME, firstPlayerSettings.getName())
				.putInt(KEY_FIRST_PLAYER_COLOR, firstPlayerSettings.getColor())
				.putString(KEY_SECOND_PLAYER_NAME, secondPlayerSettings.getName())
				.putInt(KEY_SECOND_PLAYER_COLOR, secondPlayerSettings.getColor())
				.apply();
	}

	public void saveSecondPlayerOnly(PlayerSettingsView secondPlayerSettings) {
		mPrefs.edit()
				.putString(KEY_SECOND_PLAYER_NAME, secondPlayerSettings.getName())
				.putInt(KEY_SECOND_PLAYER_COLOR, secondPlayerSettings.getColor())
				.apply();
	}
}
