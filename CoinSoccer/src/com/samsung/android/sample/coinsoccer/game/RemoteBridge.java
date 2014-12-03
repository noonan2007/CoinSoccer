package com.samsung.android.sample.coinsoccer.game;

import com.badlogic.gdx.Gdx;
import com.samsung.android.sample.coinsoccer.chord.IRemoteGame;
import com.samsung.android.sample.coinsoccer.chord.OnGameDataReceivedListener;
import com.samsung.android.sample.coinsoccer.settings.Which;

public abstract class RemoteBridge implements OnGameDataReceivedListener {

	private static final byte TYPE_STARTING_PLAYER_CHOSEN = 1;
	private static final byte TYPE_SHOT_EXPIRED = 2;
	private static final byte TYPE_SHOT_READY = 3;
	private static final byte TYPE_PAUSE_FLAG_CHANGE = 5;
	private static final byte TYPE_RAW_DATA = 6;

	private static final byte[][] sShotExpiredMessage = createMessageBytes(
			TYPE_SHOT_EXPIRED, new byte[] {});
	private static final byte[][] sShotReadyMessage = createMessageBytes(
			TYPE_SHOT_READY, new byte[] {});

	private static byte[][] createMessageBytes(byte type, byte[] payload) {
		return new byte[][] { new byte[] { type }, payload };
	}

	private static byte getType(byte[][] bytes) {
		return bytes[0][0];
	}

	private static byte[] getPayload(byte[][] bytes) {
		return bytes.length > 1 ? bytes[1] : null;
	}

	private final IRemoteGame mConnection;

	public RemoteBridge(IRemoteGame connection) {
		mConnection = connection;
	}

	public void connect() {
		mConnection.setOnGameDataReceivedListener(this);
	}

	@Override
	public final void onGameDataReceived(final byte[][] data) {
		Gdx.app.postRunnable(new Runnable() {

			@Override
			public void run() {
				switch (getType(data)) {
					case TYPE_STARTING_PLAYER_CHOSEN:
						handleStartingPlayerChosen(getPayload(data));
						break;
					case TYPE_SHOT_EXPIRED:
						onShotExpired();
						break;
					case TYPE_SHOT_READY:
						onShotReady();
						break;
					case TYPE_PAUSE_FLAG_CHANGE:
						handlePauseFlagChanged(getPayload(data));
						break;
					case TYPE_RAW_DATA:
						onRawData(getPayload(data));
						break;
				}
			}
		});
	}

	public void sendStartingPlayer(Which startingPlayer) {
		mConnection.sendGameData(createMessageBytes(
				TYPE_STARTING_PLAYER_CHOSEN,
				new byte[] { (byte) startingPlayer.ordinal() }));
	}

	public void sendShotReady() {
		mConnection.sendGameData(sShotReadyMessage);
	}

	public void sendShotExpired() {
		mConnection.sendGameData(sShotExpiredMessage);
	}

	public void sendPauseFlagChanged(boolean pauseFlag) {
		mConnection.sendGameData(createMessageBytes(
				TYPE_PAUSE_FLAG_CHANGE, new byte[] { (byte) (pauseFlag ? 1 : 0) }));
	}

	public void sendRawData(byte[] rawData) {
		mConnection.sendGameData(createMessageBytes(TYPE_RAW_DATA, rawData));
	}

	public IRemoteGame getConnection() {
		return mConnection;
	}

	void handleStartingPlayerChosen(byte[] payload) {
		if (payload == null) {
			throw new IllegalStateException("Empty payload not allowed!");
		}
		onStartingPlayerChosen(Which.forOrdinal(payload[0]));
	}

	void handlePauseFlagChanged(byte[] payload) {
		if (payload == null) {
			throw new IllegalStateException("Empty payload not allowed!");
		}
		onPauseFlagChanged(payload[0] == 1);
	}

	protected abstract void onRawData(byte[] data);

	protected abstract void onStartingPlayerChosen(Which startingPlayer);

	protected abstract void onShotExpired();

	protected abstract void onShotReady();

	protected abstract void onPauseFlagChanged(boolean pauseFlag);
}
