package com.easyandroid.autotest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.easyandroid.autotest.testactivities.AirplaneTestActivity;
import com.easyandroid.autotest.testactivities.AudioTestActivity;
import com.easyandroid.autotest.testactivities.BluetoothTestActivity;
import com.easyandroid.autotest.testactivities.CallTestActivity;
import com.easyandroid.autotest.testactivities.ClearDataTestActivity;
import com.easyandroid.autotest.testactivities.GpsTestActivity;
import com.easyandroid.autotest.testactivities.RebootTestActivity;
import com.easyandroid.autotest.testactivities.SMSTestActivity;
import com.easyandroid.autotest.testactivities.ScreenTestActivity;
import com.easyandroid.autotest.testactivities.SysSleepTestActivity;
import com.easyandroid.autotest.testactivities.WifiTestActivity;

public class AutoTestActivity extends Activity {

	private Button btnWifi;
	private Button btnBluetooth;
	private Button btnGps;
	private Button btnAirplane;
	private Button btnSysSleep;
	private Button btnScreen;
	private Button btnAudio;
	private Button btnSms;
	private Button btnReboot;
//	private Button btnClear;

	private Button btnReceiveCall;

	private CheckBox checkWifi;
	private CheckBox checkBluetooth;
	private CheckBox checkGps;
	private CheckBox checkAirplane;
	private CheckBox checkSysSleep;
	private CheckBox checkScreen;
	private CheckBox checkAudio;
	private CheckBox checkSms;
	private CheckBox checkReboot;
//	private CheckBox checkClear;

	private Button btnAutoTestItems;
	private Button btnResetSelected;

	protected static final int MENU_ABOUT = Menu.FIRST;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setTitle(R.string.hello);

		btnWifi = (Button) findViewById(R.id.btn_wifi);
		btnBluetooth = (Button) findViewById(R.id.btn_bluetooth);
		btnGps = (Button) findViewById(R.id.btn_gps);
		btnAirplane = (Button) findViewById(R.id.btn_airplane);
		btnSysSleep = (Button) findViewById(R.id.btn_syssleep);
		btnScreen = (Button) findViewById(R.id.btn_screen);
		btnAudio = (Button) findViewById(R.id.btn_audio);
		btnSms = (Button) findViewById(R.id.btn_sms);
		btnReboot = (Button) findViewById(R.id.btn_reboot);
//		btnClear = (Button) findViewById(R.id.btn_clear);
		btnAutoTestItems = (Button) findViewById(R.id.btn_auto_test);
		btnResetSelected = (Button) findViewById(R.id.btn_auto_test_clearchecked);
		btnReceiveCall = (Button) findViewById(R.id.btn_receivecall);

		checkWifi = (CheckBox) findViewById(R.id.check_wifi);
		checkBluetooth = (CheckBox) findViewById(R.id.check_bluetooth);
		checkGps = (CheckBox) findViewById(R.id.check_gps);
		checkAirplane = (CheckBox) findViewById(R.id.check_airplane);
		checkSysSleep = (CheckBox) findViewById(R.id.check_syssleep);
		checkScreen = (CheckBox) findViewById(R.id.check_screen);
		checkAudio = (CheckBox) findViewById(R.id.check_audio);
		checkSms = (CheckBox) findViewById(R.id.check_sms);
		checkReboot = (CheckBox) findViewById(R.id.check_reboot);
//		checkClear = (CheckBox) findViewById(R.id.check_clear);

		Utils.testDoing = true;

		if ((Utils.STORE_SELECTED_TEST_ITEMS_PATH).exists()) {
			getPreSelectedItems(Utils.androidFileload(getBaseContext(), Utils.STORE_SELECTED_TEST_ITEMS_FILENAME));
		}
		// Create tag of master clear, or it will do clear opera automatically.
//		if (!(Utils.STORE_CLEAR_TEST_TIMES_PATH).exists()) {
//			(Utils.STORE_CLEAR_TEST_TIMES_PATH).mkdir();
//		}
		// If have a test of many items, and clear test is contained, the tag
		// will created. so in here, if the tag exists, delete it;
		if ((Utils.STORE_SELECTED_TEST_ITEMS_PATH).exists()) {
			(Utils.STORE_SELECTED_TEST_ITEMS_PATH).delete();
		}

		btnWifi.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(AutoTestActivity.this, WifiTestActivity.class));
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});
		btnBluetooth.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(AutoTestActivity.this, BluetoothTestActivity.class));
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});
		btnGps.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(AutoTestActivity.this, GpsTestActivity.class));
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});
		btnAirplane.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(AutoTestActivity.this, AirplaneTestActivity.class));
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});
		btnSysSleep.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(AutoTestActivity.this, SysSleepTestActivity.class));
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});
		btnScreen.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(AutoTestActivity.this, ScreenTestActivity.class));
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});
		btnAudio.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(AutoTestActivity.this, AudioTestActivity.class));
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});
		btnSms.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(AutoTestActivity.this, SMSTestActivity.class));
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});
		btnReboot.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(AutoTestActivity.this, RebootTestActivity.class));
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
				finish();
			}
		});
		
