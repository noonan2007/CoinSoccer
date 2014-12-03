package com.samsung.android.sample.coinsoccer.settings.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

public class MessageDialogFragment extends CommonBaseDialogFragment {

	private static final String TAG = MessageDialogFragment.class.getName();

	public static void show(Activity activity, String title, String message,
			boolean cancelable, boolean shouldFinishActivityOnDismiss) {
		dismissIfExists(activity);
		MessageDialogFragment f = new MessageDialogFragment();
		f.setArguments(createArguments(title, message, cancelable, shouldFinishActivityOnDismiss));
		f.show(activity.getFragmentManager(), TAG);
	}
	
	public static void dismissIfExists(Activity activity) {
		MessageDialogFragment f = (MessageDialogFragment) activity.getFragmentManager().findFragmentByTag(TAG);
		if (f != null && f.isAdded()) {
			f.dismiss();
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		if (args.containsKey(ARG_TITLE)) {
			builder.setTitle(args.getString(ARG_TITLE));
		}
		if (args.containsKey(ARG_MESSAGE)) {
			builder.setMessage(args.getString(ARG_MESSAGE));
		}
		boolean cancelable = args.getBoolean(ARG_CANCELABLE, true);
		if (cancelable) {
			builder.setPositiveButton(android.R.string.ok, null);
			setCancelable(cancelable);
		}
		return builder.create();
	}
}