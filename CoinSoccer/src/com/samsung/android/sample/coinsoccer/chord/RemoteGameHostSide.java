package com.samsung.android.sample.coinsoccer.chord;

import com.samsung.android.sample.coinsoccer.settings.GameSettings;
import com.samsung.android.sample.coinsoccer.settings.PlayerSettings;
import com.samsung.chord.IChordChannelListener;

public class RemoteGameHostSide extends BaseRemoteGameConnection {

	public interface HostSetupListener {

		void onRequestJoinGame(PlayerSettings playerSettings);

		void onRequestJoinNodeLeft();
	}

	String mRequestingNodeName;
	final HostSetupListener mHostSetupListener;
	private final byte[][] mGameSettingsBytes;
	private final IChordChannelListener mSetupChannelListener = new SchordChannelEmptyStatusListener() {

		@Override
		public void onDataReceived(String fromNode, String fromChannel, String payloadType, byte[][] payload) {
			if (PAYLOAD_TYPE_REQUEST_GAME_SETTINGS.equals(payloadType)) {
				if (!isGameReady()) {
					onRequestGameSettings(fromNode);
				}
			}
			else if (PAYLOAD_TYPE_REQUEST_JOIN_GAME.equals(payloadType)) {
				if(isGameReady()) {
					mChordManager.getJoinedChannel(SETUP_CHANNEL_NAME).sendData(
							fromNode, PAYLOAD_TYPE_REJECT_JOIN_GAME,
							ALREADY_IN_GAME_BYTES_MESSAGE);
				}
				else if (mRequestingNodeName != null) {
					mChordManager.getJoinedChannel(SETUP_CHANNEL_NAME).sendData(
							fromNode, PAYLOAD_TYPE_REJECT_JOIN_GAME,
							PROCESSING_OTHER_USER_REQUEST_BYTES_MESSAGE);
				}
				else {
					onRequestJoinGame(fromNode,
							RemoteSettingsUtil.toPlayerSettings(payload[0]));
				}
			}
		}

		@Override
		public void onNodeLeft(String fromNode, String fromChannel) {
			if (!isGameReady()) {
				if(mRequestingNodeName != null && mRequestingNodeName.equals(fromNode)) {
					mRequestingNodeName = null;
					mHostSetupListener.onRequestJoinNodeLeft();
				}
			}
		}
	};

	public RemoteGameHostSide(HostSetupListener hostSetupListener,
			GameSettings gameSettings, PlayerSettings playerSettings) {
		mHostSetupListener = hostSetupListener;
		mGameSettingsBytes = new byte[][] {
				RemoteSettingsUtil.toBytes(gameSettings),
				RemoteSettingsUtil.toBytes(playerSettings)
		};
	}
	
	@Override
	public boolean isHost() {
		return true;
	}

	@Override
	protected void onConnected() {
		mChordManager.joinChannel(SETUP_CHANNEL_NAME, mSetupChannelListener);
		super.onConnected();
	}

	public boolean rejectGuestJoinRequest() {
		if (mRequestingNodeName != null) {
			mChordManager.getJoinedChannel(SETUP_CHANNEL_NAME).sendData(
					mRequestingNodeName, PAYLOAD_TYPE_REJECT_JOIN_GAME,
					REJECTED_BY_USER_BYTES_MESSAGE);
			mRequestingNodeName = null;
			return true;
		}
		return false;
	}

	public boolean acceptGuestJoinRequest() {
		if (mRequestingNodeName != null) {
			mGameChannelName = GAME_CHANNEL_BASE_NAME + "_" + System.currentTimeMillis();
			mRemotePlayerNodeName = mRequestingNodeName;
			mRequestingNodeName = null;
			mGameChannel = mChordManager.joinChannel(mGameChannelName, mGameChannelListener);
			mChordManager.getJoinedChannel(SETUP_CHANNEL_NAME).sendData(
					mRemotePlayerNodeName, PAYLOAD_TYPE_ACCEPT_JOIN_GAME,
					new byte[][] { mGameChannelName.getBytes() });
			mChordManager.leaveChannel(SETUP_CHANNEL_NAME);
			return true;
		}
		return false;
	}

	void onRequestJoinGame(String fromNode, PlayerSettings playerSettings) {
		mRequestingNodeName = fromNode;
		mHostSetupListener.onRequestJoinGame(playerSettings);
	}

	void onRequestGameSettings(String fromNode) {
		mChordManager.getJoinedChannel(SETUP_CHANNEL_NAME).sendData(
				fromNode, PAYLOAD_TYPE_SEND_GAME_SETTINGS, mGameSettingsBytes);
	}
}