//		btnClear.setOnClickListener(new Button.OnClickListener() {
//			public void onClick(View v) {
//				startActivity(new Intent(AutoTestActivity.this, ClearDataTestActivity.class));
//				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
//				finish();
//			}
//		});
		
		btnReceiveCall.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(AutoTestActivity.this, CallTestActivity.class));
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});
		btnAutoTestItems.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				String strSelectedItems = getSelectedItems();
				if (!strSelectedItems.equals("")) {
					Utils.androidFileSave(getBaseContext(), Utils.STORE_SELECTED_TEST_ITEMS_FILENAME, strSelectedItems);
					Intent i = new Intent(AutoTestActivity.this, TestSelectedActivity.class);
					Bundle b = new Bundle();
					b.putString(Utils.TAG_SELECTED_ITEMS, strSelectedItems);
					i.putExtras(b);
					startActivityForResult(i, 0);
					Log.e("AutoTestItems", "Start AutoTest Items: " + strSelectedItems);
				} else {
					Toast.makeText(getBaseContext(), R.string.autotest_selected_null, Toast.LENGTH_SHORT).show();
				}
			}
		});

		btnResetSelected.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				clearCheckedItems();
			}
		});
		checkSms.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if (checkSms.isChecked()) {
					// If do a test of many items, and the sms test contained.
					// Check the phone number.
					if (!(Utils.STORE_NUMBER_PATH).exists()) {
						Toast.makeText(getBaseContext(), R.string.autotest_sms_num_null, Toast.LENGTH_LONG).show();
						checkSms.setChecked(false);
					}
				}
			}
		});

	}

	/**
	 * Get selected items
	 * 
	 * @return
	 */
	private String getSelectedItems() {
		String forPutStr = "";
		if (checkWifi.isChecked()) {
			forPutStr = forPutStr + Utils.TEST_WIFI_TAG + ",";
		}
		if (checkBluetooth.isChecked()) {
			forPutStr = forPutStr + Utils.TEST_BLUETOOTH_TAG + ",";
		}
		if (checkGps.isChecked()) {
			forPutStr = forPutStr + Utils.TEST_GPS_TAG + ",";
		}
		if (checkAirplane.isChecked()) {
			forPutStr = forPutStr + Utils.TEST_AIRPLANE_TAG + ",";
		}
		if (checkScreen.isChecked()) {
			forPutStr = forPutStr + Utils.TEST_SCREEN_TAG + ",";
		}
		if (checkSms.isChecked()) {
			forPutStr = forPutStr + Utils.TEST_SMS_TAG + ",";
		}
		if (checkSysSleep.isChecked()) {
			forPutStr = forPutStr + Utils.TEST_SYSSLEEP_TAG + ",";
		}
		if (checkAudio.isChecked()) {
			forPutStr = forPutStr + Utils.TEST_AUDIO_TAG + ",";
		}
		if (checkReboot.isChecked()) {
			forPutStr = forPutStr + Utils.TEST_REBOOT_TAG + ",";
		}
//		if (checkClear.isChecked()) {
//			forPutStr = forPutStr + Utils.TEST_CLEAR_TAG + ",";
//			if (!(Utils.STORE_CLEAR_AUTOTEST_TIMES_PATH).exists()) {
//				(Utils.STORE_CLEAR_AUTOTEST_TIMES_PATH).mkdir();
//			}
//		}
		return forPutStr;
	}

	/**
	 * clear selected items
	 */
	private void clearCheckedItems() {
		checkWifi.setChecked(false);
		checkBluetooth.setChecked(false);
		checkGps.setChecked(false);
		checkAirplane.setChecked(false);
		checkSysSleep.setChecked(false);
		checkScreen.setChecked(false);
		checkAudio.setChecked(false);
		checkSms.setChecked(false);
		checkReboot.setChecked(false);
//		checkClear.setChecked(false);
		if ((Utils.STORE_SELECTED_TEST_ITEMS_PATH).exists()) {
			(Utils.STORE_SELECTED_TEST_ITEMS_PATH).delete();
		}
	}

	/**
	 * get last selected item
	 */
	private void getPreSelectedItems(String SelectedItems) {
		if (SelectedItems.contains(Utils.TEST_WIFI_TAG)) {
			checkWifi.setChecked(true);
		}
		if (SelectedItems.contains(Utils.TEST_BLUETOOTH_TAG)) {
			checkBluetooth.setChecked(true);
		}
		if (SelectedItems.contains(Utils.TEST_GPS_TAG)) {
			checkGps.setChecked(true);
		}
		if (SelectedItems.contains(Utils.TEST_AIRPLANE_TAG)) {
			checkAirplane.setChecked(true);
		}
		if (SelectedItems.contains(Utils.TEST_SYSSLEEP_TAG)) {
			checkSysSleep.setChecked(true);
		}
		if (SelectedItems.contains(Utils.TEST_SCREEN_TAG)) {
			checkScreen.setChecked(true);
		}
		if (SelectedItems.contains(Utils.TEST_AUDIO_TAG)) {
			checkAudio.setChecked(true);
		}
		if (SelectedItems.contains(Utils.TEST_SMS_TAG)) {
			checkSms.setChecked(true);
		}
//		if (SelectedItems.contains(Utils.TEST_CLEAR_TAG)) {
//			checkClear.setChecked(true);
//		}
		if (SelectedItems.contains(Utils.TEST_REBOOT_TAG)) {
			checkReboot.setChecked(true);
		}

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			android.os.Process.killProcess(android.os.Process.myPid());// exit , kill process, for so many threads.
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		if ((Utils.STORE_CLEAR_AUTOTEST_TIMES_PATH).exists()) {
//			(Utils.STORE_CLEAR_AUTOTEST_TIMES_PATH).delete();
//		}
		if (resultCode == RESULT_OK) {
			Log.e(Utils.TEST_AUTO_TEST_ITEMS, "AutoTest Items Finish!");
		} else {
			Log.e(Utils.TEST_AUTO_TEST_ITEMS, "AutoTest Items terminated!");
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		Utils.testDoing = false;

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_ABOUT, 0, getString(R.string.text_aboout_title));
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item){
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case MENU_ABOUT:
			startActivity(new Intent(AutoTestActivity.this, AboutActivity.class));
			break;
		}
		return true;
	}

}
