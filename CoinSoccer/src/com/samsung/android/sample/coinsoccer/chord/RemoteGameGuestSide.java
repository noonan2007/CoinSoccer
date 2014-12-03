package com.samsung.android.sample.coinsoccer.chord;

import com.samsung.android.sample.coinsoccer.settings.GameSettings;
import com.samsung.android.sample.coinsoccer.settings.PlayerSettings;
import com.samsung.chord.IChordChannelListener;

public class RemoteGameGuestSide extends BaseRemoteGameConnection {

	public interface GuestSetupListener {

		void onReceiveHostSettings(String nodeName, GameSettings gameSettings,
				PlayerSettings playerSettings);

		void onHostLeft(String nodeName);

		void onJoinRequestRejected(String nodeName, byte rejectionCode);

		void onJoinRequestAccepted(String nodeName);
	}

	final GuestSetupListener mGuestSetupListener;
	private final IChordChannelListener mSetupChannelListener = new SchordChannelEmptyStatusListener() {
		
		@Override
		public void onDataReceived(String fromNode, String fromChannel, String payloadType,
				byte[][] payload) {
			if (!isGameReady()) {
				if (PAYLOAD_TYPE_SEND_GAME_SETTINGS.equals(payloadType)) {
					onReceiveHostSettings(fromNode, payload);
				}
				else if (PAYLOAD_TYPE_ACCEPT_JOIN_GAME.equals(payloadType)) {
					onJoinRequestAccepted(fromNode, payload);
				}
				else if (PAYLOAD_TYPE_REJECT_JOIN_GAME.equals(payloadType)) {
					mGuestSetupListener.onJoinRequestRejected(fromNode, payload[0][0]);
				}
			}
		}

		@Override
		public void onNodeJoined(String fromNode, String fromChannel) {
			if (!isGameReady()) {
				mChordManager.getJoinedChannel(SETUP_CHANNEL_NAME).sendData(
						fromNode, PAYLOAD_TYPE_REQUEST_GAME_SETTINGS, EMPTY_BYTES_MESSAGE);
			}
		}

		@Override
		public void onNodeLeft(String fromNode, String fromChannel) {
			if (!isGameReady()) {
				mGuestSetupListener.onHostLeft(fromNode);
			}
		}
	};

	public RemoteGameGuestSide(GuestSetupListener guestSetupListener) {
		mGuestSetupListener = guestSetupListener;
	}

	@Override
	public boolean isHost() {
		return false;
	}

	@Override
	protected void onConnected() {
		mChordManager.joinChannel(SETUP_CHANNEL_NAME, mSetupChannelListener);
		super.onConnected();
		mChordManager.getJoinedChannel(SETUP_CHANNEL_NAME).sendDataToAll(
				PAYLOAD_TYPE_REQUEST_GAME_SETTINGS, EMPTY_BYTES_MESSAGE);
	}

	public void sendPlayerSettings(String nodeName, PlayerSettings playerSettings) {
		mChordManager.getJoinedChannel(SETUP_CHANNEL_NAME).sendData(
				nodeName, PAYLOAD_TYPE_REQUEST_JOIN_GAME,
				new byte[][] { RemoteSettingsUtil.toBytes(playerSettings) });
	}

	void onJoinRequestAccepted(String fromNode, byte[][] payload) {
		mRemotePlayerNodeName = fromNode;
		mGameChannelName = new String(payload[0]);
		mGameChannel = mChordManager.joinChannel(mGameChannelName, mGameChannelListener);
		mGuestSetupListener.onJoinRequestAccepted(fromNode);
		mChordManager.leaveChannel(SETUP_CHANNEL_NAME);
	}

	void onReceiveHostSettings(String fromNode, byte[][] payload) {
		mGuestSetupListener.onReceiveHostSettings(fromNode,
				RemoteSettingsUtil.toGameSettings(payload[0]),
				RemoteSettingsUtil.toPlayerSettings(payload[1]));
	}
}
