package com.samsung.android.sample.coinsoccer.settings;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.samsung.android.sample.coinsoccer.CoinSoccerApp;
import com.samsung.android.sample.coinsoccer.GameActivity;
import com.samsung.android.sample.coinsoccer.R;
import com.samsung.android.sample.coinsoccer.chord.BaseRemoteGameConnection;
import com.samsung.android.sample.coinsoccer.chord.RemoteGameHostSide;

public class WaitingForRemotePlayerDialogFragment extends DialogFragment implements
		RemoteGameHostSide.HostSetupListener, BaseRemoteGameConnection.GameSetupConnectionCallback,
		BaseRemoteGameConnection.OnGameConnectionDestroyedListener {

	private RemoteGameHostSide mRemoteGameHostSide;
	private TextView mStatusTextView;
	private PlayerSettings mRemotePlayerSettings;
	private JoinRequestFace mJoinRequestFace;
	private WaitingFace mWaitingFace;
	private ErrorFace mErrorFace;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog d = super.onCreateDialog(savedInstanceState);
		d.setCancelable(true);
		d.setCanceledOnTouchOutside(false);
		d.setTitle("Remote Player");
		return d;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.waiting_for_remote_player_dialog, container, false);
		mStatusTextView = (TextView) v.findViewById(R.id.status_text);
		mStatusTextView.setText("Initializing...");
		mWaitingFace = new WaitingFace(v.findViewById(R.id.waiting_face));
		mJoinRequestFace = new JoinRequestFace(v.findViewById(R.id.join_request_face));
		mErrorFace = new ErrorFace(v.findViewById(R.id.error_face));
		CoinSoccerApp app = CoinSoccerApp.get(getActivity());
		app.clearOffRemoteGameConnectionIfExists();
		mRemoteGameHostSide = new RemoteGameHostSide(this,
				app.getGameSettings(), app.getFirstPlayerSettings());
		mRemoteGameHostSide.addOnDestroyedListener(this);
		mRemoteGameHostSide.connect(getActivity(), this);
		return v;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		cleanUpOrphanedRemoteConnection();
		Activity a = getActivity();
		if (a instanceof NewGameActivity) {
			((NewGameActivity) a).resetPlayerType();
		}
		super.onDismiss(dialog);
	}

	@Override
	public void onRequestJoinGame(PlayerSettings playerSettings) {
		mJoinRequestFace.turnOn(mRemotePlayerSettings = playerSettings);
	}

	@Override
	public void onRequestJoinNodeLeft() {
		mRemotePlayerSettings = null;
		mWaitingFace.turnOn(getString(R.string.waiting_for_remote_node_left));
	}

	@Override
	public void onGameSetupConnectionSuccess() {
		mWaitingFace.turnOn(getString(R.string.waiting_for_remote_ready));
	}

	@Override
	public void onGameSetupConnectionFailure(Throwable cause) {
		mErrorFace.turnOn(TextUtils.isEmpty(cause.getMessage()) ?
				getString(R.string.connection_failure) :
				getString(R.string.connection_failure_with_error_message,
						cause.getMessage()));
	}

	@Override
	public void onRemoteGameConnectionDestroyed(boolean disconnected) {
		mErrorFace.turnOn(getString(R.string.waiting_for_remote_disconnected));
	}

	void rejectRemotePlayer() {
		mRemoteGameHostSide.rejectGuestJoinRequest();
		mRemotePlayerSettings = null;
		mWaitingFace.turnOn(getString(R.string.waiting_for_remote_after_reject));
	}

	void acceptRemotePlayer() {
		CoinSoccerApp app = CoinSoccerApp.get(getActivity());
		app.setPlayerSettings(mRemotePlayerSettings);
		mRemotePlayerSettings = null;
		app.setRemoteGameConnection(mRemoteGameHostSide);
		mRemoteGameHostSide.acceptGuestJoinRequest();
		mRemoteGameHostSide.removeOnDestroyedListener(this);
		mRemoteGameHostSide = null;
		getActivity().startActivity(new Intent(getActivity(), GameActivity.class));
		getActivity().finish();
	}

	private void cleanUpOrphanedRemoteConnection() {
		if (mRemoteGameHostSide != null && !mRemoteGameHostSide.isGameReady()) {
			mRemoteGameHostSide.removeOnDestroyedListener(this);
			mRemoteGameHostSide.destroy();
			mRemoteGameHostSide = null;
		}
	}

	private class WaitingFace {

		final View mRoot;
		final TextView mStatusText;

		public WaitingFace(View v) {
			mRoot = v;
			mStatusText = (TextView) v.findViewById(R.id.status_text);
		}

		public void turnOn(String text) {
			mErrorFace.mRoot.setVisibility(View.GONE);
			mJoinRequestFace.mRoot.setVisibility(View.GONE);
			mRoot.setVisibility(View.VISIBLE);
			mStatusText.setText(text);
		}
	}

	private class ErrorFace implements View.OnClickListener {

		final View mRoot;
		final TextView mErrorText;

		public ErrorFace(View v) {
			mRoot = v;
			mErrorText = (TextView) v.findViewById(R.id.error_text);
			v.findViewById(R.id.close_button).setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			dismiss();
		}

		public void turnOn(String text) {
			mWaitingFace.mRoot.setVisibility(View.GONE);
			mJoinRequestFace.mRoot.setVisibility(View.GONE);
			mRoot.setVisibility(View.VISIBLE);
			mErrorText.setText(text);
		}
	}

	private class JoinRequestFace implements View.OnClickListener {

		final PlayerSettingsView mRemotePlayerSettingsView;
		final View mRoot;
		final View mAcceptButton;
		final View mRejectButton;

		public JoinRequestFace(View v) {
			mRoot = v;
			mRemotePlayerSettingsView = (PlayerSettingsView) v.findViewById(R.id.player_settings_view);
			mRemotePlayerSettingsView.setEnabled(false);
			mAcceptButton = v.findViewById(R.id.accept_button);
			mAcceptButton.setOnClickListener(this);
			mRejectButton = v.findViewById(R.id.reject_button);
			mRejectButton.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			if (v == mAcceptButton) {
				v.setEnabled(false);
				mRejectButton.setEnabled(false);
				acceptRemotePlayer();
			}
			else if (v == mRejectButton) {
				rejectRemotePlayer();
			}
		}

		public void turnOn(PlayerSettings playerSettings) {
			mErrorFace.mRoot.setVisibility(View.GONE);
			mWaitingFace.mRoot.setVisibility(View.GONE);
			mRoot.setVisibility(View.VISIBLE);
			mRemotePlayerSettingsView.setColor(playerSettings.color);
			mRemotePlayerSettingsView.setName(playerSettings.name);
		}
	}
}