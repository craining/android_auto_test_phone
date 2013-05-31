package com.easyandroid.autotest.testactivities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.easyandroid.autotest.R;
import com.easyandroid.autotest.Utils;

/**
 * 
 * @author Administrator
 * 
 */
public class ScreenTestActivity extends Activity {

	private final static int MSG_SCREEN_TEST_END = 16;

	private Button btnStart;
	private Button btnStop;
	private Button btnGoBack;

	private TextView textOutput;

	private int testMax = 0;
	private EditText editTestTimes;
	private EditText editWaitTime;
	private int waitTime = Utils.WAIT_TIME_SCREEN;

	private String strOut = "";

	private PowerManager powerManager;
	private WakeLock wakeLock;

	private WakeLock mScreenonWakeLock = null;

	private static final String CONNECTIVITY_TEST_SCREEN = "com.easyandroid.autotest.CONNECTIVITY_TEST_ALARM";
	private static final String TEST_ALARM_EXTRA = "CONNECTIVITY_TEST_EXTRA";
	private static final String TEST_ALARM_ON_EXTRA = "CONNECTIVITY_TEST_ON_EXTRA";
	private static final String TEST_ALARM_OFF_EXTRA = "CONNECTIVITY_TEST_OFF_EXTRA";
	private static final String TEST_ALARM_CYCLE_EXTRA = "CONNECTIVITY_TEST_CYCLE_EXTRA";
	private static final String SCREEN_ON = "SCREEN_ON";
	private static final String SCREEN_OFF = "SCREEN_OFF";

