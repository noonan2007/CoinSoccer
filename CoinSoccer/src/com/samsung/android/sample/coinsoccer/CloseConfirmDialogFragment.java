package com.samsung.android.sample.coinsoccer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Dialog fragment wrapping back press confirm dialog
 */
public final class CloseConfirmDialogFragment extends DialogFragment implements
		DialogInterface.OnClickListener {

	public static final String TAG = CloseConfirmDialogFragment.class.getName();

	public static void show(FragmentManager fm) {
		new CloseConfirmDialogFragment().show(fm, TAG);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.confirm_exit_title)
				.setMessage(R.string.confirm_exit_message)
				.setPositiveButton(android.R.string.ok, this)
				.setNegativeButton(android.R.string.no, null).create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		((GameActivity) getActivity()).forceGameEnd();
	}
}