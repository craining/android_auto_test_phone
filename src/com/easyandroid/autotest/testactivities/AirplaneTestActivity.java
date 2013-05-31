package com.easyandroid.autotest.testactivities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
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

public class AirplaneTestActivity extends Activity {

	private final static int MSG_LISTEN_AIR_STATE = 1;
	private final static int MSG_CHANGE_AIR_STATE = 2;
	private final static int MSG_AIR_TEST_END = 3;
	private boolean testOn = false;
	private int testCount = 0;
	private int testMax = 0;
	private Handler testHandler;

	private EditText editTestTimes;
	private Button btnStart;
	private Button btnStop;
	private Button btnGoBack;
	private TextView textOutput;
	private String strOut = "";

	private ContentResolver cr;

	private EditText editWaitTime;
	private int waitTime = Utils.WAIT_TIME_AIRPLANE;

	private TextView textShowLeftTimes;
	private TextView textShowState;

	private PowerManager pm;
	private WakeLock wakeLock;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.airplane);
		setTitle(R.string.test_airplane);

		editTestTimes = (EditText) findViewById(R.id.edit_airplane);
		btnStart = (Button) findViewById(R.id.btn_airplane);
		btnStop = (Button) findViewById(R.id.btn_airplane_stop);
		btnGoBack = (Button) findViewById(R.id.btn_airplane_return);
		textOutput = (TextView) findViewById(R.id.test_airplane);
		editWaitTime = (EditText) findViewById(R.id.edit_airplane_waite);
		textShowLeftTimes = (TextView) findViewById(R.id.text_airplane_leftTimes);
		textShowState = (TextView) findViewById(R.id.text_show_airplanestate);

		//add a lock, to avoid system sleeping
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getCanonicalName());
		wakeLock.acquire();
		
		cr = getContentResolver();
		testHandler = new myHandler();

		if ((Utils.STORE_AIRPLANE_TEST_TIMES_PATH).exists()) {
			editTestTimes.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_AIRPLANE_TEST_TIMES_FILENAME));
		}
		if ((Utils.STORE_AIRPLANE_WAIT_TIME_PATH).exists()) {
			editWaitTime.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_AIRPLANE_WAIT_TIME_FILENAME));
		}

		if (Utils.autoTesting) {
			// if have a test of many items.
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
				testHandler.sendEmptyMessage(MSG_AIR_TEST_END);
			}
		});
		btnGoBack.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				goBack();
			}
		});
		listenState();
	}

	private void startTest() {
		try {
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		} catch (Exception e) {
			// Log.e(Utils.TEST_AIRPLANE_TAG, "try to hide keyboard");
		}
		testMax = 0;
		String strTestTimes = editTestTimes.getText().toString();
		String strWaite = editWaitTime.getText().toString();
		if (!TextUtils.isEmpty(strTestTimes)) {
			Utils.androidFileSave(getBaseContext(), Utils.STORE_AIRPLANE_TEST_TIMES_FILENAME, strTestTimes);
			testMax = Integer.parseInt(strTestTimes);
		} else {
			testMax = Utils.TEST_TIMES_AIRPLANE;
		}
		if (!TextUtils.isEmpty(strWaite)) {
			waitTime = Integer.parseInt(strWaite);
			Utils.androidFileSave(getBaseContext(), Utils.STORE_AIRPLANE_WAIT_TIME_FILENAME, strWaite);
		} else {
			editWaitTime.setText("" + waitTime);
		}
		editTestTimes.setText(testMax + "");
		editTestTimes.setEnabled(false);
		editWaitTime.setEnabled(false);
		btnStart.setEnabled(false);
		testOn = true;
		strOut = "++++++++++Airplane test start!";
		Utils.logOut(Utils.TEST_AIRPLANE_TAG, strOut);
		Thread testThread = new testThread();
		Thread listenThread = new listenThread();
		testThread.start();
		listenThread.start();
		textOutput.setText(strOut);
		textShowLeftTimes.setText("Left times:  " + testMax);
	}

	private void goBack() {
		if (Utils.autoTesting) {
			// test cancled return
			setResult(RESULT_CANCELED);
		}
		finish();
		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
	}

	@Override
	protected void onDestroy() {
		wakeLock.release();
		testOn = false;
		String thisOut = "++++++++++++Airplane test Activity Destroyed!";
		strOut = strOut + "\n" + thisOut;
		Utils.logOut(Utils.TEST_AIRPLANE_TAG, thisOut);
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
							testHandler.sendEmptyMessage(MSG_CHANGE_AIR_STATE);
						} else {
							testOn = false;
							testHandler.sendEmptyMessage(MSG_AIR_TEST_END);
						}
						sleep(waitTime * 1000);
					}
					sleep(500);
				}

			} catch (Exception e) {
				Log.e(Utils.TEST_AIRPLANE_TAG, "airplane test  Thread Error!");
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
					testHandler.sendEmptyMessage(MSG_LISTEN_AIR_STATE);
					sleep(Utils.LISTENTIME);
				} while (testOn);
			} catch (Exception e) {
				Log.e(Utils.TEST_AIRPLANE_TAG, "airplane test  Thread Error!");
				e.printStackTrace();
			}
		}
	}

	private class myHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LISTEN_AIR_STATE: {
				listenState();
				textOutput.setText(strOut);
				break;
			}
			case MSG_CHANGE_AIR_STATE: {
				String thisOut = "";
				String left = "Left times:  " + (int) (testMax - testCount);
				if (listenState().equals("0")) {
					textShowLeftTimes.setText(left);
					try {
						Settings.System.putString(cr, Settings.System.AIRPLANE_MODE_ON, "1");
						Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
						intent.putExtra("state", "1");
						sendBroadcast(intent);
					} catch (Exception e) {
						thisOut = "++++++++++++Airplane Mode Start ERROR! " + "\n" + e.toString();
						strOut = strOut + "\n" + thisOut;
						Utils.logOut(Utils.TEST_AIRPLANE_TAG, thisOut);
					}

				} else if (listenState().equals("1")) {
					testCount++;
					try {
						Settings.System.putString(cr, Settings.System.AIRPLANE_MODE_ON, "0");
						Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
						intent.putExtra("state", "0");
						sendBroadcast(intent);
					} catch (Exception e) {
						thisOut = "++++++++++++Airplane Mode Stop ERROR! " + "\n" + e.toString();
						strOut = strOut + "\n" + thisOut;
						Utils.logOut(Utils.TEST_AIRPLANE_TAG, thisOut);
					}
				}

				textOutput.setText(strOut);
				break;
			}
			case MSG_AIR_TEST_END: {
				editWaitTime.setEnabled(true);
				editTestTimes.setEnabled(true);
				btnStart.setEnabled(true);
				testCount = 0;
				String thisOut = "++++++++++++Airplane test finish!";
				strOut = strOut + "\n" + thisOut;
				Utils.logOut(Utils.TEST_AIRPLANE_TAG, thisOut);
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

	private String listenState() {
		String state = "-1";
		if (Settings.System.getString(cr, Settings.System.AIRPLANE_MODE_ON).equals("0")) {
			state = "0";
			String thisOut = "Airplane   state  off";
			textShowState.setText(thisOut);
		} else {
			state = "1";
			String thisOut = "Airplane   state  on";
			textShowState.setText(thisOut);
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
