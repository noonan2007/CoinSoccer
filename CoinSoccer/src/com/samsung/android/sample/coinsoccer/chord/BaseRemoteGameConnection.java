package com.samsung.android.sample.coinsoccer.chord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AndroidRuntimeException;

import com.samsung.chord.ChordManager;
import com.samsung.chord.IChordChannel;
import com.samsung.chord.IChordChannelListener;
import com.samsung.chord.IChordManagerListener;

public abstract class BaseRemoteGameConnection implements IRemoteGame,
		IChordManagerListener, Handler.Callback {

	public static final String SETUP_CHANNEL_NAME = "coinSoccerSetupChannel";
	public static final String GAME_CHANNEL_BASE_NAME = "coinSoccerGameChannel";

	public static final String PAYLOAD_TYPE_SEND_GAME_SETTINGS = "payloadTypeSendGameSettings";
	public static final String PAYLOAD_TYPE_REQUEST_GAME_SETTINGS = "payloadTypeRequestGameSettings";
	public static final String PAYLOAD_TYPE_REQUEST_JOIN_GAME = "payloadTypeRequestJoinGame";
	public static final String PAYLOAD_TYPE_ACCEPT_JOIN_GAME = "payloadTypeAcceptJoinGame";
	public static final String PAYLOAD_TYPE_REJECT_JOIN_GAME = "payloadTypeRejectJoinGame";
	public static final String PAYLOAD_TYPE_IN_GAME_DATA = "payloadTypeInGameData";

	public static final byte REJECTION_CODE_REJECTED_BY_USER = 0;
	public static final byte REJECTION_CODE_PROCESSING_OTHER_USER_REQUEST = 1;
	public static final byte REJECTION_CODE_ALREADY_IN_GAME = 2;

	public static final int STATUS_VIRGIN = 0;
	public static final int STATUS_CONNECTING = 1;
	public static final int STATUS_CONNECTED = 2;
	public static final int STATUS_ERROR = -1;
	public static final int STATUS_DESTROYED = -2;

	protected static final byte[][] EMPTY_BYTES_MESSAGE = new byte[][] {};
	protected static final byte[][] REJECTED_BY_USER_BYTES_MESSAGE = new byte[][] { new byte[] { REJECTION_CODE_REJECTED_BY_USER } };
	protected static final byte[][] PROCESSING_OTHER_USER_REQUEST_BYTES_MESSAGE = new byte[][] { new byte[] { REJECTION_CODE_PROCESSING_OTHER_USER_REQUEST } };
	protected static final byte[][] ALREADY_IN_GAME_BYTES_MESSAGE = new byte[][] { new byte[] { REJECTION_CODE_ALREADY_IN_GAME } };
	
	private static final int HANDLER_WHAT_SEND_GAME_DATA = 1;

	public interface GameSetupConnectionCallback {
		
		void onGameSetupConnectionSuccess();

		void onGameSetupConnectionFailure(Throwable cause);
	}

	public interface OnGameConnectionDestroyedListener {

		void onRemoteGameConnectionDestroyed(boolean disconnected);
	}

	protected final IChordChannelListener mGameChannelListener = new SchordChannelEmptyStatusListener() {

		@Override
		public void onDataReceived(String fromNode, String fromChannel, String payloadType,
				byte[][] payload) {
			if (mGameChannelName != null && mGameChannelName.equals(fromChannel)) {
				if (fromNode.equals(mRemotePlayerNodeName)) {
					if (mOnGameDataReceivedListener == null) {
						mInputDataCache.add(payload);
					}
					else {
						mOnGameDataReceivedListener.onGameDataReceived(payload);
					}
				}
			}
		}

		@Override
		public void onNodeLeft(String fromNode, String fromChannel) {
			if (mGameChannelName != null && mGameChannelName.equals(fromChannel)) {
				if (fromNode.equals(mRemotePlayerNodeName)) {
					destroy();
				}
			}
		}
	};
	protected final List<byte[][]> mInputDataCache;
	protected final Handler mUiHandler;
	protected final List<OnGameConnectionDestroyedListener> mOnGameConnectionDestroyedListeners;
	protected List<byte[][]> mOutputDataCache;
	protected ChordManager mChordManager;
	protected GameSetupConnectionCallback mGameSetupConnectionCallback;
	protected volatile OnGameDataReceivedListener mOnGameDataReceivedListener;
	protected String mRemotePlayerNodeName;
	protected IChordChannel mGameChannel;
	protected String mGameChannelName;
	protected int mStatus = STATUS_VIRGIN;

	public BaseRemoteGameConnection() {
		mInputDataCache = new ArrayList<byte[][]>();
		mOutputDataCache = new ArrayList<byte[][]>();
		mOnGameConnectionDestroyedListeners = new ArrayList<OnGameConnectionDestroyedListener>();
		mUiHandler = new Handler(this);
	}

	public void addOnDestroyedListener(OnGameConnectionDestroyedListener onDestroyedListener) {
		mOnGameConnectionDestroyedListeners.add(onDestroyedListener);
	}

	public boolean removeOnDestroyedListener(OnGameConnectionDestroyedListener onDestroyedListener) {
		return mOnGameConnectionDestroyedListeners.remove(onDestroyedListener);
	}

	@Override
	public void setOnGameDataReceivedListener(final OnGameDataReceivedListener onGameDataReceivedListener) {
		mUiHandler.post(new Runnable() {

			@Override
			public void run() {
				if (onGameDataReceivedListener != null && mInputDataCache.size() > 0) {
					Iterator<byte[][]> it = mInputDataCache.iterator();
					while (it.hasNext()) {
						onGameDataReceivedListener.onGameDataReceived(it.next());
						it.remove();
					}
				}
				mOnGameDataReceivedListener = onGameDataReceivedListener;
			}
		});
	}

	public int getStatus() {
		return mStatus;
	}

	@Override
	public void onStarted(String nodeName, int reason) {
		mStatus = STATUS_CONNECTED;
		onConnected();
	}
	
	@Override
	public void onError(int errorCode) {
		if(mStatus == STATUS_VIRGIN) {
			onChordConnectionFailure(new AndroidRuntimeException(
					"Error code: " + errorCode));
		}
		else {
			destroy();
		}
	}
	
	@Override
	public void onNetworkDisconnected() {
		onDestroy(true);
	}

	public void destroy() {
		onDestroy(false);
	}

	@Override
	public void sendGameData(byte[][] data) {
		mUiHandler.obtainMessage(HANDLER_WHAT_SEND_GAME_DATA, 0, 0, data).sendToTarget();
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case HANDLER_WHAT_SEND_GAME_DATA:
				doSendGameDataOnUiThread((byte[][]) msg.obj);
				return true;
		}
		return false;
	}

	private void doSendGameDataOnUiThread(byte[][] data) {
		if (mOutputDataCache == null) {
			mGameChannel.sendData(mRemotePlayerNodeName, PAYLOAD_TYPE_IN_GAME_DATA, data);
		}
		else {
			boolean isRemotePlayerReady = false;
			try {
				isRemotePlayerReady = mGameChannel.getJoinedNodeList().contains(
						mRemotePlayerNodeName);
			} catch (Exception e) {}
			if (isRemotePlayerReady) {
				if (mOutputDataCache.size() > 0) {
					Iterator<byte[][]> it = mOutputDataCache.iterator();
					while (it.hasNext()) {
						mGameChannel.sendData(mRemotePlayerNodeName,
								PAYLOAD_TYPE_IN_GAME_DATA, it.next());
					}
				}
				mOutputDataCache = null;
				mGameChannel.sendData(mRemotePlayerNodeName, PAYLOAD_TYPE_IN_GAME_DATA, data);
			}
			else {
				mOutputDataCache.add(data);
			}
		}
	}

	public void connect(Context context, GameSetupConnectionCallback connectionCallback) {
		mGameSetupConnectionCallback = connectionCallback;
		try {
			mChordManager = ChordManager.getInstance(context.getApplicationContext());
			mChordManager.setHandleEventLooper(context.getMainLooper());
			mChordManager.setNodeKeepAliveTimeout(34000);
		} catch (Exception e) {
			onChordConnectionFailure(e);
			return;
		}
		Iterator<Integer> it = mChordManager.getAvailableInterfaceTypes().iterator();
		while (it.hasNext()) {
			try {
				mChordManager.start(it.next(), this);
				return;

			} catch (Exception e) {}
		}
		mStatus = STATUS_ERROR;
		onChordConnectionFailure(new AndroidRuntimeException(
				"No valid network interface is available."));
	}

	public String getRemotePlayerNodeName() {
		return mRemotePlayerNodeName;
	}

	public boolean isGameReady() {
		return getRemotePlayerNodeName() != null;
	}

	protected void onConnected() {
		if (mGameSetupConnectionCallback != null) {
			mGameSetupConnectionCallback.onGameSetupConnectionSuccess();
		}
	}

	protected void onChordConnectionFailure(Throwable cause) {
		if (mGameSetupConnectionCallback != null) {
			mGameSetupConnectionCallback.onGameSetupConnectionFailure(cause);
		}
	}

	protected void onDestroy(boolean onDisconnected) {
		if (mStatus != STATUS_DESTROYED) {
			mStatus = STATUS_DESTROYED;
			for (OnGameConnectionDestroyedListener l : mOnGameConnectionDestroyedListeners) {
				l.onRemoteGameConnectionDestroyed(onDisconnected);
			}
			if (mChordManager != null) {
				mChordManager.stop();
				mChordManager = null;
			}
		}
	}
}
