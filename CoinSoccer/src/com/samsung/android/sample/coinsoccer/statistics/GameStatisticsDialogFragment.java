package com.samsung.android.sample.coinsoccer.statistics;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.samsung.android.sample.coinsoccer.CoinSoccerApp;
import com.samsung.android.sample.coinsoccer.R;

public class GameStatisticsDialogFragment extends DialogFragment {

	private static final String TAG = GameStatisticsDialogFragment.class.getName();

	public static void show(FragmentManager fm, Bundle args) {
		dismissIfExists(fm);
		GameStatisticsDialogFragment f = new GameStatisticsDialogFragment();
		f.setArguments(args);
		f.show(fm, TAG);
	}

	public static void dismissIfExists(FragmentManager fm) {
		GameStatisticsDialogFragment f = (GameStatisticsDialogFragment) fm.findFragmentByTag(TAG);
		if (f != null && f.isAdded()) {
			f.dismiss();
		}
	}

	private StatisticsSummaryView mStatisticsSummaryView;
	private GameStatistics mGameStatistics;

	public GameStatisticsDialogFragment() {
		setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mGameStatistics = GameStatistics.fromBundle(getArguments());
		Dialog d = super.onCreateDialog(savedInstanceState);
		d.setCanceledOnTouchOutside(false);
		d.setTitle(mGameStatistics.isGameEnd() ?
				R.string.stats_title_end_of_game : R.string.stats_title_in_game);
		return d;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.game_statistics_fragment, container, false);
		mStatisticsSummaryView = (StatisticsSummaryView) v.findViewById(
				R.id.statistics_summary_view);
		updateStatistics();
		return v;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if (mGameStatistics.mGameEnd) {
			getActivity().finish();
		}
		else {
			super.onCancel(dialog);
		}
	}

	private void updateStatistics() {
		CoinSoccerApp app = CoinSoccerApp.get(getActivity());
		mStatisticsSummaryView.setPlayer(app.getFirstPlayerSettings());
		mStatisticsSummaryView.setPlayer(app.getSecondPlayerSettings());
		mStatisticsSummaryView.setGameStatistics(mGameStatistics);
	}
}
