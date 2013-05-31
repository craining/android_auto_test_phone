package com.easyandroid.autotest.testactivities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.SmsManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.easyandroid.autotest.R;
import com.easyandroid.autotest.Utils;

public class SMSTestActivity extends Activity {
	private final static int MSG_START_SEND_MSSAGE = 17;
	private final static int MSG_STOP_SEND_MESSAGE = 18;
	public final static int MSG_RECEIVE_SHOW = 19;

	private String SENT_SMS_ACTION = "SENT_SMS_ACTION";
	public static final String sendHead = "Sended COUNT:  ";
	public static final String receiveHead = "Received COUNT:  ";

	private boolean testOn = false;
	private int testCount = 0;
	private int testMax = 0;
	private String msgContent = "";
	public Handler testHandler;

	private EditText editTestTimes;
	private EditText editNumber;
	private EditText editMsgContent;
	private Button btnStart;
	private Button btnStop;
	private Button btnGoBack;
	private TextView textOutput;
	private String getPhoneNum = "";

	public static int receiveCount = 0;

	private EditText editWaitTime;
	private int waitTime = Utils.WAIT_TIME_SMS;
	private String output = "";

	public static String getMessage = "";
	public static boolean receivedMsg = false;

	private boolean running = false;
	private PowerManager pm;
	private WakeLock wakeLock;

