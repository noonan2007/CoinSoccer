package com.samsung.android.sample.coinsoccer.sounds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.samsung.android.sample.coinsoccer.sounds.VolumeSettings.VolumeSettingsChangeListener;

public class GameSounds implements VolumeSettingsChangeListener, Runnable, Disposable {

	private static final String SHOT_CANCELLED_SOUND_PATH = "sounds/shotcancelled.ogg";
	private static final String COIN_COLLISION_SOUND_PATH = "sounds/coincollision.ogg";
	private static final String WHISTLE_LONG_SOUND_PATH = "sounds/whistlelong.ogg";
	private static final String WHISTLE_SHORT_SOUND_PATH = "sounds/whistleshort.ogg";
	private static final String WHISTLE_THRICE_SOUND_PATH = "sounds/whistlethrice.ogg";
	private static final String CLICK_SOUND_PATH = "sounds/click.ogg";
	private static final String GOAL_CHEERS_MUSIC_PATH = "sounds/goalcheers.ogg";
	private static final String BACKGROUND_MUSIC_PATH = "sounds/background.ogg";

	private Sound mShotCancelledSound;
	private Sound mCoinCollisionSound;
	private Sound mWhistleSound;
	private Sound mWhistleShortSound;
	private Sound mWhistleThriceSound;
	private Sound mClickSound;
	private Music mGoalCheersMusic;
	private Music mBackgroundMusic;
	private final VolumeSettings mVolumeSettings;
	private boolean mIsGdxThreadScheduled;
	private boolean mIsFrozen;

	public GameSounds(VolumeSettings volumeSettings) {
		mVolumeSettings = volumeSettings;
		mVolumeSettings.setVolumeSettingsChangeListener(this);
	}

	public void onGdxCreate() {
		mShotCancelledSound = Gdx.audio.newSound(Gdx.files.internal(SHOT_CANCELLED_SOUND_PATH));
		mCoinCollisionSound = Gdx.audio.newSound(Gdx.files.internal(COIN_COLLISION_SOUND_PATH));
		mWhistleSound = Gdx.audio.newSound(Gdx.files.internal(WHISTLE_LONG_SOUND_PATH));
		mWhistleShortSound = Gdx.audio.newSound(Gdx.files.internal(WHISTLE_SHORT_SOUND_PATH));
		mWhistleThriceSound = Gdx.audio.newSound(Gdx.files.internal(WHISTLE_THRICE_SOUND_PATH));
		mClickSound = Gdx.audio.newSound(Gdx.files.internal(CLICK_SOUND_PATH));
		mGoalCheersMusic = Gdx.audio.newMusic(Gdx.files.internal(GOAL_CHEERS_MUSIC_PATH));
		mBackgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC_PATH));
		adjustBackgroundMusic();
	}

	public void shotCancelled() {
		if (!shouldNotPlay()) {
			mShotCancelledSound.play(1);
		}
	}

	public void coinCollision() {
		if (!shouldNotPlay()) {
			mCoinCollisionSound.play(1);
		}
	}

	public void whistleLong() {
		if (!shouldNotPlay()) {
			mWhistleSound.play(1);
		}
	}

	public void whistleShort() {
		if (!shouldNotPlay()) {
			mWhistleShortSound.play(1);
		}
	}

	public void whistleThrice() {
		if (!shouldNotPlay()) {
			mWhistleThriceSound.play(1);
		}
	}

	public void click() {
		if (!shouldNotPlay()) {
			mClickSound.play(1);
		}
	}

	public void goalCheers() {
		if (mGoalCheersMusic.isPlaying()) {
			mGoalCheersMusic.stop();
		}
		mGoalCheersMusic.setVolume(shouldNotPlay() ? 0 : 1);
		mGoalCheersMusic.play();
	}

	/**
	 * Caution: Must be called from GDX rendering thread
	 * 
	 * @param isFrozen
	 */
	public void setFrozen(boolean isFrozen) {
		mIsFrozen = isFrozen;
		run();
	}

	@Override
	public void onVolumeSettingsChange() {
		if (!mIsGdxThreadScheduled) {
			mIsGdxThreadScheduled = true;
			Gdx.app.postRunnable(this);
		}
	}

	@Override
	public void run() {
		mIsGdxThreadScheduled = false;
		if (mGoalCheersMusic.isPlaying()) {
			mGoalCheersMusic.setVolume(shouldNotPlay() ? 0 : 1);
		}
		adjustBackgroundMusic();
	}

	@Override
	public void dispose() {
		disposeSounds();
		mGoalCheersMusic.stop();
		mGoalCheersMusic.dispose();
		mBackgroundMusic.stop();
		mBackgroundMusic.dispose();
	}

	private boolean shouldNotPlay() {
		return mIsFrozen || mVolumeSettings.isMuted();
	}

	private void adjustBackgroundMusic() {
		if (mVolumeSettings.isMuted() || mVolumeSettings.isBackgroundSoundDisabled() || mIsFrozen) {
			mBackgroundMusic.stop();
		}
		else {
			mBackgroundMusic.setLooping(true);
			mBackgroundMusic.setVolume(1);
			mBackgroundMusic.play();
		}
	}

	private void stopSounds() {
		mCoinCollisionSound.stop();
		mWhistleSound.stop();
		mWhistleShortSound.stop();
		mWhistleThriceSound.stop();
		mShotCancelledSound.stop();
		mClickSound.stop();
	}

	private void disposeSounds() {
		stopSounds();
		mCoinCollisionSound.dispose();
		mWhistleSound.dispose();
		mWhistleShortSound.dispose();
		mWhistleThriceSound.dispose();
		mShotCancelledSound.dispose();
		mClickSound.dispose();
	}
}
