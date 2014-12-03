package com.samsung.android.sample.coinsoccer;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;

/*
 * Queer but useful! - on tablets options menu is not shown unless action bar is visible
 */
public class GameMenuDialogFragment extends DialogFragment implements
		OnClickListener {

	private static final String TAG = GameMenuDialogFragment.class.getName();

	public static void show(FragmentManager fm) {
		dismissIfExists(fm);
		new GameMenuDialogFragment().show(fm, TAG);
	}
	
	public static void dismissIfExists(FragmentManager fm) {
		GameMenuDialogFragment f = (GameMenuDialogFragment) fm.findFragmentByTag(TAG);
		if(f != null && f.isAdded()) {
			f.dismiss();
		}
	}

	private View mPauseButton;
	private View mResumeButton;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog d = super.onCreateDialog(savedInstanceState);
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.getWindow().getAttributes().gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		d.setCanceledOnTouchOutside(true);
		return d;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.game_menu_fragment, container, false);
		mPauseButton = v.findViewById(R.id.game_menu_pause);
		mPauseButton.setOnClickListener(this);
		mResumeButton = v.findViewById(R.id.game_menu_resume);
		mResumeButton.setOnClickListener(this);
		v.findViewById(R.id.game_menu_quit).setOnClickListener(this);
		v.findViewById(R.id.game_menu_statistics).setOnClickListener(this);
		v.findViewById(R.id.game_menu_volume).setOnClickListener(this);
		togglePauseResume(((GameActivity) getActivity()).isGamePaused());
		return v;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.game_menu_pause:
				togglePauseResume(true);
				break;
			case R.id.game_menu_resume:
				togglePauseResume(false);
				break;
		}
		((GameActivity) getActivity()).onGameMenuItemClicked(v.getId());
		dismiss();
	}

	private void togglePauseResume(boolean paused) {
		if (paused) {
			mPauseButton.setVisibility(View.GONE);
			mResumeButton.setVisibility(View.VISIBLE);
		}
		else {
			mResumeButton.setVisibility(View.GONE);
			mPauseButton.setVisibility(View.VISIBLE);
		}
	}
}