	// private String sendContent = "";
	public static int countLength = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sms);
		setTitle(getString(R.string.test_sms));

		editTestTimes = (EditText) findViewById(R.id.edit_sms);
		editNumber = (EditText) findViewById(R.id.edit_sms_num);
		editMsgContent = (EditText) findViewById(R.id.edit_sms_content);

		btnStart = (Button) findViewById(R.id.btn_sms);
		btnStop = (Button) findViewById(R.id.btn_sms_stop);
		btnGoBack = (Button) findViewById(R.id.btn_sms_return);

		textOutput = (TextView) findViewById(R.id.text_sms);
		editWaitTime = (EditText) findViewById(R.id.edit_sms_waite);

		msgContent = Utils.SMS_TEST_DEFAULT_CONTENT;

		// add a lock, to avoid system sleeping
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getCanonicalName());
		wakeLock.acquire();

		if ((Utils.STORE_SMS_TEST_LOG_PATH).exists()) {
			String outPutStr = "Recently test result : \n\n" + Utils.getFileContent(Utils.STORE_SMS_TEST_LOG_PATH);
			textOutput.setText(getTextStyle(outPutStr));
			// textOutput.setText(getTextStyle(outPutStr, receiveHead,
			// Color.GREEN));
		}
		running = true;

		testHandler = new myHandler();

		if ((Utils.STORE_NUMBER_PATH).exists()) {
			editNumber.setText(Utils.androidFileload(SMSTestActivity.this, Utils.STORE_NUMBER_FILENAME));
		}
		if ((Utils.STORE_SMS_TEST_TIMES_PATH).exists()) {
			String times = Utils.androidFileload(getBaseContext(), Utils.STORE_SMS_TEST_TIMES_FILENAME);
			editTestTimes.setText(times);
			countLength = times.length();
		}
		if ((Utils.STORE_SMS_WAIT_TIME_PATH).exists()) {
			editWaitTime.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_SMS_WAIT_TIME_FILENAME));
		}
		if ((Utils.STORE_SMS_CONTENT_PATH).exists()) {
			editMsgContent.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_SMS_CONTENT_FILENAME));
		}
		registerReceiver(sendMessage, new IntentFilter(SENT_SMS_ACTION));

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
				testHandler.sendEmptyMessage(MSG_STOP_SEND_MESSAGE);
			}
		});
		btnGoBack.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				goBack();
			}
		});

	}

	private SpannableStringBuilder getTextStyle(String outPut) {
		SpannableStringBuilder style = new SpannableStringBuilder(outPut);
		String outTemp = outPut;
		int start = 0;
		int tagSendLength = countLength + sendHead.length();
		int tagreceiveLength = countLength + receiveHead.length();
		while (outTemp.contains(sendHead)) {
			start = start + outTemp.indexOf(sendHead);
			outTemp = outPut.substring((start + tagSendLength), (outPut.length() - 1));
			style.setSpan(new BackgroundColorSpan(Color.GREEN), start, start + tagSendLength, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			start += tagSendLength;
		}
		outTemp = outPut;
		start = 0;
		while (outTemp.contains(receiveHead)) {
			start = start + outTemp.indexOf(receiveHead);
			outTemp = outPut.substring((start + tagreceiveLength), (outPut.length() - 1));
			style.setSpan(new BackgroundColorSpan(Color.YELLOW), start, start + tagreceiveLength, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
			start += tagreceiveLength;
		}
		return style;
	}

	public static String getCountFormat(int count) {

		String countStr = count + "";
		if (countStr.length() < countLength) {
			String tempZero = "";
			for (int i = 0; i <= countLength; i++) {
				tempZero = tempZero + "0";
			}
			String pre = tempZero.substring(0, (tempZero.length() - countStr.length() - 1));
			return pre + countStr;
		} else {
			return count + "";
		}

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
		running = false;
		testOn = false;
		String thisOut = "++++++++++++Sms test Activity Destroyed!";
		Log.i(Utils.TEST_SMS_TAG, thisOut);
		unregisterReceiver(sendMessage);
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
							testHandler.sendEmptyMessage(MSG_START_SEND_MSSAGE);
						} else {
							testOn = false;
							testHandler.sendEmptyMessage(MSG_STOP_SEND_MESSAGE);
						}
						sleep(waitTime * 1000);
					}
					sleep(500);
				}
			} catch (Exception e) {
				Log.e(Utils.TEST_SMS_TAG, "Sms test  Thread Error!");
				e.printStackTrace();
			}
		}
	}

	class receiveThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				do {
					if (receivedMsg) {
						receivedMsg = false;
						testHandler.sendEmptyMessage(MSG_RECEIVE_SHOW);
					}
					sleep(1000);
				} while (running);
			} catch (Exception e) {
				Log.e(Utils.TEST_SMS_TAG, "Sms test  Thread Error!");
				e.printStackTrace();
			}
		}
	}

	private class myHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_START_SEND_MSSAGE: {
				testCount++;
				// String sendContent = Utils.TAG_SMS_CONTENT_HEAD + ":" +
				// testCount;
				String showContent = sendHead + getCountFormat(testCount) + "----Content: " + msgContent;
				try {
					sendSMS(getPhoneNum, msgContent);
					Utils.logOut(Utils.TEST_SMS_TAG, showContent);
					output = output + "\n" + showContent;
				} catch (Exception e) {
					Utils.logOut(Utils.TEST_SMS_TAG, "!!!  SEND MSG ERROR!!!");
					output = output + "\n!!!  SEND MSG ERROR!!!";
				}
				textOutput.setText(getTextStyle(output));
				break;
			}
			case MSG_STOP_SEND_MESSAGE: {
				// output = output + "\n\n" + getString(R.string.all_send) +
				// testCount + "\n";
				// textOutput.setText(output);
				editTestTimes.setEnabled(true);
				btnStart.setEnabled(true);
				editNumber.setEnabled(true);
				editWaitTime.setEnabled(true);
				editMsgContent.setEnabled(true);
				break;
			}
			case MSG_RECEIVE_SHOW: {
				output = output + "\n" + getMessage;
				if (receiveCount == testMax) {
					output = output + "\n\n\nAll  " + receiveCount + "messages have been received!  \n\n\n";
					// Only all send messages have been received can do the next
					// test if is having a test of many items.
					if (Utils.autoTesting) {
						setResult(RESULT_OK);
						finish();
					}
				}
				textOutput.setText(getTextStyle(output));
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
		Utils.newSmsTest = true;
		testCount = 0;
		testMax = 0;
		receiveCount = 0;
		try {
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		} catch (Exception e) {
			// Log.e(Utils.TEST_SMS_TAG, "try to hide keyboard");
		}
		String strTestTimes = editTestTimes.getText().toString();
		String strNum = editNumber.getText().toString();
		String strWaite = editWaitTime.getText().toString();
		String strMsgContent = editMsgContent.getText().toString();
		if (!TextUtils.isEmpty(strTestTimes)) {
			Utils.androidFileSave(SMSTestActivity.this, Utils.STORE_SMS_TEST_TIMES_FILENAME, strTestTimes);
			testMax = Integer.parseInt(strTestTimes);
		} else {
			testMax = Utils.TEST_TIMES_SMS;
		}

		countLength = (testMax + "").length();
		if (!TextUtils.isEmpty(strMsgContent)) {
			msgContent = strMsgContent;
			Utils.androidFileSave(SMSTestActivity.this, Utils.STORE_SMS_CONTENT_FILENAME, msgContent);
		}
		if (!TextUtils.isEmpty(strNum)) {

			if (!TextUtils.isEmpty(strWaite)) {
				waitTime = Integer.parseInt(strWaite);
				Utils.androidFileSave(SMSTestActivity.this, Utils.STORE_SMS_WAIT_TIME_FILENAME, strWaite);
			} else {
				editWaitTime.setText("" + waitTime);
			}
			editTestTimes.setText(testMax + "");
			editMsgContent.setText(msgContent);
			editMsgContent.setEnabled(false);
			getPhoneNum = strNum;
			editNumber.setEnabled(false);
			editWaitTime.setEnabled(false);
			editTestTimes.setEnabled(false);
			btnStart.setEnabled(false);
			testOn = true;
			output = "++++++++++SMS test start!";
			textOutput.setText(output);
			Thread testThread = new testThread();
			testThread.start();
			Thread receive = new receiveThread();
			receive.start();
			String toast = "All test times: " + testMax + "\nWait time: " + waitTime;
			output = output + "\n" + toast;
			textOutput.setText(output);
			Utils.logOut(Utils.TEST_SMS_TAG, output);
			Utils.androidFileSave(SMSTestActivity.this, Utils.STORE_NUMBER_FILENAME, getPhoneNum);
		} else {
			Toast.makeText(getBaseContext(), getString(R.string.number), Toast.LENGTH_LONG).show();
			Log.e(Utils.TEST_SMS_TAG, getString(R.string.number));
			if (Utils.autoTesting) {
				// test error , goback
				setResult(RESULT_CANCELED);
				finish();
			}
		}
	}

	/**
	 * send one message
	 * 
	 * @param phoneNumber
	 * @param message
	 */

	private void sendSMS(String phoneNumber, String message) {
		// ---sends an SMS message to another device---
		SmsManager sms = SmsManager.getDefault();
		// create the sentIntent parameter
		Intent sentIntent = new Intent(SENT_SMS_ACTION);
		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, sentIntent, 0);
		sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
		addToDatabase(message);
	}

	/**
	 * add to database
	 * 
	 * @param content
	 */
	private void addToDatabase(String content) {
		ContentValues values = new ContentValues();
		// send time
		values.put("date", System.currentTimeMillis());
		// have read or not
		values.put("read", 0);
		// 1:receive 2:send
		values.put("type", 2);
		// send to whom
		values.put("address", getPhoneNum);
		// send message content
		values.put("body", content);
		// insert to database
		getContentResolver().insert(Uri.parse("content://sms"), values);
	}

	private BroadcastReceiver sendMessage = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				// send success! but it is late.

				break;
			default:
				break;

			}
		}
	};

}