	private long mSCOnDuration = 2000;
	private long mSCOffDuration = 2000;
	private int mSCCycleCount = 0;
	private Handler testHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen);
		setTitle(R.string.test_screen);

		editTestTimes = (EditText) findViewById(R.id.edit_screen);
		btnStart = (Button) findViewById(R.id.btn_screen);
		btnStop = (Button) findViewById(R.id.btn_screen_stop);
		btnGoBack = (Button) findViewById(R.id.btn_screen_return);
		editWaitTime = (EditText) findViewById(R.id.edit_screen_waite);
		textOutput = (TextView) findViewById(R.id.test_screen);

		testHandler = new myHandler();

		powerManager = (PowerManager) ScreenTestActivity.this.getSystemService(Context.POWER_SERVICE);

		if ((Utils.STORE_SCREEN_TEST_TIMES_PATH).exists()) {
			editTestTimes.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_SCREEN_TEST_TIMES_FILENAME));
		}
		if ((Utils.STORE_SCREEN_WAIT_TIME_PATH).exists()) {
			editWaitTime.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_SCREEN_WAIT_TIME_FILENAME));
		}

		if (Utils.autoTesting) {
			// if is doing the test of many items
			btnStop.setText(R.string.autotest_stop_one);
			btnGoBack.setText(R.string.autotest_stop_all);
			startTest();
		}

		btnStart.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startTest();
			}
		});

		btnStop.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				testMax = 0;
				testHandler.sendEmptyMessage(MSG_SCREEN_TEST_END);
			}
		});
		btnGoBack.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				goBack();
			}
		});

		registerReceiver(mReceiver, new IntentFilter(CONNECTIVITY_TEST_SCREEN));
	}

	private void goBack() {
		if (Utils.autoTesting) {
			// test canceled
			setResult(RESULT_CANCELED);
		}
		finish();
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
	}

	@Override
	protected void onDestroy() {
		// testOn = false;
		String thisOut = "+++++++++++Screen test Activity Destroyed!";
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}
		unregisterReceiver(mReceiver);
		Utils.logOut(Utils.TEST_SCREEN_TAG, thisOut);

		super.onDestroy();
	}

	public BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(CONNECTIVITY_TEST_SCREEN)) {
				String extra = (String) intent.getStringExtra(TEST_ALARM_EXTRA);
				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				Long on = new Long(2000);
				Long off = new Long(2000);
				int cycle = 0;
				try {
					on = Long.parseLong((String) intent.getStringExtra(TEST_ALARM_ON_EXTRA));
					off = Long.parseLong((String) intent.getStringExtra(TEST_ALARM_OFF_EXTRA));
					cycle = Integer.parseInt((String) intent.getStringExtra(TEST_ALARM_CYCLE_EXTRA));
					Log.e("screentest", "on: " + on + "  off: " + off + "cycle: " + cycle);
				} catch (Exception e) {
					Log.e("screentest", "get timer error!");
				}

				if (extra.equals(SCREEN_ON)) {
					Log.e("screentest", "turn on screen ");
					mScreenonWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ScreenTest2");
					mScreenonWakeLock.acquire();

					mSCCycleCount = cycle + 1;
					mSCOnDuration = on;
					mSCOffDuration = off;
					Log.e("screentest", "times: " + mSCCycleCount);
					if (mSCCycleCount < testMax) {
						scheduleAlarm(mSCOnDuration, SCREEN_OFF);
					} else {
						testHandler.sendEmptyMessage(MSG_SCREEN_TEST_END);
					}

				} else if (extra.equals(SCREEN_OFF)) {
					mSCCycleCount = cycle;
					mSCOnDuration = on;
					mSCOffDuration = off;

					mScreenonWakeLock.release();
					mScreenonWakeLock = null;
					if (mSCCycleCount < testMax) {
						scheduleAlarm(mSCOffDuration, SCREEN_ON);
						pm.goToSleep(SystemClock.uptimeMillis());
						Log.e("screen", "turn off screen");
					}

				}
			}
		}
	};

	private void scheduleAlarm(long delayMs, String eventType) {
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(CONNECTIVITY_TEST_SCREEN);

		i.putExtra(TEST_ALARM_EXTRA, eventType);
		i.putExtra(TEST_ALARM_ON_EXTRA, Long.toString(mSCOnDuration));
		i.putExtra(TEST_ALARM_OFF_EXTRA, Long.toString(mSCOffDuration));
		i.putExtra(TEST_ALARM_CYCLE_EXTRA, Integer.toString(mSCCycleCount));

		PendingIntent p = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delayMs, p);
	}

	private class myHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MSG_SCREEN_TEST_END: {
				editTestTimes.setEnabled(true);
				btnStart.setEnabled(true);
				editWaitTime.setEnabled(true);
				String thisOut = "+++++++++++Screen test finish!";
				strOut = strOut + "\n" + thisOut;
				Utils.logOut(Utils.TEST_SCREEN_TAG, thisOut);
				textOutput.setText(strOut);
				if (Utils.autoTesting) {
					// test finish
					setResult(RESULT_OK);
					finish();
				}
				break;
			}
			default:
				break;
			}
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			goBack();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void startTest() {
		try {
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); 
		} catch (Exception e) {
//			Log.e(Utils.TEST_SCREEN_TAG, "try to hide keyboard");
		}
		testMax = 0;
		String strTestTimes = editTestTimes.getText().toString();
		String strWait = editWaitTime.getText().toString();
		if (!TextUtils.isEmpty(strTestTimes)) {
			Utils.androidFileSave(getBaseContext(), Utils.STORE_SCREEN_TEST_TIMES_FILENAME, strTestTimes);
			testMax = Integer.parseInt(strTestTimes);
		} else {
			testMax =  Utils.TEST_TIMES_SCREEN;
		}
		if (!TextUtils.isEmpty(strWait)) {
			waitTime = Integer.parseInt(strWait);
			Utils.androidFileSave(getBaseContext(), Utils.STORE_SCREEN_WAIT_TIME_FILENAME, strWait);
		} else {
			editWaitTime.setText("" + waitTime);
		}
		editTestTimes.setText(testMax + "");
		editTestTimes.setEnabled(false);
		btnStart.setEnabled(false);
		editWaitTime.setEnabled(false);
		// testOn = true;
		strOut = "++++++++++Screen test start!";
		Utils.logOut(Utils.TEST_SCREEN_TAG, strOut);
		textOutput.setText(strOut);
		mSCOnDuration = waitTime * 1000;
		mSCOffDuration = waitTime * 1000;
		mSCCycleCount = 0;
		
		mScreenonWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "ScreenTest1");
		mScreenonWakeLock.acquire();

		scheduleAlarm(10, SCREEN_OFF);// turn off screen right now

	}

}
