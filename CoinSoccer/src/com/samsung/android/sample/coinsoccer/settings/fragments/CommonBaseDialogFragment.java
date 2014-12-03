package com.samsung.android.sample.coinsoccer.settings.fragments;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public abstract class CommonBaseDialogFragment extends DialogFragment {

	protected static final String ARG_TITLE = "title";
	protected static final String ARG_MESSAGE = "message";
	protected static final String ARG_CANCELABLE = "cancelable";
	protected static final String ARG_SHOULD_FINISH_ACTIVITY_ON_DISMISS = "shouldFinishActivityOnDismiss";

	protected static Bundle createArguments(String title, String message, boolean cancelable,
			boolean shouldFinishActivityOnDismiss) {
		Bundle args = new Bundle();
		args.putString(ARG_TITLE, title);
		args.putString(ARG_MESSAGE, message);
		args.putBoolean(ARG_CANCELABLE, cancelable);
		args.putBoolean(ARG_SHOULD_FINISH_ACTIVITY_ON_DISMISS, shouldFinishActivityOnDismiss);
		return args;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		if (getArguments().getBoolean(ARG_SHOULD_FINISH_ACTIVITY_ON_DISMISS, false)) {
			getActivity().finish();
		}
	}
}