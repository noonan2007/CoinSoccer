package com.samsung.android.sample.coinsoccer.statistics;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.samsung.android.sample.coinsoccer.R;
import com.samsung.android.sample.coinsoccer.colorpicker.ColorPickerView;
import com.samsung.android.sample.coinsoccer.settings.PlayerSettings;
import com.samsung.android.sample.coinsoccer.settings.Which;

public class StatisticsSummaryView extends LinearLayout {

	private static class LineHolder {

		private final TextView mFirstValue;
		private final TextView mSecondValue;

		LineHolder(View v) {
			mFirstValue = (TextView) v.findViewById(R.id.entry_left_value);
			mSecondValue = (TextView) v.findViewById(R.id.entry_right_value);
		}

		public void setValue(Which which, float value) {
			setValue(which, String.valueOf(value));
		}

		public void setValue(Which which, int value) {
			setValue(which, String.valueOf(value));
		}

		public void setValue(Which which, CharSequence value) {
			getValueView(which).setText(value);
		}

		private TextView getValueView(Which which) {
			switch (which) {
				case FIRST:
					return mFirstValue;
				case SECOND:
					return mSecondValue;
			}
			throw new ArrayIndexOutOfBoundsException();
		}
	}

	private static class LineWithColorBoxesHolder extends LineHolder {

		private final View mFirstColorBox;
		private final View mSecondColorBox;

		public LineWithColorBoxesHolder(View v) {
			super(v);
			mFirstColorBox = v.findViewById(R.id.entry_left_color);
			mSecondColorBox = v.findViewById(R.id.entry_right_color);
		}

		public void setColor(Which which, int color) {
			ColorPickerView.updateDisplayedColor(getColorBox(which), color);
		}

		private View getColorBox(Which which) {
			switch (which) {
				case FIRST:
					return mFirstColorBox;
				case SECOND:
					return mSecondColorBox;
			}
			throw new ArrayIndexOutOfBoundsException();
		}
	}

	private static class LineWithTitleHolder extends LineHolder {

		private final TextView mTitleView;

		LineWithTitleHolder(View v, CharSequence title) {
			super(v);
			mTitleView = (TextView) v.findViewById(R.id.entry_title);
			setTitle(title);
		}

		public void setTitle(CharSequence title) {
			mTitleView.setText(title);
		}
	}
	
	private static LineWithTitleHolder inflateSummaryLine(ViewGroup root, int titleResId) {
		Context context = root.getContext();
		View v = LayoutInflater.from(context).inflate(
				R.layout.statistics_summary_line, root, false);
		root.addView(v, root.getChildCount());
		return new LineWithTitleHolder(v, context.getString(titleResId));
	}

	public static LineHolder inflateScoreLine(ViewGroup root) {
		View v = LayoutInflater.from(root.getContext()).inflate(
				R.layout.statistics_score_line, root, false);
		root.addView(v);
		return new LineHolder(v);
	}

	public static LineWithColorBoxesHolder inflatePlayerNamesLine(ViewGroup root) {
		View v = LayoutInflater.from(root.getContext()).inflate(
				R.layout.statistics_names_line, root, false);
		root.addView(v);
		return new LineWithColorBoxesHolder(v);
	}

	private final LineWithColorBoxesHolder mNamesLine;
	private final LineHolder mScoreLine;
	private final LineWithTitleHolder mTotalFoulsLine;
	private final LineWithTitleHolder mOffensiveFoulsLine;
	private final LineWithTitleHolder mIllegalPawnsPositionFoulsLine;
	private final LineWithTitleHolder mAvgTurnsPerGoal;
	private final LineWithTitleHolder mMinTurnsPerGoal;
	private final LineWithTitleHolder mMaxTurnsPerGoal;
	private final LineWithTitleHolder mExpiredShotsLine;
	private final LineWithTitleHolder mOwnGoalsLine;

	public StatisticsSummaryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mNamesLine = inflatePlayerNamesLine(this);
		mScoreLine = inflateScoreLine(this);
		mTotalFoulsLine = inflateSummaryLine(this, R.string.stats_summary_total_fouls);
		mOffensiveFoulsLine = inflateSummaryLine(this, R.string.stats_summary_offensive_fouls);
		mIllegalPawnsPositionFoulsLine = inflateSummaryLine(this, R.string.stats_summary_illegal_position_fouls);
		mAvgTurnsPerGoal = inflateSummaryLine(this, R.string.stats_summary_avg_turns_per_goal);
		mMinTurnsPerGoal = inflateSummaryLine(this, R.string.stats_summary_min_turns_per_goal);
		mMaxTurnsPerGoal = inflateSummaryLine(this, R.string.stats_summary_max_turns_per_goal);
		mExpiredShotsLine = inflateSummaryLine(this, R.string.stats_summary_expired_shots);
		mOwnGoalsLine = inflateSummaryLine(this, R.string.stats_summary_own_goals);
	}

	public void setPlayer(PlayerSettings player) {
		mNamesLine.setValue(player.which, player.name);
		mNamesLine.setColor(player.which, player.color);
	}

	public void setGameStatistics(GameStatistics gameStatistics) {
		updatePlayerStatistics(gameStatistics, Which.FIRST);
		updatePlayerStatistics(gameStatistics, Which.SECOND);
	}

	@Override
	protected int getSuggestedMinimumHeight() {
		return 0;
	}

	@Override
	protected int getSuggestedMinimumWidth() {
		return 0;
	}

	private void updatePlayerStatistics(GameStatistics gameStatistics, Which which) {
		PlayerStatistics playerStatistics = gameStatistics.getPlayerStatistics(which);
		mScoreLine.setValue(which, playerStatistics.getScore());
		mOffensiveFoulsLine.setValue(which, playerStatistics.getOffensiveFoulsCount());
		mTotalFoulsLine.setValue(which, playerStatistics.getTotalFoulsCount());
		mIllegalPawnsPositionFoulsLine.setValue(which, playerStatistics.getIllegalPawnsPositionFoulsCount());
		mAvgTurnsPerGoal.setValue(which, playerStatistics.getAvgTurnsPerGoal());
		mMinTurnsPerGoal.setValue(which, playerStatistics.getMinTurnsPerGoal());
		mMaxTurnsPerGoal.setValue(which, playerStatistics.getMaxTurnsPerGoal());
		mExpiredShotsLine.setValue(which, playerStatistics.getExpiredShotsCount());
		mOwnGoalsLine.setValue(which, playerStatistics.getOwnGoals());
	}
}