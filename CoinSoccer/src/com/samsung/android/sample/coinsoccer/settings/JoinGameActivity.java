package com.samsung.android.sample.coinsoccer.settings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.samsung.android.sample.coinsoccer.CoinSoccerApp;
import com.samsung.android.sample.coinsoccer.GameActivity;
import com.samsung.android.sample.coinsoccer.R;
import com.samsung.android.sample.coinsoccer.chord.BaseRemoteGameConnection;
import com.samsung.android.sample.coinsoccer.chord.RemoteGameGuestSide;
import com.samsung.android.sample.coinsoccer.settings.fragments.MessageDialogFragment;
import com.samsung.android.sample.coinsoccer.settings.fragments.WaitingDialogFragment;

public class JoinGameActivity extends Activity implements RemoteGameGuestSide.GuestSetupListener,
		BaseRemoteGameConnection.GameSetupConnectionCallback,
		BaseRemoteGameConnection.OnGameConnectionDestroyedListener,
		RemoteGameSettingsViewTag.OnItemSelectedListener {

	private class HostsListAdapter extends BaseAdapter {

		private final Map<String, Pair<GameSettings, PlayerSettings>> mRemoteHosts =
				new LinkedHashMap<String, Pair<GameSettings, PlayerSettings>>();
		private Map.Entry<String, Pair<GameSettings, PlayerSettings>>[] mEntries;

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RemoteGameSettingsViewTag tag;
			if (convertView == null) {
				convertView = LayoutInflater.from(parent.getContext()).inflate(
						R.layout.remote_game_settings_item, parent, false);
				tag = new RemoteGameSettingsViewTag(convertView,
						JoinGameActivity.this);
			}
			else {
				tag = (RemoteGameSettingsViewTag) convertView.getTag();
			}
			Entry<String, Pair<GameSettings, PlayerSettings>> item = getItem(position);
			tag.setEntry(item.getKey(), item.getValue().first, item.getValue().second);
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public Map.Entry<String, Pair<GameSettings, PlayerSettings>> getItem(int position) {
			return mEntries[position];
		}

		@Override
		public int getCount() {
			return mEntries == null ? 0 : mEntries.length;
		}

		public void putHost(String nodeName, GameSettings gameSettings, PlayerSettings playerSettings) {
			mRemoteHosts.put(nodeName, new Pair<GameSettings, PlayerSettings>(
					gameSettings, playerSettings));
			notifyDataSetChanged();
		}

		public void removeHost(String nodeName) {
			mRemoteHosts.remove(nodeName);
			notifyDataSetChanged();
		}

		public Pair<GameSettings, PlayerSettings> getHostSettingsByName(String nodeName) {
			return mRemoteHosts.get(nodeName);
		}

		@Override
		public void notifyDataSetChanged() {
			resetEntries();
			super.notifyDataSetChanged();
		}

		@Override
		public void notifyDataSetInvalidated() {
			resetEntries();
			super.notifyDataSetInvalidated();
		}
		
		@SuppressWarnings("unchecked")
		private void resetEntries() {
			mEntries = mRemoteHosts.entrySet().toArray(new Map.Entry[mRemoteHosts.size()]);
		}
	}

	private final HostsListAdapter mAdapter = new HostsListAdapter();
	private PlayerSettingsView mPlayerSettingsView;
	private TextView mStatusView;
	private GamePreferences mGameSettingsPrefs;
	private RemoteGameGuestSide mRemoteGameGuestSide;
	private String mRequestedHostName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.join_game_activity);
		mPlayerSettingsView = (PlayerSettingsView) findViewById(R.id.player_settings_view);
		mStatusView = (TextView) findViewById(R.id.status_text);
		mGameSettingsPrefs = new GamePreferences(this);
		mPlayerSettingsView.setColor(mGameSettingsPrefs.getSecondPlayerColor());
		mPlayerSettingsView.setName(mGameSettingsPrefs.getSecondPlayerName());
		ListView listView = (ListView) findViewById(R.id.remote_games_list);
		listView.setAdapter(mAdapter);
		connect();
	}

	@Override
	protected void onDestroy() {
		cleanUpOrphanedRemoteConnection();
		super.onDestroy();
	}

	public void connect() {
		mStatusView.setEnabled(false);
		mStatusView.setText(R.string.connecting);
		CoinSoccerApp.get(this).clearOffRemoteGameConnectionIfExists();
		mRemoteGameGuestSide = new RemoteGameGuestSide(this);
		mRemoteGameGuestSide.addOnDestroyedListener(this);
		mRemoteGameGuestSide.connect(this, this);
	}

	@Override
	public void onGameSetupConnectionSuccess() {
		mStatusView.setText(R.string.join_game_waiting_for_hosts);
	}

	@Override
	public void onGameSetupConnectionFailure(Throwable cause) {
		MessageDialogFragment.show(this, getString(R.string.connection_failure),
				getString(R.string.connection_failure_with_error_message, cause.getMessage()), 
				true, true);
	}

	@Override
	public void onRemoteGameConnectionDestroyed(boolean disconnected) {
		MessageDialogFragment.show(this, getString(R.string.join_game_disconnected_title),
				getString(R.string.join_game_disconnected_message), true, true);
	}

	@Override
	public void onReceiveHostSettings(String nodeName,
			GameSettings gameSettings, PlayerSettings playerSettings) {
		mAdapter.putHost(nodeName, gameSettings, playerSettings);
		mStatusView.setText(getString(R.string.join_game_available_game_hosts, 
				mAdapter.getCount()));
	}

	@Override
	public void onHostLeft(String nodeName) {
		mAdapter.removeHost(nodeName);
		if (mAdapter.getCount() == 0) {
			mStatusView.setText(R.string.join_game_waiting_for_hosts);
		}
		if (mRequestedHostName != null && nodeName.equals(mRequestedHostName)) {
			mRequestedHostName = null;
			WaitingDialogFragment.dismissIfExists(this);
			MessageDialogFragment.show(this,
							getString(R.string.join_game_remote_host_disconnected_title),
							getString(R.string.join_game_remote_host_disconnected_message),
							true, false);
			mPlayerSettingsView.setEnabled(true);
		}
	}

	@Override
	public void onJoinRequestRejected(String nodeName, byte rejectionCode) {
		mRequestedHostName = null;
		WaitingDialogFragment.dismissIfExists(this);
		int messageResId;
		switch (rejectionCode) {
			case BaseRemoteGameConnection.REJECTION_CODE_ALREADY_IN_GAME:
				mAdapter.removeHost(nodeName);
				messageResId = R.string.join_game_request_rejected_already_in_game_message;
				break;
			case BaseRemoteGameConnection.REJECTION_CODE_PROCESSING_OTHER_USER_REQUEST:
				messageResId = R.string.join_game_request_rejected_processing_other_message;
				break;
			case BaseRemoteGameConnection.REJECTION_CODE_REJECTED_BY_USER:
			default:
				messageResId = R.string.join_game_request_rejected_message;
				break;
		}
		MessageDialogFragment.show(this,
				getString(R.string.join_game_request_rejected_title),
				getString(messageResId), true, false);
		mPlayerSettingsView.setEnabled(true);
	}

	@Override
	public void onJoinRequestAccepted(String nodeName) {
		CoinSoccerApp app = CoinSoccerApp.get(this);
		Pair<GameSettings, PlayerSettings> pair = mAdapter.getHostSettingsByName(nodeName);
		app.setGameSettings(pair.first);
		app.setPlayerSettings(pair.second);
		app.setRemoteGameConnection(mRemoteGameGuestSide);
		mRemoteGameGuestSide.removeOnDestroyedListener(this);
		mRemoteGameGuestSide = null;
		startActivity(new Intent(this, GameActivity.class));
		finish();
	}
	
	@Override
	public void onItemSelected(String nodeName) {
		WaitingDialogFragment.show(this,
				getString(R.string.join_game_waiting_for_acceptance_title),
				getString(R.string.join_game_waiting_for_acceptance_message),
				true, true);
		mPlayerSettingsView.setEnabled(false);
		mGameSettingsPrefs.saveSecondPlayerOnly(mPlayerSettingsView);
		PlayerSettings playerSettings = new PlayerSettings(Which.SECOND,
				mPlayerSettingsView.getColor(), mPlayerSettingsView.getName());
		CoinSoccerApp.get(this).setPlayerSettings(playerSettings);
		mRequestedHostName = nodeName;
		mRemoteGameGuestSide.sendPlayerSettings(mRequestedHostName, playerSettings);
	}

	private void cleanUpOrphanedRemoteConnection() {
		if (mRemoteGameGuestSide != null && !mRemoteGameGuestSide.isGameReady()) {
			mRemoteGameGuestSide.removeOnDestroyedListener(this);
			mRemoteGameGuestSide.destroy();
			mRemoteGameGuestSide = null;
		}
	}
}
