package com.easyandroid.autotest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.easyandroid.autotest.testactivities.AirplaneTestActivity;
import com.easyandroid.autotest.testactivities.AudioTestActivity;
import com.easyandroid.autotest.testactivities.BluetoothTestActivity;
import com.easyandroid.autotest.testactivities.ClearDataTestActivity;
import com.easyandroid.autotest.testactivities.GpsTestActivity;
import com.easyandroid.autotest.testactivities.RebootTestActivity;
import com.easyandroid.autotest.testactivities.SMSTestActivity;
import com.easyandroid.autotest.testactivities.ScreenTestActivity;
import com.easyandroid.autotest.testactivities.SysSleepTestActivity;
import com.easyandroid.autotest.testactivities.WifiTestActivity;

public class TestSelectedActivity extends Activity {

	private boolean wifiTestOk;
	private boolean bluetoothTestOk;
	private boolean gpsTestOk;
	private boolean airplaneTestOk;
	private boolean screenTestOk;
	private boolean smsTestOk;
	private boolean sleepTestOk;
	private boolean audioTestOk;
	private boolean rebootTestOk;
	private boolean wipedataTestOk;

	private static String testItems = "";
	private int testingItem = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		testItems = bundle.getString(Utils.TAG_SELECTED_ITEMS);
		getOneSelectedItemToTest(testItems);
	}

	/**
	 * get selected items and test one item
	 */
	private void getOneSelectedItemToTest(String SelectedItems) {
		if (SelectedItems.contains(Utils.TEST_WIFI_TAG) && !wifiTestOk) {
			// 1. wifi test
			testingItem = 1;
			Utils.autoTesting = true;
			startActivityForResult(new Intent(TestSelectedActivity.this, WifiTestActivity.class), testingItem);
			Log.e(Utils.TEST_AUTO_TEST_ITEMS, "Going to test Wifi");

		} else if (SelectedItems.contains(Utils.TEST_BLUETOOTH_TAG) && !bluetoothTestOk) {
			// 2. bluetooth test
			testingItem = 2;
			Utils.autoTesting = true;
			startActivityForResult(new Intent(TestSelectedActivity.this, BluetoothTestActivity.class), testingItem);
			Log.e(Utils.TEST_AUTO_TEST_ITEMS, "Going to test Bluetooth");

		} else if (SelectedItems.contains(Utils.TEST_GPS_TAG) && !gpsTestOk) {
			// 3. GPS test
			testingItem = 3;
			Utils.autoTesting = true;
			startActivityForResult(new Intent(TestSelectedActivity.this, GpsTestActivity.class), testingItem);
			Log.e(Utils.TEST_AUTO_TEST_ITEMS, "Going to test GPS");

		} else if (SelectedItems.contains(Utils.TEST_AIRPLANE_TAG) && !airplaneTestOk) {
			// 4. Airplane test
			testingItem = 4;
			Utils.autoTesting = true;
			startActivityForResult(new Intent(TestSelectedActivity.this, AirplaneTestActivity.class), testingItem);
			Log.e(Utils.TEST_AUTO_TEST_ITEMS, "Going to test Airplane");

		} else if (SelectedItems.contains(Utils.TEST_SYSSLEEP_TAG) && !sleepTestOk) {
			// 5. system sleep test
			testingItem = 5;
			Utils.autoTesting = true;
			startActivityForResult(new Intent(TestSelectedActivity.this, SysSleepTestActivity.class), testingItem);
			Log.e(Utils.TEST_AUTO_TEST_ITEMS, "Going to test System sleep");

		} else if (SelectedItems.contains(Utils.TEST_SCREEN_TAG) && !screenTestOk) {
			// 6. screen test
			testingItem = 6;
			Utils.autoTesting = true;
			startActivityForResult(new Intent(TestSelectedActivity.this, ScreenTestActivity.class), testingItem);
			Log.e(Utils.TEST_AUTO_TEST_ITEMS, "Going to test Screen");

		} else if (SelectedItems.contains(Utils.TEST_AUDIO_TAG) && !audioTestOk) {
			// 7. audio output test
			testingItem = 7;
			Utils.autoTesting = true;
			startActivityForResult(new Intent(TestSelectedActivity.this, AudioTestActivity.class), testingItem);
			Log.e(Utils.TEST_AUTO_TEST_ITEMS, "Going to test Audio output");

		} else if (SelectedItems.contains(Utils.TEST_SMS_TAG) && !smsTestOk) {
			// 8. sms test
			testingItem = 8;
			Utils.autoTesting = true;
			startActivityForResult(new Intent(TestSelectedActivity.this, SMSTestActivity.class), testingItem);
			Log.e(Utils.TEST_AUTO_TEST_ITEMS, "Going to test SMS");

		} else if (SelectedItems.contains(Utils.TEST_REBOOT_TAG) && !rebootTestOk) {
			// 9. reboot test
			testingItem = 9;
			Utils.autoTesting = true;
			startActivityForResult(new Intent(TestSelectedActivity.this, RebootTestActivity.class), testingItem);
			Log.e(Utils.TEST_AUTO_TEST_ITEMS, "Going to test Reboot");

		} else if (SelectedItems.contains(Utils.TEST_CLEAR_TAG) && !wipedataTestOk) {
			// 10. clear test
			testingItem = 10;
			Utils.autoTesting = true;
			startActivityForResult(new Intent(TestSelectedActivity.this, ClearDataTestActivity.class), testingItem);
			Log.e(Utils.TEST_AUTO_TEST_ITEMS, "Going to test Wipe data");

		} else {
			Utils.autoTesting = false;
			setResult(RESULT_OK);
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 1: {
				wifiTestOk = true;
				getOneSelectedItemToTest(testItems);
				break;
			}
			case 2: {
				bluetoothTestOk = true;
				getOneSelectedItemToTest(testItems);
				break;
			}
			case 3: {
				gpsTestOk = true;
				getOneSelectedItemToTest(testItems);
				break;
			}
			case 4: {
				airplaneTestOk = true;
				getOneSelectedItemToTest(testItems);
				break;
			}
			case 5: {
				sleepTestOk = true;
				getOneSelectedItemToTest(testItems);
				break;
			}
			case 6: {
				screenTestOk = true;
				getOneSelectedItemToTest(testItems);
				break;
			}
			case 7: {
				audioTestOk = true;
				getOneSelectedItemToTest(testItems);
				break;
			}
			case 8: {
				smsTestOk = true;
				getOneSelectedItemToTest(testItems);
				break;
			}
			case 9: {
				rebootTestOk = true;
				getOneSelectedItemToTest(testItems);
				break;
			}
			case 10: {
				wipedataTestOk = true;
				getOneSelectedItemToTest(testItems);
				break;
			}
			default:
				break;
			}
		} else {
			Utils.autoTesting = false;
			Toast.makeText(getBaseContext(), R.string.autotest_stop, Toast.LENGTH_LONG).show();
			setResult(RESULT_CANCELED);
			finish();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

}