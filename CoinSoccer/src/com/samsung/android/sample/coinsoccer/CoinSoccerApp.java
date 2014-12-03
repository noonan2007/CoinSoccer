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
package com.samsung.android.sample.coinsoccer;

import android.app.Application;
import android.content.Context;

import com.samsung.android.sample.coinsoccer.chord.BaseRemoteGameConnection;
import com.samsung.android.sample.coinsoccer.settings.GameSettings;
import com.samsung.android.sample.coinsoccer.settings.PlayerSettings;
import com.samsung.android.sample.coinsoccer.settings.Which;


/**
 * Application class - it is extended to store application state.
 */
public class CoinSoccerApp extends Application {

	/**
	 * Utility method for accessing {@link CoinSoccerApp} instance from given context
	 *
	 * @param context
	 *            - any context inside this application
	 * @return Application object/context casted on {@link CoinSoccerApp} class
	 */
	public static CoinSoccerApp get(Context context) {
		return (CoinSoccerApp) context.getApplicationContext();
	}

	private GameSettings mGameSettings;
	private PlayerSettings mFirstPlayerSettings;
	private PlayerSettings mSecondPlayerSettings;
	private BaseRemoteGameConnection mRemoteGameConnection;

	/**
	 * Returns game settings as set by {@link #setGameSettings(GameSettings)}
	 * or null if none set.
	 * Not thread safe - should be called from UI thread!!!
	 *
	 * @return current {@link GameSettings} for the game
	 */
	public GameSettings getGameSettings() {
		return mGameSettings;
	}

	public PlayerSettings getPlayerSettings(Which whichPlayer) {
		switch (whichPlayer) {
			case FIRST:
				return mFirstPlayerSettings;
			case SECOND:
				return mSecondPlayerSettings;
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	public PlayerSettings getFirstPlayerSettings() {
		return mFirstPlayerSettings;
	}

	public PlayerSettings getSecondPlayerSettings() {
		return mSecondPlayerSettings;
	}

	/**
	 * Sets {@link GameSettings} for the game.
	 * Not thread safe - should be called from UI thread!!!
	 *
	 * @param gameSettings
	 */
	public void setGameSettings(GameSettings gameSettings) {
		mGameSettings = gameSettings;
	}

	public void setPlayerSettings(PlayerSettings playerSettings) {
		switch (playerSettings.which) {
			case FIRST:
				mFirstPlayerSettings = playerSettings;
				break;
			case SECOND:
				mSecondPlayerSettings = playerSettings;
				break;
		}
	}

	public void setRemoteGameConnection(final BaseRemoteGameConnection remoteGameConnection) {
		mRemoteGameConnection = remoteGameConnection;
	}

	public BaseRemoteGameConnection getRemoteGameConnection() {
		return mRemoteGameConnection;
	}

	public boolean clearOffRemoteGameConnectionIfExists() {
		if (mRemoteGameConnection != null) {
			mRemoteGameConnection.destroy();
			mRemoteGameConnection = null;
			return true;
		}
		return false;
	}
}
