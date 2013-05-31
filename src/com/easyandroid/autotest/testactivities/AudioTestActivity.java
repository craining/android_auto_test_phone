package com.easyandroid.autotest.testactivities;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
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

public class AudioTestActivity extends Activity {

	private Button btnStart;
	private Button btnStop;
	private Button btnGoback;
	private EditText editTestAlltime;
	private EditText editWaitTime;
	private TextView textShowState;

	private boolean testOn;
	private int testAllTime = Utils.AUDIO_TEST_ALL_TIME;
	private int testWaitTime = Utils.AUDIO_TEST_WAIT_TIME;
	private int testCount = 0;// when testCount%testWaitTime == 0, change the
								// state
	private Handler testHandler;
	private final static int MSG_CHANGE_AUDIO_STATE = 30;
	private final static int MSG_LISTEN_AUDIO_STATE = 31;
	private final static int MSG_AUDIO_TEST_END = 32;

	private AudioManager audioMg;

	private MediaPlayer mediaPlayer;
	private PowerManager pm;
	private WakeLock wakeLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audio);
		setTitle(R.string.test_audio);

		btnStart = (Button) findViewById(R.id.btn_audio_start);
		btnStop = (Button) findViewById(R.id.btn_audio_stop);
		btnGoback = (Button) findViewById(R.id.btn_audio_return);
		textShowState = (TextView) findViewById(R.id.test_audio_state);
		editTestAlltime = (EditText) findViewById(R.id.edit_audio_alltime);
		editWaitTime = (EditText) findViewById(R.id.edit_audio_waite);

		//add a lock, to avoid system sleeping
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getCanonicalName());
		wakeLock.acquire();
		
		mediaPlayer = MediaPlayer.create(this, R.raw.soundtest);

		audioMg = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		testHandler = new myHandler();
		Thread testThread = new testThread();
		testThread.start();

		if ((Utils.STORE_AUDIO_TEST_ALL_TIME_PATH).exists()) {
			editTestAlltime.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_AUDIO_TEST_ALL_TIME_FILENAME));
		}
		if ((Utils.STORE_AUDIO_WAIT_TIME_PATH).exists()) {
			editWaitTime.setText(Utils.androidFileload(getBaseContext(), Utils.STORE_AUDIO_WAIT_TIME_FILENAME));
		}
		testState();
		if (Utils.autoTesting) {
			// if is doing the test of many items
			btnStop.setText(R.string.autotest_stop_one);
			btnGoback.setText(R.string.autotest_stop_all);
			startTest();
		}
		btnStart.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startTest();
			}
		});
		btnStop.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				testHandler.sendEmptyMessage(MSG_AUDIO_TEST_END);
			}
		});
		btnGoback.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				goBack();
			}
		});

	}

	class testThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				while (true) {
					if (testOn) {
						if (testCount < testAllTime) {
							testHandler.sendEmptyMessage(MSG_LISTEN_AUDIO_STATE);
							if (testCount % testWaitTime == 0) {
								testHandler.sendEmptyMessage(MSG_CHANGE_AUDIO_STATE);
							}
						} else {
							testHandler.sendEmptyMessage(MSG_AUDIO_TEST_END);
						}
						sleep(1000);
						testCount++;
					} else {
						sleep(1000);
					}
				}
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
			case MSG_LISTEN_AUDIO_STATE: {
				testState();
				break;
			}
			case MSG_CHANGE_AUDIO_STATE: {
				changeState();
				break;
			}
			case MSG_AUDIO_TEST_END: {
				if (mediaPlayer != null && mediaPlayer.isPlaying()) {
					mediaPlayer.stop();
				}
				testOn = false;
				editTestAlltime.setEnabled(true);
				editWaitTime.setEnabled(true);
				btnStart.setEnabled(true);
				audioMg.setMode(AudioManager.MODE_NORMAL);
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

	private void testState() {
		String forReturn = "";
		String showStateInTextView = "";
		// check the state of speaker
		if (audioMg.isSpeakerphoneOn()) {
			forReturn = "Speaker ON   ";
			showStateInTextView = getString(R.string.text_speaker) + " ON      ";
		} else {
			forReturn = "Speaker OFF   ";
			showStateInTextView = getString(R.string.text_speaker) + " OFF      ";
		}
		// check the state of earpiece
		if (audioMg.getMode() == AudioManager.MODE_IN_CALL) {
			forReturn = forReturn + "earpiece  ON  ";
			showStateInTextView = showStateInTextView + getString(R.string.text_earpiece) + " ON  ";
		} else {
			forReturn = forReturn + "earpiece  OFF  ";
			showStateInTextView = showStateInTextView + getString(R.string.text_earpiece) + " OFF  ";
		}
		
//		// check the state of headset
//		if (audioMg.isWiredHeadsetOn()) {
//			forReturn = forReturn + " Headset ON  ";
//			showStateInTextView = showStateInTextView + getString(R.string.text_headset) + " ON  ";
//		} else {
//			forReturn = forReturn + " Headset OFF  ";
//			showStateInTextView = showStateInTextView + getString(R.string.text_headset) + " OFF  ";
//		}
		
		textShowState.setText(showStateInTextView);
		// Utils.logOut(Utils.TEST_AUDIO_TAG, forReturn);
	}

	private void changeState() {

		if (audioMg.isSpeakerphoneOn()) {
			audioMg.setSpeakerphoneOn(false);
			audioMg.setMode(AudioManager.MODE_IN_CALL);
		} else {
			audioMg.setSpeakerphoneOn(true);
			audioMg.setMode(AudioManager.MODE_NORMAL);
		}
	}

	private void goBack() {
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
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
		String thisOut = "+++++++++++Audio test Activity Destroyed!";
		Utils.logOut(Utils.TEST_WIFI_TAG, thisOut);
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		super.onDestroy();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			goBack();
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void startTest() {

		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}

		playSound(R.raw.soundtest);

		try {
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); 
		} catch (Exception e) {
//			 Log.e(Utils.TEST_AUDIO_TAG, "try to hide keyboard");
		}
		String getAllTime = editTestAlltime.getText().toString();
		String getWaitTime = editWaitTime.getText().toString();
		if (!(TextUtils.isEmpty(getAllTime))) {
			testAllTime = Integer.parseInt(getAllTime);
			editTestAlltime.setText(testAllTime + "");
			Utils.androidFileSave(getBaseContext(), Utils.STORE_AUDIO_TEST_ALL_TIME_FILENAME, testAllTime + "");
		}
		if (!(TextUtils.isEmpty(getWaitTime))) {
			testWaitTime = Integer.parseInt(getWaitTime);
			editWaitTime.setText(testWaitTime + "");
			Utils.androidFileSave(getBaseContext(), Utils.STORE_AUDIO_WAIT_TIME_FILENAME, testWaitTime + "");
		}

		editTestAlltime.setText(testAllTime + "");
		editWaitTime.setText(testWaitTime + "");
		testOn = true;
		testCount = 0;
		editTestAlltime.setEnabled(false);
		editWaitTime.setEnabled(false);
		btnStart.setEnabled(false);
	}

	private void playSound(int raw) {
		try {
			mediaPlayer = MediaPlayer.create(this, raw);
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.reset();
			}
			mediaPlayer.start();
		} catch (Exception e) {
			Log.e(Utils.TEST_AUDIO_TAG, "PLAY Test Sound error!!!!\n" + e + "");
		}
		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0) {
				if (mediaPlayer != null) {
					mediaPlayer.start();
				}
			}
		});
		mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer player, int arg1, int arg2) {
				mediaPlayer.release();
				mediaPlayer = null;
				return false;
			}
		});
	}

}
