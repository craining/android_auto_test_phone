package com.easyandroid.autotest;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.easyandroid.autotest.R;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about);
		setTitle(R.string.text_aboout_title);
		
	}

}
