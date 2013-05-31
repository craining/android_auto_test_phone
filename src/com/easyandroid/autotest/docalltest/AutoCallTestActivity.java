package com.easyandroid.autotest.docalltest;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.easyandroid.autotest.R;
import com.easyandroid.autotest.Utils;

public class AutoCallTestActivity extends Activity {
	/** Called when the activity is first created. */

	private static final String CALL_STATE_FILTER = "android.intent.action.PHONE_STATE";

	private static final int MSG_SHOW_CALL_TIMES = 115;
	private static final int MSG_CALL_TEST_END = 116;
	private TelephonyManager teleManager;
	private Handler callTestHandler;

	private TextView textShowCallTimes;
	private EditText editCallNumber;
	private EditText editCallWaitTime;
	private EditText editCallTimes;
	private Button btnCallStart;
	private Button btnCallStop;
	private Button btnGoback;

	private int testTimes = 10;
	private int testCount = 0;

	private PowerManager pm;
	private WakeLock wakeLock;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.docall);
		textShowCallTimes = (TextView) findViewById(R.id.text_call_leftTimes);
		editCallNumber = (EditText) findViewById(R.id.edit_call_number);
		editCallWaitTime = (EditText) findViewById(R.id.edit_call_waittime);
		editCallTimes = (EditText) findViewById(R.id.edit_call_times);
		btnCallStart = (Button) findViewById(R.id.btn_call_start);
		btnCallStop = (Button) findViewById(R.id.btn_call_stop);
		btnGoback = (Button) findViewById(R.id.btn_call_return);
		btnCallStop.setEnabled(false);
		teleManager = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
		callTestHandler = new myHandler();

		// add a lock, to avoid system sleeping
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getCanonicalName());
		wakeLock.acquire();

		if (Utils.STORE_CALL_NUMBER_PATH.exists()) {
			editCallNumber.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_CALL_NUMBER_FILENAME));
		}
		if (Utils.STORE_CALL_TIMES_PATH.exists()) {
			editCallTimes.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_CALL_TIMES_FILENAME));
		}
		if (Utils.STORE_CALL_WAITTIME_PATH.exists()) {
			editCallWaitTime.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_CALL_WAITTIME_FILENAME));
		}

		btnCallStart.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				String getCallTimes = editCallTimes.getText().toString();
				String getCallNumber = editCallNumber.getText().toString();
				String getCallWaitTime = editCallWaitTime.getText().toString();
				if (!TextUtils.isEmpty(getCallNumber)) {
					Utils.callNumber = getCallNumber;
					Utils.androidFileSave(getBaseContext(), Utils.STORE_CALL_NUMBER_FILENAME, getCallNumber);
					if (!TextUtils.isEmpty(getCallTimes)) {
						testTimes = Integer.parseInt(getCallTimes);
						Utils.androidFileSave(getBaseContext(), Utils.STORE_CALL_TIMES_FILENAME, testTimes + "");
					}
					if (!TextUtils.isEmpty(getCallWaitTime)) {
						Utils.callWaitTime = Integer.parseInt(getCallWaitTime);
						Utils.androidFileSave(getBaseContext(), Utils.STORE_CALL_WAITTIME_FILENAME, getCallWaitTime);
					}
					// do call and register
					Utils.testOn = true;
					testCount = 1;
					registerReceiver();
					startService(new Intent(AutoCallTestActivity.this, CallService.class));
					editCallNumber.setEnabled(false);
					editCallWaitTime.setEnabled(false);
					editCallTimes.setEnabled(false);
					btnCallStart.setEnabled(false);
					btnCallStop.setEnabled(true);
					callTestHandler.sendEmptyMessage(MSG_SHOW_CALL_TIMES);
				} else {
					Toast.makeText(getBaseContext(), R.string.phone_number_error, Toast.LENGTH_SHORT).show();
				}
			}
		});
		btnCallStop.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Utils.testOn = false;
				unregisterReceiver();
				textShowCallTimes.setText("");
				editCallNumber.setEnabled(true);
				editCallWaitTime.setEnabled(true);
				editCallTimes.setEnabled(true);
				btnCallStart.setEnabled(true);
				btnCallStop.setEnabled(false);
			}
		});
		btnGoback.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
			}
		});
	}

	@Override
	protected void onDestroy() {
		wakeLock.release();
		if (Utils.testOn) {
			unregisterReceiver();
			Utils.testOn = false;
		}
		super.onDestroy();
	}

	private void registerReceiver() {
		registerReceiver(CallReceiver, new IntentFilter(CALL_STATE_FILTER));
	}

	private void unregisterReceiver() {
		unregisterReceiver(CallReceiver);
	}

	private class myHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SHOW_CALL_TIMES: {
				textShowCallTimes.setText("Doing call times: " + testCount);
				break;
			}
			case MSG_CALL_TEST_END: {
				editCallNumber.setEnabled(true);
				editCallWaitTime.setEnabled(true);
				editCallTimes.setEnabled(true);
				btnCallStart.setEnabled(true);
				btnCallStop.setEnabled(false);
				textShowCallTimes.setText("Finish!");
				break;
			}
			default:
				break;
			}
		}
	}

	public BroadcastReceiver CallReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Call in
			switch (teleManager.getCallState()) {
			case TelephonyManager.CALL_STATE_RINGING:
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				Log.e(Utils.TEST_CALL_TAG, "off hook");
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				Log.e(Utils.TEST_CALL_TAG, "idle");
				if (Utils.testOn) {
					// do call
					if (testCount < testTimes) {
						// start service to have test
						startService(new Intent(AutoCallTestActivity.this, CallService.class));
						testCount++;
						callTestHandler.sendEmptyMessage(MSG_SHOW_CALL_TIMES);
					} else {
						Utils.testOn = false;
						callTestHandler.sendEmptyMessage(MSG_CALL_TEST_END);
					}

					break;
				}
				break;
			}
		}
	};

}