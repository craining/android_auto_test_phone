package com.easyandroid.autotest.testactivities;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
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

public class GpsTestActivity extends Activity {
	private final static int MSG_LISTEN_GPS_STATE = 10;
	private final static int MSG_CHANGE_GPS_STATE = 11;
	private final static int MSG_GPS_TEST_END = 12;
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

	private EditText editWaitTime;
	private int waitTime = Utils.WAIT_TIME_GPS;
	private TextView textShowLeftTimes;
	private TextView textShowState;
	private PowerManager pm;
	private WakeLock wakeLock;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gps);
		setTitle(R.string.test_gps);
		editTestTimes = (EditText) findViewById(R.id.edit_gps);
		btnStart = (Button) findViewById(R.id.btn_gps);
		btnStop = (Button) findViewById(R.id.btn_gps_stop);
		btnGoBack = (Button) findViewById(R.id.btn_gps_return);
		textOutput = (TextView) findViewById(R.id.test_gps);
		editWaitTime = (EditText) findViewById(R.id.edit_gps_waite);
		textShowLeftTimes = (TextView) findViewById(R.id.text_gps_leftTimes);
		textShowState = (TextView) findViewById(R.id.text_show_gpsstate);


		//add a lock, to avoid system sleeping
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getCanonicalName());
		wakeLock.acquire();
		
		if ((Utils.STORE_GPS_TEST_TIMES_PATH).exists()) {
			editTestTimes.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_GPS_TEST_TIMES_FILENAME));
		}
		if ((Utils.STORE_GPS_WAIT_TIME_PATH).exists()) {
			editWaitTime.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_GPS_WAIT_TIME_FILENAME));
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
				testHandler.sendEmptyMessage(MSG_GPS_TEST_END);
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
			// Log.e(Utils.TEST_GPS_TAG, "try to hide keyboard");
		}
		testMax = 0;
		String strTestTimes = editTestTimes.getText().toString();
		String strWaite = editWaitTime.getText().toString();
		if (!TextUtils.isEmpty(strTestTimes)) {
			Utils.androidFileSave(getBaseContext(), Utils.STORE_GPS_TEST_TIMES_FILENAME, strTestTimes);
			testMax = Integer.parseInt(strTestTimes);
		} else {
			testMax = Utils.TEST_TIMES_GPS;
		}
		if (!TextUtils.isEmpty(strWaite)) {
			waitTime = Integer.parseInt(strWaite);
			Utils.androidFileSave(getBaseContext(), Utils.STORE_GPS_WAIT_TIME_FILENAME, strWaite);
		} else {
			editWaitTime.setText("" + waitTime);
		}
		editTestTimes.setText(testMax + "");
		editTestTimes.setEnabled(false);
		editWaitTime.setEnabled(false);
		btnStart.setEnabled(false);
		testOn = true;
		strOut = "++++++++++GPS test start!";
		Utils.logOut(Utils.TEST_GPS_TAG, strOut);
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
		String thisOut = "++++++++++++GPS test Activity Destroyed";
		strOut = strOut + "\n" + thisOut;
		Utils.logOut(Utils.TEST_GPS_TAG, thisOut);

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
							testHandler.sendEmptyMessage(MSG_CHANGE_GPS_STATE);
						} else {
							testOn = false;
							testHandler.sendEmptyMessage(MSG_GPS_TEST_END);
						}
						sleep(waitTime * 1000);
					}
					sleep(500);
				}

			} catch (Exception e) {
				Log.e(Utils.TEST_GPS_TAG, "gps test  Thread Error!");
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
					testHandler.sendEmptyMessage(MSG_LISTEN_GPS_STATE);
					sleep(Utils.LISTENTIME);
				} while (testOn);
			} catch (Exception e) {
				Log.e(Utils.TEST_GPS_TAG, "gps test  Thread Error!");
				e.printStackTrace();
			}
		}
	}

	private class myHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LISTEN_GPS_STATE: {
				listenState( );
				textOutput.setText(strOut);
				break;
			}
			case MSG_CHANGE_GPS_STATE: {
				String thisOut = "";
				String left = "Left times:  " + (int) (testMax - testCount);
				if (!listenState( )) {
					textShowLeftTimes.setText(left);
					try {
						changeGpsState();
					} catch (Exception e) {
						thisOut = "++++++++++++GPS Change state ERROR!!  " + "\n" + e.toString();
						strOut = strOut + "\n" + thisOut;
						Utils.logOut(Utils.TEST_GPS_TAG, thisOut);
					}
				} else {
					testCount++;
					try {
						changeGpsState();
					} catch (Exception e) {
						thisOut = "++++++++++++GPS Change state ERROR!!  " + "\n" + e.toString();
						strOut = strOut + "\n" + thisOut;
						Utils.logOut(Utils.TEST_GPS_TAG, thisOut);
					}
				}
				textOutput.setText(strOut);
				break;
			}
			case MSG_GPS_TEST_END: {
				testOn = false;
				editTestTimes.setEnabled(true);
				editWaitTime.setEnabled(true);
				btnStart.setEnabled(true);
				testCount = 0;
				String thisOut = "++++++++++++GPS test finish!";
				strOut = strOut + "\n" + thisOut;
				Utils.logOut(Utils.TEST_GPS_TAG, thisOut);
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

	private boolean listenState() {
		boolean state = Settings.Secure.isLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER);
		if (state) {
			String thisOut = "gps state on!";
			textShowState.setText(thisOut);
		} else {
			String thisOut = "gps state off!";
			textShowState.setText(thisOut);
		}
		return state;
	}

	private void changeGpsState() {
		boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER);
		if (gpsEnabled) {
			// turn off GPS
			Settings.Secure.setLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER, false);
		} else {
			// turn on GPS
			Settings.Secure.setLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER, true);
		}

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			goBack();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

}