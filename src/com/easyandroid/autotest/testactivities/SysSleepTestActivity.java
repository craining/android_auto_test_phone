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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.easyandroid.autotest.R;
import com.easyandroid.autotest.Utils;

public class SysSleepTestActivity extends Activity {

	private EditText editTestTimes;

	private final static int MSG_SYS_GOTOSLEEP_TEST_END = 21;

	private Button btnStart;
	private Button btnStop;
	private Button btnGoBack;

	private TextView textOutput;

	private int testMax = 0;

	private EditText editGoToSleepTime;
	private EditText editSleepTime;
	private int sleepTime = Utils.WAIT_TIME_SLEEP_ALARM_TIME;
	private int goToSleepTime = Utils.WAIT_TIME_SLEEP_GOTOSLEEP_TIME;

	private String strOut = "";

	private PowerManager pm;
	private WakeLock wakeLock;

	private static final String CONNECTIVITY_TEST_SLEEP = "com.easyandroid.autotest.sleep.CONNECTIVITY_TEST_ALARM";
	private static final String TEST_ALARM_EXTRA = "CONNECTIVITY_TEST_EXTRA";
	private static final String TEST_GOTO_SLEEP_EXTRA = "CONNECTIVITY_TEST_ON_EXTRA";
	private static final String TEST_ALARM_TIME_EXTRA = "CONNECTIVITY_TEST_OFF_EXTRA";
	private static final String TEST_ALARM_CYCLE_EXTRA = "CONNECTIVITY_TEST_CYCLE_EXTRA";
	private static final String SCREEN_ON = "SCREEN_ON";
	private static final String SCREEN_OFF = "SCREEN_OFF";

	private long mGotoSleepDuration = 2000;
	private long mAlarmDuration = 2000;
	private int mSCCycleCount = 0;

