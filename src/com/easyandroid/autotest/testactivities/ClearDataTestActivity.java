package com.easyandroid.autotest.testactivities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.easyandroid.autotest.AutoTestActivity;
import com.easyandroid.autotest.R;
import com.easyandroid.autotest.Utils;

public class ClearDataTestActivity extends Activity {

	private Handler testHandler;
	private static final int MSG_WAPE_DATA = 111;
	private static final int MSG_UPDATE_VIEW = 112;
	private static final String TAG = "ClearDataTest";
	private TextView textShowShowLeftSeconds;
	private Button btnGoback;
	private Button btnStop;
	private Button btnStart;

	private static final int waitTime = Utils.WAIT_TIME_CLEAR;
	private boolean testOff = true;
	private int count = 0;
	
	private PowerManager pm;
	private WakeLock wakeLock;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cleardata);
		setTitle(R.string.btn_wipedatatest);
		textShowShowLeftSeconds = (TextView) findViewById(R.id.text_showLeftSeconds);
		btnGoback = (Button) findViewById(R.id.btn_clear_goback);
		btnStop = (Button) findViewById(R.id.btn_clear_stop);
		btnStart = (Button) findViewById(R.id.btn_celar_start);

		//add a lock, to avoid system sleeping
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getCanonicalName());
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getCanonicalName());
		wakeLock.acquire();
		
		testHandler = new myHandler();

		Thread testThread = new testThread();
		testThread.start();

		if (!(Utils.STORE_CLEAR_TEST_TIMES_PATH).exists()) {
			btnStart.setEnabled(false);
			btnStop.setEnabled(true);
			testOff = false;
		} else {
			testOff = true;
			btnStart.setEnabled(true);
			btnStop.setEnabled(false);
		}

		textShowShowLeftSeconds.setText(waitTime + "");

		if (Utils.autoTesting) {
			// if doing the test of many items
			btnStop.setText(R.string.autotest_stop_one);
			btnGoback.setText(R.string.autotest_stop_all);
			startTest();
		}

		btnGoback.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				goBack();
			}
		});

		btnStart.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startTest();
			}
		});

		btnStop.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				testOff = true;
				count = 0;
				(Utils.STORE_CLEAR_TEST_TIMES_PATH).mkdir();
				btnStart.setEnabled(true);
				btnStop.setEnabled(false);
				testHandler.sendEmptyMessage(MSG_UPDATE_VIEW); 
				if (Utils.autoTesting) {
					// test finish
					setResult(RESULT_OK);
					finish();
				}
			}
		});

	}

	private void goBack() {
		testOff = true;
		(Utils.STORE_CLEAR_TEST_TIMES_PATH).mkdir();

		if (Utils.autoTesting) {
			// test canceled
			setResult(RESULT_CANCELED);
		} else {
			startActivity(new Intent(ClearDataTestActivity.this, AutoTestActivity.class));
		}

		finish();

		overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
	}

	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		wakeLock.release();
		super.onDestroy();
	}


	class testThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				while (true) {
					if (count >= waitTime) {
						if (!testOff) {
							testHandler.sendEmptyMessage(MSG_WAPE_DATA);
						}
					} else {
						if (!testOff) {
							count++;
							testHandler.sendEmptyMessage(MSG_UPDATE_VIEW); 
						}
					}
					sleep(1000);
				}
			} catch (Exception e) {
				Log.e(TAG, "Screen test  Thread Error!");
				e.printStackTrace();
			}
		}
	}

	private class myHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_VIEW: {
				textShowShowLeftSeconds.setText(waitTime - count + "");
				break;
			}
			case MSG_WAPE_DATA: {
				Log.e(TAG, "Clearing data!");
				if (!(Utils.STORE_CLEAR_TEST_TIMES_PATH).exists()) {
					try {
						sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
						testOff = true;
					} catch (Exception e) {
						Log.e(TAG, "sendBroadcast MASTER_CLEAR error!");
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
		// delete the tag of clear test
		if ((Utils.STORE_CLEAR_TEST_TIMES_PATH).exists()) {
			(Utils.STORE_CLEAR_TEST_TIMES_PATH).delete();
			testOff = false;
			btnStart.setEnabled(false);
			btnStop.setEnabled(true);
		}
	}

}