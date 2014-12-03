package com.samsung.android.sample.coinsoccer;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scrollable_text);
		WindowSizeHelper.adjustWindowSize(this);
		TextView v = (TextView) findViewById(R.id.text_target);
		v.setText(Html.fromHtml(getString(R.string.about_text)));
	}
}
