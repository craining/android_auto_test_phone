package com.easyandroid.autotest.testactivities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.easyandroid.autotest.AutoTestActivity;
import com.easyandroid.autotest.R;
import com.easyandroid.autotest.Utils;

public class RebootTestActivity extends Activity {

	private EditText editTestTimes;
	private EditText editWaitTime;
	private Button btnStart;
	private Button btnStop;
	private Button btnGoBack;
	private TextView textShowLeftTimes;

	private int waitTime = Utils.WAIT_TIME_REBOOT;

	private final static int MSG_REBOOT_NOW = 13;

	private Handler testHandler;
	private boolean testOn = false;
	
	private PowerManager pm;
	private WakeLock wakeLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reboot);
		setTitle(R.string.test_reboot);
		textShowLeftTimes = (TextView) findViewById(R.id.text_rebootlefttimes);
		editTestTimes = (EditText) findViewById(R.id.edit_reboot);
		editWaitTime = (EditText) findViewById(R.id.edit_reboot_waite);
		btnStart = (Button) findViewById(R.id.btn_reboot_start);
		btnStop = (Button) findViewById(R.id.btn_reboot_stop);
		btnGoBack = (Button) findViewById(R.id.btn_reboot_return);

		//add a lock, to avoid system sleeping
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getCanonicalName());
		wakeLock.acquire();
		
		testHandler = new myHandler();
		testOn = false;

		if ((Utils.STORE_REBOOT_WAIT_TIME_PATH).exists()) {
			// get saved wait time
			editWaitTime.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_REBOOT_WAIT_TIME_FILENAME));
			String getWait = editWaitTime.getText().toString();
			waitTime = Integer.parseInt(getWait);
		}
		if ((Utils.STORE_REBOOT_TEST_TIMES_PATH).exists()) {
			// get saved test times
			editTestTimes.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_REBOOT_TEST_TIMES_FILENAME));
		}

		if ((Utils.STORE_REBOOT_LEFT_TIMES_PATH).exists()) {
			// if is doing the reboot test 
			int leftTimes = Integer.parseInt(Utils.androidFileload(getBaseContext(), Utils.STORE_REBOOT_LEFT_TIMES_FILENAME));
			leftTimes--;
			if (leftTimes > 0) {
				textShowLeftTimes.setText("Left: " + leftTimes);
				Utils.androidFileSave(getBaseContext(), Utils.STORE_REBOOT_LEFT_TIMES_FILENAME, "" + leftTimes);
				testOn = true;
				testHandler.sendEmptyMessageDelayed(MSG_REBOOT_NOW, waitTime * 1000);
				editTestTimes.setEnabled(false);
				editWaitTime.setEnabled(false);
				btnStart.setEnabled(false);
			} else {
				testOn = false;
				(Utils.STORE_REBOOT_LEFT_TIMES_PATH).delete();
				// if is doing the test of many items, and then should do the clear test, start it
//				if ((Utils.STORE_CLEAR_AUTOTEST_TIMES_PATH).exists()) {
//					(Utils.STORE_CLEAR_AUTOTEST_TIMES_PATH).delete();
//					Utils.autoTesting = true;
//					startActivity(new Intent(RebootTestActivity.this, ClearDataTestActivity.class));
//					finish();
//				} else {
					goBack();
//				}
			}
		} else {
			testOn = false;
		}

		if (Utils.autoTesting) {
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
				testOn = false;
				if ((Utils.STORE_REBOOT_LEFT_TIMES_PATH).exists()) {
					(Utils.STORE_REBOOT_LEFT_TIMES_PATH).delete();
				}

				editTestTimes.setEnabled(true);
				editWaitTime.setEnabled(true);
				btnStart.setEnabled(true);
				textShowLeftTimes.setText("");
				if (Utils.autoTesting) {
					setResult(RESULT_OK);
					finish();
				}
			}
		});

		btnGoBack.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				goBack();
			}
		});

	}

	private void goBack() {

		if (Utils.autoTesting) {
			setResult(RESULT_CANCELED);
		} else {
			startActivity(new Intent(RebootTestActivity.this, AutoTestActivity.class));
		}
		finish();
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		testOn = false;
		if ((Utils.STORE_REBOOT_LEFT_TIMES_PATH).exists()) {
			(Utils.STORE_REBOOT_LEFT_TIMES_PATH).delete();
		}
		wakeLock.release();
		super.onDestroy();
	}

	private class myHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REBOOT_NOW: {
				// Reboot now

				if (testOn && (Utils.STORE_REBOOT_LEFT_TIMES_PATH).exists()) {
					try {
						Log.e(Utils.TEST_REBOOT_TAG, "REBOOT NOW!");
						Intent intent = new Intent(Intent.ACTION_REBOOT);
						intent.putExtra("nowait", 1);
						intent.putExtra("interval", 1);
						intent.putExtra("window", 0);
						sendBroadcast(intent);
					} catch (Exception e) {
						Log.e(Utils.TEST_REBOOT_TAG, "REBOOT error!\n" + e.toString());
					}
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
			// Log.e(Utils.TEST_REBOOT_TAG, "try to hide keyboard");
		}
		String getTimes = editTestTimes.getText().toString();
		String getWait = editWaitTime.getText().toString();
		if (!TextUtils.isEmpty(getTimes)) {
			Utils.androidFileSave(getBaseContext(), Utils.STORE_REBOOT_LEFT_TIMES_FILENAME, getTimes + "");
			Utils.androidFileSave(getBaseContext(), Utils.STORE_REBOOT_TEST_TIMES_FILENAME, getTimes + "");
		} else {
			Utils.androidFileSave(getBaseContext(), Utils.STORE_REBOOT_LEFT_TIMES_FILENAME, Utils.TEST_TIMES_REBOOT + "");
			Utils.androidFileSave(getBaseContext(), Utils.STORE_REBOOT_TEST_TIMES_FILENAME, Utils.TEST_TIMES_REBOOT + "");
		}
		if (!TextUtils.isEmpty(getWait)) {
			waitTime = Integer.parseInt(getWait);
			Utils.androidFileSave(getBaseContext(), Utils.STORE_REBOOT_WAIT_TIME_FILENAME, waitTime + "");
		}
		editWaitTime.setText("" + waitTime);
		testOn = true;
		testHandler.sendEmptyMessageDelayed(MSG_REBOOT_NOW, waitTime * 1000);
		editTestTimes.setEnabled(false);
		editWaitTime.setEnabled(false);
		btnStart.setEnabled(false);
	}

}
