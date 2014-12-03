package com.samsung.android.sample.coinsoccer.chord;

public interface IRemoteGame {

	void sendGameData(byte[][] data);

	void setOnGameDataReceivedListener(OnGameDataReceivedListener listener);

	boolean isHost();
}
