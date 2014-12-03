/*
 ********************************************************************************
 * Copyright (c) 2013 Samsung Electronics, Inc.
 * All rights reserved.
 *
 * This software is a confidential and proprietary information of Samsung
 * Electronics, Inc. ("Confidential Information"). You shall not disclose such
 * Confidential Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with Samsung Electronics.
 ********************************************************************************
 */
package com.samsung.android.sample.coinsoccer.settings;

/**
 * Frozen settings for the game.
 */
public class GameSettings {

	/**
	 * If set to number bigger than 0 than game is played until
	 * one of users achieves score which equals to score limit.
	 */
	public final int scoreLimit;

	/**
	 * If set to number bigger than 0 than game is played until
	 * the game "consumed" number of turns equal to turns limit.
	 */
	public final int turnLimit;

	/**
	 * How much time (in milliseconds) can one turn take. This
	 * refers to the time when user is preparing his shot not
	 * the time when physical simulation is done. If user would
	 * not prepare within the given time then he loses his turn.
	 */
	public final int timePerTurnMillis;

	/**
	 * @param scoreLimit
	 *            see {@link #scoreLimit} description
	 * @param turnLimit
	 *            see {@link #turnLimit} description
	 * @param timePerTurnMillis
	 *            see {@link #timePerTurnMillis} description
	 * @param timePerTurnMillis
	 *            see {@link #timePerTurnMillis} description
	 */
	public GameSettings(int scoreLimit, int turnLimit, int timePerTurnMillis) {
		this.scoreLimit = scoreLimit;
		this.turnLimit = turnLimit;
		this.timePerTurnMillis = timePerTurnMillis;
	}

	public static GameSettings createFromUserInput(GameSettingsView ui) {
		switch (ui.getLimitType()) {
			case SCORE_LIMIT:
				return new GameSettings(ui.getScoreLimit(), 0, ui.getTimePerTurnMillis());
			case TURN_LIMIT:
				return new GameSettings(0, ui.getTurnLimit(), ui.getTimePerTurnMillis());
		}
		throw new InstantiationError();
	}

	public boolean hasScoreLimit() {
		return scoreLimit > 0;
	}

	public boolean hasTurnLimit() {
		return turnLimit > 0;
	}
}
