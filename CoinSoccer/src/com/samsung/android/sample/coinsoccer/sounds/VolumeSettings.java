package com.samsung.android.sample.coinsoccer.sounds;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class VolumeSettings implements OnSharedPreferenceChangeListener {
	
	private static final String PREF_NAME = VolumeSettings.class.getName();
	private static final String MUTED = "muted";
	private static final String BACKGROUND_SOUND_DISABLED = "backgroundSoundDisabled";

	public interface VolumeSettingsChangeListener {

		void onVolumeSettingsChange();
	}

	private VolumeSettingsChangeListener mListener;
	private final SharedPreferences mSharedPreferences;

	public VolumeSettings(Context context) {
		mSharedPreferences = context.getSharedPreferences(
				PREF_NAME, Context.MODE_PRIVATE);
	}

	public boolean isMuted() {
		return mSharedPreferences.getBoolean(MUTED, false);
	}

	public boolean isBackgroundSoundDisabled() {
		return mSharedPreferences.getBoolean(BACKGROUND_SOUND_DISABLED, false);
	}

	public void setMuted(boolean isMuted) {
		mSharedPreferences.edit().putBoolean(MUTED, isMuted).apply();
	}

	public void setBackgroundSoundDisabled(boolean isBackgroundSoundDisabled) {
		mSharedPreferences.edit().putBoolean(BACKGROUND_SOUND_DISABLED, isBackgroundSoundDisabled).apply();
	}

	public void setVolumeSettingsChangeListener(VolumeSettingsChangeListener listener) {
		mListener = listener;
		if (listener == null) {
			mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		}
		else {
			mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		mListener.onVolumeSettingsChange();
	}
}