	private Handler testHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_sleep);
		setTitle(R.string.test_syssleep);

		editTestTimes = (EditText) findViewById(R.id.edit_syssleep);
		btnStart = (Button) findViewById(R.id.btn_syssleep);
		btnStop = (Button) findViewById(R.id.btn_syssleep_stop);
		btnGoBack = (Button) findViewById(R.id.btn_syssleep_return);
		editGoToSleepTime = (EditText) findViewById(R.id.edit_syssleep_waite);
		editSleepTime = (EditText) findViewById(R.id.edit_syssleep_on);
		textOutput = (TextView) findViewById(R.id.test_syssleep);

		testHandler = new myHandler();

		pm = (PowerManager) SysSleepTestActivity.this.getSystemService(Context.POWER_SERVICE);

		if ((Utils.STORE_SLEEP_TEST_TIMES_PATH).exists()) {
			editTestTimes.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_SLEEP_TEST_TIMES_FILENAME));
		}
		if ((Utils.STORE_GOTOSLEEP_TIME_PATH).exists()) {
			editGoToSleepTime.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_GOTOSLEEP_TIME_FILENAME));
		}
		if ((Utils.STORE_ALARM_TIME_PATH).exists()) {
			editSleepTime.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_ALARM_TIME_FILENAME));
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
				testHandler.sendEmptyMessage(MSG_SYS_GOTOSLEEP_TEST_END);
			}
		});
		btnGoBack.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				goBack();
			}
		});
		registerReceiver(mReceiver, new IntentFilter(CONNECTIVITY_TEST_SLEEP));
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
		Utils.logOut(Utils.TEST_SYSSLEEP_TAG, "+++++++++++System sleep test Activity Destroyed!");
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	public BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(CONNECTIVITY_TEST_SLEEP)) {
				String extra = (String) intent.getStringExtra(TEST_ALARM_EXTRA);
				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				Long gotosleepTime = new Long(2000);
				Long alarmTime = new Long(10000);
				int cycle = 0;
				try {
					gotosleepTime = Long.parseLong((String) intent.getStringExtra(TEST_GOTO_SLEEP_EXTRA));
					alarmTime = Long.parseLong((String) intent.getStringExtra(TEST_ALARM_TIME_EXTRA));
					cycle = Integer.parseInt((String) intent.getStringExtra(TEST_ALARM_CYCLE_EXTRA));
					Utils.logOut(Utils.TEST_SYSSLEEP_TAG, "gotosleepTime: " + gotosleepTime + "  alarmTime:" + alarmTime);
				} catch (Exception e) {
					Utils.logOut(Utils.TEST_SYSSLEEP_TAG, "get time error!");
				}

				if (extra.equals(SCREEN_ON)) {
					Utils.logOut(Utils.TEST_SYSSLEEP_TAG, "WAKE UP!");
					wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ScreenTest2");
					wakeLock.acquire();

					mSCCycleCount = cycle + 1;
					mGotoSleepDuration = gotosleepTime;
					mAlarmDuration = alarmTime;
					Utils.logOut(Utils.TEST_SYSSLEEP_TAG, "times: " + mSCCycleCount);
					if (mSCCycleCount < testMax) {
						scheduleAlarm(mGotoSleepDuration, SCREEN_OFF);
					} else {
						testHandler.sendEmptyMessage(MSG_SYS_GOTOSLEEP_TEST_END);
					}

				} else if (extra.equals(SCREEN_OFF)) {
					mSCCycleCount = cycle;
					mGotoSleepDuration = gotosleepTime;
					mAlarmDuration = alarmTime;

					wakeLock.release();
					wakeLock = null;
					if (mSCCycleCount < testMax) {
						scheduleAlarm(mAlarmDuration, SCREEN_ON);
						pm.goToSleep(SystemClock.uptimeMillis());
						Utils.logOut(Utils.TEST_SYSSLEEP_TAG, "Do goto sleep opera!");
					}

				}
			}
		}
	};

	private void scheduleAlarm(long delayMs, String eventType) {
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(CONNECTIVITY_TEST_SLEEP);

		i.putExtra(TEST_ALARM_EXTRA, eventType);
		i.putExtra(TEST_GOTO_SLEEP_EXTRA, Long.toString(mGotoSleepDuration));
		i.putExtra(TEST_ALARM_TIME_EXTRA, Long.toString(mAlarmDuration));
		i.putExtra(TEST_ALARM_CYCLE_EXTRA, Integer.toString(mSCCycleCount));

		PendingIntent p = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayMs, p);
	}

	private class myHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MSG_SYS_GOTOSLEEP_TEST_END: {
				Utils.logOut(Utils.TEST_SYSSLEEP_TAG, "+++++++++++System sleep test finish!");
				btnStart.setEnabled(true);
				editTestTimes.setEnabled(true);
				editGoToSleepTime.setEnabled(true);
				editSleepTime.setEnabled(true);
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
//			Log.e(Utils.TEST_SYSSLEEP_TAG, "try to hide keyboard");
		}
		testMax = 0;
		String strTestTimes = editTestTimes.getText().toString();
		String strGoToSleepTime = editGoToSleepTime.getText().toString();
		String strAlarmTime = editSleepTime.getText().toString();
		if (!TextUtils.isEmpty(strTestTimes)) {
			Utils.androidFileSave(getBaseContext(), Utils.STORE_SLEEP_TEST_TIMES_FILENAME, strTestTimes);
			testMax = Integer.parseInt(strTestTimes);
		} else {
			testMax = Utils.TEST_TIMES_SYSTEM_SLEEP;
		}
		if (!TextUtils.isEmpty(strGoToSleepTime)) {
			goToSleepTime = Integer.parseInt(strGoToSleepTime);
			Utils.androidFileSave(getBaseContext(), Utils.STORE_GOTOSLEEP_TIME_FILENAME, goToSleepTime + "");
		} else {
			editGoToSleepTime.setText("" + goToSleepTime);
		}
		if (!TextUtils.isEmpty(strAlarmTime)) {
			sleepTime = Integer.parseInt(strAlarmTime);
			Utils.androidFileSave(getBaseContext(), Utils.STORE_ALARM_TIME_FILENAME, sleepTime + "");
		} else {
			editSleepTime.setText("" + sleepTime);
		}

		strOut = "++++++++++System Sleep test start!";
		Utils.logOut(Utils.TEST_SYSSLEEP_TAG, strOut);

		mGotoSleepDuration = goToSleepTime * 1000;
		mAlarmDuration = sleepTime * 1000;

		mSCCycleCount = 0;

		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "SleepTest");
		wakeLock.acquire();

		scheduleAlarm(mGotoSleepDuration, SCREEN_OFF);// go to sleep right now
		editTestTimes.setText(testMax + "");
		btnStart.setEnabled(false);
		editTestTimes.setEnabled(false);
		editGoToSleepTime.setEnabled(false);
		editSleepTime.setEnabled(false);

	}

}