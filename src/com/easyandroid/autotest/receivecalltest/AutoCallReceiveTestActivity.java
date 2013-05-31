package com.easyandroid.autotest.receivecalltest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.easyandroid.autotest.R;
import com.easyandroid.autotest.Utils;

public class AutoCallReceiveTestActivity extends Activity {
	/** Called when the activity is first created. */

	private static String receiveNumber = "";
	private boolean incomingFlag = false;

	private static final String CALL_STATE_FILTER = "android.intent.action.PHONE_STATE";
	private static final int MSG_END_CALL = 113;
	private static final int MSG_ACCEPT_CALL = 114;
	private Handler callTestHandler;

	private int waitTime = 5;

	private TextView textShowReceiveTimes;
	private EditText editReceiveNumber;
	private EditText editAcceptWaitTime;
	private Button btnListenStart;
	private Button btnListenStop;
	private Button btnGoback;

	private int receivedCallTimes = 0;

	private TelephonyManager teleManager;
	private boolean registed;

	private PowerManager pm;
	private WakeLock wakeLock;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.receivecall);
		setTitle(R.string.test_phone_call);

		textShowReceiveTimes = (TextView) findViewById(R.id.text_receivecall_times);
		editReceiveNumber = (EditText) findViewById(R.id.edit_receivecall_number);
		editAcceptWaitTime = (EditText) findViewById(R.id.edit_receivecall_waittime);
		btnListenStart = (Button) findViewById(R.id.btn_receivecall_start);
		btnListenStop = (Button) findViewById(R.id.btn_receivecall_stop);
		btnGoback = (Button) findViewById(R.id.btn_receivecall_return);
		btnListenStop.setEnabled(false);

		// add a lock, to avoid system sleeping
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getCanonicalName());
		wakeLock.acquire();

		if (Utils.STORE_CALL_RECEIVE_NUMBER_PATH.exists()) {
			editReceiveNumber.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_CALL_RECEIVE_NUMBER_FILENAME));
		}
		if (Utils.STORE_CALL_RECEIVE_WAITTIME_PATH.exists()) {
			editAcceptWaitTime.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_CALL_RECEIVE_WAITTIME_FILENAME));
		}

		teleManager = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
		callTestHandler = new myHandler();

		btnListenStart.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// get the listen number and wait time
				String getNumber = editReceiveNumber.getText().toString();
				String getWaitTime = editAcceptWaitTime.getText().toString();
				if (!(TextUtils.isEmpty(getNumber))) {
					receiveNumber = getNumber;
					Utils.androidFileSave(getBaseContext(), Utils.STORE_CALL_RECEIVE_NUMBER_FILENAME, receiveNumber);
					if (!TextUtils.isEmpty(getWaitTime)) {
						waitTime = Integer.parseInt(getWaitTime);
						Utils.androidFileSave(getBaseContext(), Utils.STORE_CALL_RECEIVE_WAITTIME_FILENAME, getWaitTime);
					}
					registerReceiver();
					receivedCallTimes = 0;
					textShowReceiveTimes.setText("received times: " + receivedCallTimes);
					btnListenStart.setEnabled(false);
					btnListenStop.setEnabled(true);
					editReceiveNumber.setEnabled(false);
					editAcceptWaitTime.setEnabled(false);

				} else {
					Toast.makeText(getBaseContext(), R.string.phone_number_error, Toast.LENGTH_SHORT).show();
				}
			}
		});
		btnListenStop.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				unregisterReceiver();
				textShowReceiveTimes.setText("");
				btnListenStart.setEnabled(true);
				btnListenStop.setEnabled(false);
				editReceiveNumber.setEnabled(true);
				editAcceptWaitTime.setEnabled(true);
			}
		});
		btnGoback.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				finish();
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
			}
		});
	}

	private class myHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_END_CALL: {
				// end call
				endCall();
				break;
			}
			case MSG_ACCEPT_CALL: {
				// answer call
				answerCall();
				receivedCallTimes++;
				textShowReceiveTimes.setText("received times: " + receivedCallTimes);
				break;
			}
			default:
				break;
			}
		}
	}

	private void registerReceiver() {
		registed = true;
		registerReceiver(CallReceiver, new IntentFilter(CALL_STATE_FILTER));
	}

	private void unregisterReceiver() {
		registed = false;
		unregisterReceiver(CallReceiver);
	}

	@Override
	protected void onDestroy() {
		wakeLock.release();
		if (registed) {
			unregisterReceiver(CallReceiver);
		}
		super.onDestroy();
	}

	public BroadcastReceiver CallReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Call in
			switch (teleManager.getCallState()) {
			case TelephonyManager.CALL_STATE_RINGING:
				incomingFlag = true;
				String incoming_number = intent.getStringExtra("incoming_number");
				Log.i(Utils.TEST_CALL_TAG, "RINGING :" + incoming_number);
				if (incoming_number.equals(receiveNumber)) {
					// if is test phone number, answer it. And end it after wait
					// time later.
					callTestHandler.sendEmptyMessage(MSG_ACCEPT_CALL);
					callTestHandler.sendEmptyMessageDelayed(MSG_END_CALL, waitTime * 1000);
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				if (incomingFlag) {
					Log.i(Utils.TEST_CALL_TAG, "incoming ACCEPT");
				}
				break;

			case TelephonyManager.CALL_STATE_IDLE:
				if (incomingFlag) {
					Log.i(Utils.TEST_CALL_TAG, "incoming IDLE");
				}
				break;
			}
		}
	};

	private void answerCall() {
		Class<TelephonyManager> c = TelephonyManager.class;
		Method getITelephonyMethod = null;
		try {
			getITelephonyMethod = c.getDeclaredMethod("getITelephony", (Class[]) null);
			getITelephonyMethod.setAccessible(true);
		} catch (SecurityException e) {
			Log.e(Utils.TEST_CALL_TAG, "SecurityException: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			Log.e(Utils.TEST_CALL_TAG, "NoSuchMethodException: " + e.getMessage());
		}
		try {
			// ITelephony iTelephony = (ITelephony)
			// getITelephonyMethod.invoke(teleManager, (Object[]) null);
			ITelephony iTelephony = PhoneUtils.getITelephony(teleManager);
			iTelephony.silenceRinger();
			// answer call
			iTelephony.answerRingingCall();
		} catch (IllegalArgumentException e) {
			Log.e(Utils.TEST_CALL_TAG, "IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			Log.e(Utils.TEST_CALL_TAG, "IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			Log.e(Utils.TEST_CALL_TAG, "InvocationTargetException: " + e.getMessage());
		} catch (RemoteException e) {
			Log.e(Utils.TEST_CALL_TAG, "RemoteException: " + e.getMessage());
		} catch (Exception e) {
			Log.e(Utils.TEST_CALL_TAG, "Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void endCall() {
		try {
			ITelephony iTelephony = PhoneUtils.getITelephony(teleManager);
			// end call
			iTelephony.endCall();
		} catch (IllegalArgumentException e) {
			Log.e(Utils.TEST_CALL_TAG, "IllegalArgumentException: " + e.getMessage());
		} catch (IllegalAccessException e) {
			Log.e(Utils.TEST_CALL_TAG, "IllegalAccessException: " + e.getMessage());
		} catch (InvocationTargetException e) {
			Log.e(Utils.TEST_CALL_TAG, "InvocationTargetException: " + e.getMessage());
		} catch (RemoteException e) {
			Log.e(Utils.TEST_CALL_TAG, "RemoteException: " + e.getMessage());
		} catch (Exception e) {
			Log.e(Utils.TEST_CALL_TAG, "Exception: " + e.getMessage());
			e.printStackTrace();
		}
	}

}