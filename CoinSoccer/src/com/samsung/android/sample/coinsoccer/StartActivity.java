package com.samsung.android.sample.coinsoccer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.samsung.android.sample.coinsoccer.settings.JoinGameActivity;
import com.samsung.android.sample.coinsoccer.settings.NewGameActivity;

public class StartActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_activity);
		findViewById(R.id.new_game_button).setOnClickListener(this);
		findViewById(R.id.join_game_button).setOnClickListener(this);
		findViewById(R.id.about_button).setOnClickListener(this);
		findViewById(R.id.help_button).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.new_game_button:
				startActivity(new Intent(this, NewGameActivity.class));
				break;
			case R.id.join_game_button:
				startActivity(new Intent(this, JoinGameActivity.class));
				break;
			case R.id.about_button:
				startActivity(new Intent(this, AboutActivity.class));
				break;
			case R.id.help_button:
				startActivity(new Intent(this, HelpActivity.class));
				break;
		}
	}
}
