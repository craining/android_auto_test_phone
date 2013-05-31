package com.easyandroid.autotest.testactivities;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
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

import com.easyandroid.autotest.R;
import com.easyandroid.autotest.Utils;

public class WifiTestActivity extends Activity {

	private WifiManager wifiManager;
	private final static int MSG_LISTEN_WIFI_STATE = 22;
	private final static int MSG_CHANGE_WIFI_STATE = 23;
	private final static int MSG_WIFI_TEST_END = 24;
	private boolean testOn = false;
	private int testCount = 0;
	private int testMax = 0;
	private Handler testHandler;

	private EditText editTestTimes;

	private Button btnStart;
	private Button btnStop;
	private Button btnGoBack;

	private TextView textOutput;
	private TextView textShowLeftTimes;

	private String strOut = "";

	private EditText editWaitTime;
	private int waitTime = Utils.WAIT_TIME_WIFI;
	private TextView textShowState;
	private PowerManager pm;
	private WakeLock wakeLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wifi);
		setTitle(R.string.test_wifi);
		editTestTimes = (EditText) findViewById(R.id.edit_wifi);
		btnStart = (Button) findViewById(R.id.btn_wifi);
		btnStop = (Button) findViewById(R.id.btn_wifi_stop);
		btnGoBack = (Button) findViewById(R.id.btn_wifi_return);
		textOutput = (TextView) findViewById(R.id.test_wifi_errorshow);
		editWaitTime = (EditText) findViewById(R.id.edit_wifi_waite);
		textShowState = (TextView) findViewById(R.id.text_show_wifistate);
		textShowLeftTimes = (TextView) findViewById(R.id.text_wifi_leftTimes);

		// add a lock, to avoid system sleeping
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getCanonicalName());
		wakeLock.acquire();

		if ((Utils.STORE_WIFI_TEST_TIMES_PATH).exists()) {
			editTestTimes.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_WIFI_TEST_TIMES_FILENAME));
		}
		if ((Utils.STORE_WIFI_WAIT_TIME_PATH).exists()) {
			editWaitTime.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_WIFI_WAIT_TIME_FILENAME));
		}
		wifiManager = (WifiManager) WifiTestActivity.this.getSystemService(Context.WIFI_SERVICE);
		testHandler = new myHandler();
		listenState();
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
				testOn = false;
				testHandler.sendEmptyMessage(MSG_WIFI_TEST_END);
			}
		});
		btnGoBack.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				goBack();
			}
		});

	}

	private void startTest() {
		try {
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); 
		} catch (Exception e) {
			// Log.e(Utils.TEST_WIFI_TAG, "try to hide keyboard");
		}
		testMax = 0;
		String strTestTimes = editTestTimes.getText().toString();
		String strWaite = editWaitTime.getText().toString();
		if (!TextUtils.isEmpty(strTestTimes)) {
			Utils.androidFileSave(getBaseContext(), Utils.STORE_WIFI_TEST_TIMES_FILENAME, strTestTimes);
			testMax = Integer.parseInt(strTestTimes);
		} else {
			testMax = Utils.TEST_TIMES_WIFI;
		}

		if (!TextUtils.isEmpty(strWaite)) {
			waitTime = Integer.parseInt(strWaite);
			Utils.androidFileSave(getBaseContext(), Utils.STORE_WIFI_WAIT_TIME_FILENAME, strWaite);
		} else {
			editWaitTime.setText(waitTime + "");
		}
		editTestTimes.setText(testMax + "");
		editWaitTime.setEnabled(false);
		editTestTimes.setEnabled(false);
		btnStart.setEnabled(false);
		testOn = true;
		strOut = "++++++++++wifi test start!";
		Utils.logOut(Utils.TEST_WIFI_TAG, strOut);
		Thread testThread = new testThread();
		Thread listenThread = new listenThread();
		testThread.start();
		listenThread.start();
		textOutput.setText(strOut);
		textShowLeftTimes.setText("Left times:  " + testMax);
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
		wakeLock.release();
		testOn = false;
		String thisOut = "+++++++++++wifi test Activity Destroyed!";
		strOut = strOut + "\n" + thisOut;
		Utils.logOut(Utils.TEST_WIFI_TAG, thisOut);
		super.onDestroy();
	}

	class testThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				while (true) {
					if (testOn) {
						if (testCount < testMax) {
							testHandler.sendEmptyMessage(MSG_CHANGE_WIFI_STATE);
						} else {
							testOn = false;
							testHandler.sendEmptyMessage(MSG_WIFI_TEST_END);
						}
						sleep(waitTime * 1000);
					}
					sleep(500);
				}
			} catch (Exception e) {
				Log.e(Utils.TEST_WIFI_TAG, "wifi test  Thread Error!");
				e.printStackTrace();
			}
		}
	}

	class listenThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				do {
					testHandler.sendEmptyMessage(MSG_LISTEN_WIFI_STATE);
					sleep(Utils.LISTENTIME);
				} while (testOn);
			} catch (Exception e) {
				Log.e(Utils.TEST_WIFI_TAG, "wifi test  Thread Error!");
				e.printStackTrace();
			}
		}
	}

	private class myHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LISTEN_WIFI_STATE: {
				listenState();
				// textOutput.setText(strOut);
				break;
			}
			case MSG_CHANGE_WIFI_STATE: {
				String thisOut = "";
				String left = "Left times:  " + (int) (testMax - testCount);
				if (listenState() == 1) {
					int m = testCount + 1;
					textShowLeftTimes.setText(left);
					try {
						wifiManager.setWifiEnabled(true);
					} catch (Exception e) {
						thisOut = "+++++++++++Wifi Enable ERROR!" + "\n" + e.toString();
						strOut = strOut + "\n" + thisOut;
						Utils.logOut(Utils.TEST_WIFI_TAG, thisOut);
					}
				} else if (listenState() == 3) {
					testCount++;
					try {
						wifiManager.setWifiEnabled(false);
					} catch (Exception e) {
						thisOut = "+++++++++++Wifi Disable ERROR!" + "\n" + e.toString();
						strOut = strOut + "\n" + thisOut;
						Utils.logOut(Utils.TEST_WIFI_TAG, thisOut);
					}

				}
				textOutput.setText(strOut);
				break;
			}
			case MSG_WIFI_TEST_END: {
				editTestTimes.setEnabled(true);
				btnStart.setEnabled(true);
				editWaitTime.setEnabled(true);
				testCount = 0;
				String thisOut = "+++++++++++wifi test finish!";
				strOut = strOut + "\n" + thisOut;
				Utils.logOut(Utils.TEST_WIFI_TAG, thisOut);
				textOutput.setText(strOut);
				textShowLeftTimes.setText("Finish!");
				textShowState.setText("");
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

	private int listenState() {

		int state = 0;
		String thisOut = "";

		switch (wifiManager.getWifiState()) {
		case WifiManager.WIFI_STATE_DISABLED: {
			thisOut = "wifi disabled!";
			textShowState.setText(thisOut);
			state = 1;
			break;
		}

		case WifiManager.WIFI_STATE_DISABLING: {
			thisOut = "wifi disabling...";
			textShowState.setText(thisOut);
			state = 2;
			break;
		}

		case WifiManager.WIFI_STATE_ENABLED: {
			thisOut = "wifi enabled!";
			textShowState.setText(thisOut);
			state = 3;
			break;
		}

		case WifiManager.WIFI_STATE_ENABLING: {
			thisOut = "wifi enabling...";
			textShowState.setText(thisOut);
			state = 4;
			break;
		}

		case WifiManager.WIFI_STATE_UNKNOWN: {
			thisOut = "wifi unknown state!!";
			textShowState.setText(thisOut);
			state = 5;
			break;
		}

		default:
			break;
		}
		return state;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			goBack();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

}
