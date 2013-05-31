package com.easyandroid.autotest.testactivities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.easyandroid.autotest.R;
import com.easyandroid.autotest.docalltest.AutoCallTestActivity;
import com.easyandroid.autotest.receivecalltest.AutoCallReceiveTestActivity;

public class CallTestActivity extends Activity {

	private Button btnDoCall;
	private Button btnDoAnswer;
	private Button btnGoback;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calltest);
		setTitle(R.string.test_phone_call);

		btnDoCall = (Button) findViewById(R.id.btn_calltest_docall);
		btnDoAnswer = (Button) findViewById(R.id.btn_calltest_answer);
		btnGoback = (Button) findViewById(R.id.btn_calltest_return);
		btnDoCall.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(CallTestActivity.this, AutoCallTestActivity.class));
				finish();
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});
		btnDoAnswer.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(CallTestActivity.this, AutoCallReceiveTestActivity.class));
				finish();
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});
		btnGoback.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
			}
		});
	}

}
