package com.samsung.android.sample.coinsoccer.sounds;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.samsung.android.sample.coinsoccer.R;

public class VolumeSettingsDialogFragment extends DialogFragment implements OnClickListener, OnSeekBarChangeListener,
		OnKeyListener, OnCheckedChangeListener {

	public static void show(FragmentManager fm) {
		String tag = VolumeSettingsDialogFragment.class.getName();
		VolumeSettingsDialogFragment fragment = (VolumeSettingsDialogFragment) fm.findFragmentByTag(tag);
		if (fragment == null) {
			fragment = new VolumeSettingsDialogFragment();
		}
		fragment.show(fm, tag);
	}

	private SeekBar mVolumeBar;
	private CheckBox mMuteToggle;
	private CheckBox mBackgroundSoundToggle;
	private VolumeSettings mVolumeSettings;

	public VolumeSettingsDialogFragment() {
		setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mVolumeSettings = new VolumeSettings(getActivity());

		getDialog().setTitle(R.string.game_menu_volume);
		getDialog().setOnKeyListener(this);

		View view = inflater.inflate(R.layout.audio_settings_fragment, container, false);

		mVolumeBar = (SeekBar) view.findViewById(R.id.volume_bar);
		AudioManager am = getAudioManager();
		mVolumeBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		mVolumeBar.setProgress(am.getStreamVolume(AudioManager.STREAM_MUSIC));
		mVolumeBar.setOnSeekBarChangeListener(this);

		mMuteToggle = (CheckBox) view.findViewById(R.id.mute_toggle);
		mMuteToggle.setOnCheckedChangeListener(this);
		mBackgroundSoundToggle = (CheckBox) view
				.findViewById(R.id.backgroud_sound_toggle);

		mMuteToggle.setChecked(mVolumeSettings.isMuted());
		mBackgroundSoundToggle.setChecked(mVolumeSettings
				.isBackgroundSoundDisabled());

		mMuteToggle.setOnClickListener(this);
		mBackgroundSoundToggle.setOnClickListener(this);

		return view;
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == mMuteToggle.getId()) {
			mVolumeSettings.setMuted(mMuteToggle.isChecked());
		}
		else if (v.getId() == mBackgroundSoundToggle.getId()) {
			mVolumeSettings.setBackgroundSoundDisabled(mBackgroundSoundToggle.isChecked());
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		getAudioManager().setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {}

	AudioManager getAudioManager() {
		return (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				mVolumeBar.setProgress(
						mVolumeBar.getProgress() - mVolumeBar.getKeyProgressIncrement());
				return true;
			case KeyEvent.KEYCODE_VOLUME_UP:
				mVolumeBar.setProgress(
						mVolumeBar.getProgress() + mVolumeBar.getKeyProgressIncrement());
				return true;
		}
		return false;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		mBackgroundSoundToggle.setEnabled(!mMuteToggle.isChecked());
	}
}
