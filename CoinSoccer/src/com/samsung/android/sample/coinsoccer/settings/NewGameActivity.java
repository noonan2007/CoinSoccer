package com.samsung.android.sample.coinsoccer.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;

import com.samsung.android.sample.coinsoccer.CoinSoccerApp;
import com.samsung.android.sample.coinsoccer.GameActivity;
import com.samsung.android.sample.coinsoccer.R;

public class NewGameActivity extends Activity implements OnClickListener {

	private GamePreferences mGameSettingsPrefs;
	private GameSettingsView mGameSettingsView;
	private PlayerSettingsView mFirstPlayerSettingsView;
	private PlayerSettingsView mSecondPlayerSettingsView;
	private CheckBox mLocalPlayerButton;
	private CheckBox mRemotePlayerButton;
	private View mStartLocalGameButton;
	private View mSecondPlayerDivider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.new_game_activity);
		mGameSettingsPrefs = new GamePreferences(this);
		mGameSettingsView = (GameSettingsView) findViewById(R.id.game_settings);
		mFirstPlayerSettingsView = (PlayerSettingsView) findViewById(R.id.first_player_settings);
		mSecondPlayerSettingsView = (PlayerSettingsView) findViewById(R.id.second_player_settings);
		mSecondPlayerDivider = findViewById(R.id.second_player_divider);
		mLocalPlayerButton = (CheckBox) findViewById(R.id.local_player_button);
		mLocalPlayerButton.setOnClickListener(this);
		mRemotePlayerButton = (CheckBox) findViewById(R.id.remote_player_button);
		mRemotePlayerButton.setOnClickListener(this);
		mStartLocalGameButton = findViewById(R.id.start_game_button);
		mStartLocalGameButton.setOnClickListener(this);
		fillUiFromPrefs();
	}

	@Override
	protected void onDestroy() {
		mGameSettingsView.destroyDialogs();
		mFirstPlayerSettingsView.destroyDialog();
		mSecondPlayerSettingsView.destroyDialog();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.start_game_button:
				startLocalGame();
				break;
			case R.id.local_player_button:
				if (mLocalPlayerButton.isChecked()) {
					selectLocalPlayer();
				}
				else {
					resetPlayerType();
				}
				break;
			case R.id.remote_player_button:
				if (mRemotePlayerButton.isChecked()) {
					selectRemotePlayer();
				}
				else {
					resetPlayerType();
				}
				break;
		}
	}

	void resetPlayerType() {
		mGameSettingsView.setEnabled(true);
		mFirstPlayerSettingsView.setEnabled(true);
		mRemotePlayerButton.setChecked(false);
		mLocalPlayerButton.setChecked(false);
		mStartLocalGameButton.setEnabled(false);
		mSecondPlayerSettingsView.setVisibility(View.GONE);
		mSecondPlayerDivider.setVisibility(View.GONE);
	}

	private void selectRemotePlayer() {
		mGameSettingsView.setEnabled(false);
		mFirstPlayerSettingsView.setEnabled(false);
		mRemotePlayerButton.setChecked(true);
		mLocalPlayerButton.setChecked(false);
		mStartLocalGameButton.setEnabled(false);
		mSecondPlayerSettingsView.setVisibility(View.GONE);
		mSecondPlayerDivider.setVisibility(View.GONE);

		mGameSettingsPrefs.save(mGameSettingsView,
				mFirstPlayerSettingsView, mSecondPlayerSettingsView);
		CoinSoccerApp app = CoinSoccerApp.get(this);
		app.setGameSettings(GameSettings.createFromUserInput(mGameSettingsView));
		app.setPlayerSettings(new PlayerSettings(Which.FIRST,
				mFirstPlayerSettingsView.getColor(),
				mFirstPlayerSettingsView.getName()));
		new WaitingForRemotePlayerDialogFragment().show(getFragmentManager(),
				WaitingForRemotePlayerDialogFragment.class.getName());
	}

	private void selectLocalPlayer() {
		mGameSettingsView.setEnabled(true);
		mFirstPlayerSettingsView.setEnabled(true);
		mLocalPlayerButton.setChecked(true);
		mRemotePlayerButton.setChecked(false);
		mStartLocalGameButton.setEnabled(true);
		mSecondPlayerSettingsView.setVisibility(View.VISIBLE);
		mSecondPlayerDivider.setVisibility(View.VISIBLE);
	}

	private void startLocalGame() {
		mGameSettingsPrefs.save(mGameSettingsView,
				mFirstPlayerSettingsView, mSecondPlayerSettingsView);
		CoinSoccerApp app = CoinSoccerApp.get(this);
		app.setGameSettings(GameSettings.createFromUserInput(mGameSettingsView));
		app.setPlayerSettings(new PlayerSettings(Which.FIRST,
				mFirstPlayerSettingsView.getColor(),
				mFirstPlayerSettingsView.getName()));
		app.setPlayerSettings(new PlayerSettings(Which.SECOND,
				mSecondPlayerSettingsView.getColor(),
				mSecondPlayerSettingsView.getName()));
		startActivity(new Intent(this, GameActivity.class));
		finish();
	}

	private void fillUiFromPrefs() {
		mGameSettingsView.setGameSettings(mGameSettingsPrefs.getTimePerTurnMillis(),
				mGameSettingsPrefs.getTurnLimit(), mGameSettingsPrefs.getScoreLimit(),
				mGameSettingsPrefs.getLimitType());
		mFirstPlayerSettingsView.setName(mGameSettingsPrefs.getFirstPlayerName());
		mFirstPlayerSettingsView.setColor(mGameSettingsPrefs.getFirstPlayerColor());
		mSecondPlayerSettingsView.setName(mGameSettingsPrefs.getSecondPlayerName());
		mSecondPlayerSettingsView.setColor(mGameSettingsPrefs.getSecondPlayerColor());
	}
}
