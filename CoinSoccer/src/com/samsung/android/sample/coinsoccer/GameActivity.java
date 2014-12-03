package com.samsung.android.sample.coinsoccer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.samsung.android.sample.coinsoccer.chord.BaseRemoteGameConnection;
import com.samsung.android.sample.coinsoccer.game.CoinSoccerGame;
import com.samsung.android.sample.coinsoccer.game.CoinSoccerGameRemoteImpl;
import com.samsung.android.sample.coinsoccer.game.GameSettingsHelper;
import com.samsung.android.sample.coinsoccer.hud.HudInfoBoxes;
import com.samsung.android.sample.coinsoccer.hud.HudViewWrapper;
import com.samsung.android.sample.coinsoccer.hud.ThreadSafeHudUpdater;
import com.samsung.android.sample.coinsoccer.settings.GameSettings;
import com.samsung.android.sample.coinsoccer.settings.PlayerSettings;
import com.samsung.android.sample.coinsoccer.sounds.VolumeSettings;
import com.samsung.android.sample.coinsoccer.sounds.VolumeSettingsDialogFragment;
import com.samsung.android.sample.coinsoccer.statistics.GameStatisticsDialogFragment;

/**
 * Main game Activity. Handles the game and user input during the game.
 */
public class GameActivity extends AndroidApplication implements
		BaseRemoteGameConnection.OnGameConnectionDestroyedListener,
		CoinSoccerGame.OnGameEndListener, Handler.Callback {

	private static final int MESSAGE_WHAT_SHOW_STATISTICS = 1;
	private static final int MESSAGE_WHAT_END_GAME = 2;
	
	final Runnable mShowGameStatisticsFromUiThreadRunnable = new Runnable() {

		@Override
		public void run() {
			Message msg = mUiHandler.obtainMessage(MESSAGE_WHAT_SHOW_STATISTICS);
			msg.setData(mGame.getStatistics().toBundle());
			msg.sendToTarget();
		}
	};
	CoinSoccerGame mGame;
	private boolean mIsLocalGame;
	private final Handler mUiHandler = new Handler(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.game_activity);

		final HudInfoBoxes exposedDialogs = new HudInfoBoxes(this);
		final CoinSoccerApp app = CoinSoccerApp.get(this);
		final BaseRemoteGameConnection remoteGameConnection = app.getRemoteGameConnection();
		final GameSettings gameSettings = app.getGameSettings();
		final PlayerSettings firstPlayerSettings = app.getFirstPlayerSettings();
		final PlayerSettings secondPlayerSettings = app.getSecondPlayerSettings();
		final GameSettingsHelper gameSettingsHelper = new GameSettingsHelper(gameSettings);
		final ThreadSafeHudUpdater hudUpdater = new ThreadSafeHudUpdater(
				new HudViewWrapper(findViewById(R.id.game_activity_root), gameSettings,
						firstPlayerSettings, secondPlayerSettings));
		final VolumeSettings VolumeSettings = new VolumeSettings(this);
		mIsLocalGame = remoteGameConnection == null;
		if (mIsLocalGame) {
			mGame = new CoinSoccerGame(this, exposedDialogs, gameSettingsHelper,
					firstPlayerSettings, secondPlayerSettings, hudUpdater, VolumeSettings);
		}
		else {
			mGame = new CoinSoccerGameRemoteImpl(this, exposedDialogs, 
					gameSettingsHelper, firstPlayerSettings, secondPlayerSettings, 
					hudUpdater, VolumeSettings, remoteGameConnection);
			remoteGameConnection.addOnDestroyedListener(this);
		}
		((FrameLayout) findViewById(R.id.gl_view_frame)).addView(
				initializeForView(mGame, false), createLayoutParams());
		if (!mIsLocalGame) {
			if (remoteGameConnection.getStatus() != BaseRemoteGameConnection.STATUS_CONNECTED
					|| !remoteGameConnection.isGameReady()) {
				forceGameEnd();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_MENU:
			case KeyEvent.KEYCODE_BACK:
				GameMenuDialogFragment.show(getFragmentManager());
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
			case KeyEvent.KEYCODE_VOLUME_UP:
				showVolumeSettings();
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onGameEnd() {
		mUiHandler.removeMessages(MESSAGE_WHAT_END_GAME);
		mUiHandler.removeMessages(MESSAGE_WHAT_SHOW_STATISTICS);
		Message msg = mUiHandler.obtainMessage(MESSAGE_WHAT_END_GAME);
		msg.setData(mGame.getStatistics().toBundle());
		msg.sendToTarget();
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case MESSAGE_WHAT_END_GAME:
				CoinSoccerApp.get(GameActivity.this).clearOffRemoteGameConnectionIfExists();
				// no break;
			case MESSAGE_WHAT_SHOW_STATISTICS:
				GameStatisticsDialogFragment.show(getFragmentManager(), msg.peekData());
				return true;
		}
		return false;
	}

	@Override
	public void onRemoteGameConnectionDestroyed(boolean disconnected) {
		forceGameEnd();
	}

	void forceGameEnd() {
		mGame.forceGameEndFromUiThread();
	}

	void showVolumeSettings() {
		VolumeSettingsDialogFragment.show(getFragmentManager());
	}

	boolean isGamePaused() {
		return mGame.isGamePaused();
	}

	void onGameMenuItemClicked(int which) {
		switch (which) {

			case R.id.game_menu_pause:
				mGame.setGamePausedFromAndroidUi(true);
				break;

			case R.id.game_menu_resume:
				mGame.setGamePausedFromAndroidUi(false);
				break;

			case R.id.game_menu_statistics:
				postRunnable(mShowGameStatisticsFromUiThreadRunnable);
				break;

			case R.id.game_menu_volume:
				showVolumeSettings();
				break;

			case R.id.game_menu_quit:
				CloseConfirmDialogFragment.show(getFragmentManager());
				break;
		}
	}
}
