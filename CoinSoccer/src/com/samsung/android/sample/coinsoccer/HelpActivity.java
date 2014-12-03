package com.samsung.android.sample.coinsoccer;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabWidget;
import android.widget.TextView;

public class HelpActivity extends Activity implements ActionBar.TabListener {
	
	private static abstract class HelpFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.scrollable_text, container, false);
			TextView textView = (TextView) view.findViewById(R.id.text_target);
			textView.setText(Html.fromHtml(getString(getTextResourceId())));
			return view;
		}

		abstract int getTextResourceId();
	}

	public static class HowToShootFragment extends HelpFragment {

		@Override
		int getTextResourceId() {
			return R.string.help_how_to_shoot;
		}
	}

	public static class GameRulesFragment extends HelpFragment {

		@Override
		int getTextResourceId() {
			return R.string.help_game_rules;
		}
	}

	public static class HowToInitGameFragment extends HelpFragment {

		@Override
		int getTextResourceId() {
			return R.string.help_local_remote;
		}
	}

	private static final int[] sTabTitleResIds = new int[] {
			R.string.help_tab_local_remote,
			R.string.help_tab_game_rules,
			R.string.help_tab_how_to_shoot
	};
	TabWidget mTabWidget;

	@SuppressWarnings("unchecked")
	private static final Class<? extends Fragment>[] sTabFragmentClasses = new Class[] {
			HowToInitGameFragment.class,
			GameRulesFragment.class,
			HowToShootFragment.class
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_activity);
		WindowSizeHelper.adjustWindowSize(this);
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		for (int i = 0; i < sTabTitleResIds.length; i++) {
			Tab tab = actionBar.newTab().setText(sTabTitleResIds[i]).setTabListener(this);
			actionBar.addTab(tab);
		}
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		Class<? extends Fragment> c = sTabFragmentClasses[tab.getPosition()];
		Fragment fragment = getFragmentManager().findFragmentByTag(c.getName());
		if(fragment == null) {
			ft.add(R.id.fragment_target, Fragment.instantiate(HelpActivity.this, c.getName()), c.getName());
		}
		else {
			ft.attach(fragment);
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		Class<? extends Fragment> c = sTabFragmentClasses[tab.getPosition()];
		ft.detach(getFragmentManager().findFragmentByTag(c.getName()));
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {}
}
