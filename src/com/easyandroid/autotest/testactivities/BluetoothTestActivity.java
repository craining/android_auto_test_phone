package com.easyandroid.autotest.testactivities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
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

public class BluetoothTestActivity extends Activity {

	private BluetoothAdapter mAdapter;
	private final static int MSG_LISTEN_BLUETOOTH_STATE = 5;
	private final static int MSG_CHANGE_BLUETOOTH_STATE = 6;
	private final static int MSG_BLUETOOTH_TEST_END = 7;
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
	private TextView textShowState;

	private String strOut = "";

	private EditText editWaitTime;
	private int waitTime = Utils.WAIT_TIME_BLUETOOTH;
	private PowerManager pm;
	private WakeLock wakeLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bluetooth);
		setTitle(R.string.test_bluetooth);
		editTestTimes = (EditText) findViewById(R.id.edit_bluetooth);
		btnStart = (Button) findViewById(R.id.btn_bluetooth);
		btnStop = (Button) findViewById(R.id.btn_bluetooth_stop);
		btnGoBack = (Button) findViewById(R.id.btn_bluetooth_return);
		textOutput = (TextView) findViewById(R.id.test_bluetooth);
		editWaitTime = (EditText) findViewById(R.id.edit_bluetooth_waite);
		textShowLeftTimes = (TextView) findViewById(R.id.text_bluetooth_leftTimes);
		textShowState = (TextView) findViewById(R.id.text_show_bluetoothstate);


		//add a lock, to avoid system sleeping
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getCanonicalName());
		wakeLock.acquire();

		mAdapter = BluetoothAdapter.getDefaultAdapter();

		if ((Utils.STORE_BLUETOOTH_TIMES_PATH).exists()) {
			editTestTimes.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_BLUETOOTH_TIMES_FILENAME));
		}
		if ((Utils.STORE_BLUETOOTH_WAIT_TIME_PATH).exists()) {
			editWaitTime.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_BLUETOOTH_WAIT_TIME_FILENAME));
		}
		testHandler = new myHandler();

		if (Utils.autoTesting) {
			// if doing the test of many items
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
				testHandler.sendEmptyMessage(MSG_BLUETOOTH_TEST_END);
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
			// Log.e(Utils.TEST_BLUETOOTH_TAG, "try to hide keyboard");
		}

		testMax = 0;
		String strTestTimes = editTestTimes.getText().toString();
		String strWaite = editWaitTime.getText().toString();
		if (!TextUtils.isEmpty(strTestTimes)) {
			Utils.androidFileSave(getBaseContext(), Utils.STORE_BLUETOOTH_TIMES_FILENAME, strTestTimes);
			testMax = Integer.parseInt(strTestTimes);
		} else {
			testMax = Utils.TEST_TIMES_BLUETOOTH;
		}
		if (!TextUtils.isEmpty(strWaite)) {
			waitTime = Integer.parseInt(strWaite);
			Utils.androidFileSave(getBaseContext(), Utils.STORE_BLUETOOTH_WAIT_TIME_FILENAME, strWaite);
		} else {
			editWaitTime.setText("" + waitTime);
		}
		editTestTimes.setText(testMax + "");
		editTestTimes.setEnabled(false);
		editWaitTime.setEnabled(false);
		btnStart.setEnabled(false);
		testOn = true;
		strOut = "++++++++++Bluetooth test start!";
		Utils.logOut(Utils.TEST_BLUETOOTH_TAG, strOut);
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
		String thisOut = "++++++++++++Bluetooth test Activity Destroyed!";
		strOut = strOut + "\n" + thisOut;
		Utils.logOut(Utils.TEST_BLUETOOTH_TAG, thisOut);

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
							testHandler.sendEmptyMessage(MSG_CHANGE_BLUETOOTH_STATE);
						} else {
							testOn = false;
							testHandler.sendEmptyMessage(MSG_BLUETOOTH_TEST_END);
						}
						sleep(waitTime * 1000);
					}
					sleep(500);
				}

			} catch (Exception e) {
				Log.e(Utils.TEST_BLUETOOTH_TAG, "Bluetooth test  Thread Error!");
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
					testHandler.sendEmptyMessage(MSG_LISTEN_BLUETOOTH_STATE);
					sleep(Utils.LISTENTIME);
				} while (testOn);
			} catch (Exception e) {
				Log.e(Utils.TEST_BLUETOOTH_TAG, "Bluetooth test  Thread Error!");
				e.printStackTrace();
			}
		}
	}

	private class myHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LISTEN_BLUETOOTH_STATE: {
				listenState( );
				textOutput.setText(strOut);
				break;
			}
			case MSG_CHANGE_BLUETOOTH_STATE: {
				String left = "Left times:  " + (int) (testMax - testCount);
				String thisOut = "";
				if (listenState( ) == 1) {
 
					Utils.logOut(Utils.TEST_BLUETOOTH_TAG, thisOut);
					textShowLeftTimes.setText(left);
					try {
						mAdapter.enable();
					} catch (Exception e) {
						thisOut = "++++++++++++Bluetooth  Enable ERROR!" + "\n" + e.toString();
						strOut = strOut + "\n" + thisOut;
						Utils.logOut(Utils.TEST_BLUETOOTH_TAG, thisOut);
					}

				} else if (listenState( ) == 3) {
					testCount++;
					try {
						mAdapter.disable();
					} catch (Exception e) {
						thisOut = "++++++++++++Bluetooth  Disable ERROR!" + "\n" + e.toString();
						strOut = strOut + "\n" + thisOut;
						Utils.logOut(Utils.TEST_BLUETOOTH_TAG, thisOut);
					}

				}
				textOutput.setText(strOut);
				break;
			}
			case MSG_BLUETOOTH_TEST_END: {
				editTestTimes.setEnabled(true);
				btnStart.setEnabled(true);
				editWaitTime.setEnabled(true);
				testCount = 0;
				String thisOut = "++++++++++++Bluetooth test finish!";
				strOut = strOut + "\n" + thisOut;
				Utils.logOut(Utils.TEST_BLUETOOTH_TAG, thisOut);
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
		switch (mAdapter.getState()) {
		case BluetoothAdapter.STATE_OFF: {
			thisOut = "Bluetooth off!";
			textShowState.setText(thisOut);
			state = 1;
			break;
		}

		case BluetoothAdapter.STATE_TURNING_OFF: {
			thisOut = "Bluetooth turnning off ....";
			textShowState.setText(thisOut);
			state = 2;
			break;
		}

		case BluetoothAdapter.STATE_ON: {
			thisOut = "Buletooth on!";
			textShowState.setText(thisOut);
			state = 3;
			break;
		}

		case BluetoothAdapter.STATE_TURNING_ON: {
			thisOut = "Bluetooth turnning on...";
			textShowState.setText(thisOut);
			state = 4;
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
