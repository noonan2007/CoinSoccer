package com.samsung.android.sample.coinsoccer.settings.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;

public class WaitingDialogFragment extends CommonBaseDialogFragment {

	private static final String DEFAULT_TAG = WaitingDialogFragment.class.getName();
	
	public static void show(Activity activity, String title, String message,
			boolean cancelable, boolean shouldFinishActivityOnDismiss) {
		dismissIfExists(activity);
		WaitingDialogFragment f = new WaitingDialogFragment();
		f.setArguments(createArguments(title, message, cancelable, shouldFinishActivityOnDismiss));
		f.show(activity.getFragmentManager(), DEFAULT_TAG);
	}

	public static void dismissIfExists(Activity activity) {
		WaitingDialogFragment f = (WaitingDialogFragment) activity.getFragmentManager().findFragmentByTag(DEFAULT_TAG);
		if (f != null && f.isAdded()) {
			f.dismiss();
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		ProgressDialog d = new ProgressDialog(getActivity());
		d.setTitle(args.getString(ARG_TITLE));
		d.setMessage(args.getString(ARG_MESSAGE));
		d.setCanceledOnTouchOutside(false);
		setCancelable(args.getBoolean(ARG_CANCELABLE, false));
		return d;
	}
}