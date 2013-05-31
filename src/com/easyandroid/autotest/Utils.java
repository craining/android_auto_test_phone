package com.easyandroid.autotest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.http.util.EncodingUtils;

import android.content.Context;
import android.util.Log;

public class Utils {

	public static boolean testDoing = false;

	public static boolean autoTesting = false;// is doing the test of many items or not

	private static final String FILE_ENCODING = "utf-8";

	public static final int LISTENTIME = 1000;// listen the state in some test item

	public static final String TAG_SELECTED_ITEMS = "selected";

	public static final String TEST_AUTO_TEST_ITEMS = "AutoTestItems";
	public static final String TEST_WIFI_TAG = "WifiTest";
	public static final String TEST_BLUETOOTH_TAG = "BluetoothTest";
	public static final String TEST_GPS_TAG = "GpsTest";
	public static final String TEST_AIRPLANE_TAG = "AirplaneTest";
	public static final String TEST_SCREEN_TAG = "ScreenTest";
	public static final String TEST_AUDIO_TAG = "AudioTest";
	public static final String TEST_SMS_TAG = "SmsTest";
	public static final String TEST_SYSSLEEP_TAG = "SystemSleepTest";
	public static final String TEST_REBOOT_TAG = "RebootTest";
	public static final String TEST_CLEAR_TAG = "WipeDataTest";
	public static final String TEST_CALL_TAG = "CallTest";

	public static final String TAG_SMS_CONTENT_HEAD = "easyandroid";

	// default test times
	public static final int TEST_TIMES_WIFI = 10;
	public static final int TEST_TIMES_BLUETOOTH = 10;
	public static final int TEST_TIMES_GPS = 10;
	public static final int TEST_TIMES_AIRPLANE = 10;
	public static final int TEST_TIMES_SCREEN = 10;
	public static final int TEST_TIMES_SMS = 10;
	public static final int TEST_TIMES_SYSTEM_SLEEP = 10;
	// public static final int TEST_TIMES_AUDIO = 10;//
	public static final int TEST_TIMES_REBOOT = 10;
	// public static final int TEST_TIMES_CLEAR = 10;

	// default test wait time
	public static final int WAIT_TIME_WIFI = 10;
	public static final int WAIT_TIME_BLUETOOTH = 10;
	public static final int WAIT_TIME_GPS = 10;
	public static final int WAIT_TIME_AIRPLANE = 10;
	public static final int WAIT_TIME_SCREEN = 2;
	public static final int WAIT_TIME_SMS = 5;
	public static final int WAIT_TIME_REBOOT = 10;
	public static final int WAIT_TIME_CLEAR = 5;

	public static final int WAIT_TIME_SLEEP_GOTOSLEEP_TIME = 5;
	public static final int WAIT_TIME_SLEEP_ALARM_TIME = 20;

	public static final int AUDIO_TEST_ALL_TIME = 300;
	public static final int AUDIO_TEST_WAIT_TIME = 10;

	public static final String SMS_TEST_DEFAULT_CONTENT = "101";

	// save the last test of many items
	public static final String STORE_SELECTED_TEST_ITEMS_FILENAME = "selecteditems.cfg";
	public static final File STORE_SELECTED_TEST_ITEMS_PATH = new File("/data/data/com.easyandroid.autotest/files/selecteditems.cfg");
	// save phone number
	public static final String STORE_NUMBER_FILENAME = "numbers.cfg";
	public static final File STORE_NUMBER_PATH = new File("/data/data/com.easyandroid.autotest/files/numbers.cfg");
	// save reboot test times
	public static final String STORE_REBOOT_TEST_TIMES_FILENAME = "reboottesttimes.cfg";
	public static final File STORE_REBOOT_TEST_TIMES_PATH = new File("/data/data/com.easyandroid.autotest/files/reboottesttimes.cfg");
	// save reboot test wait time
	public static final String STORE_REBOOT_WAIT_TIME_FILENAME = "rebootwaittime.cfg";
	public static final File STORE_REBOOT_WAIT_TIME_PATH = new File("/data/data/com.easyandroid.autotest/files/rebootwaittime.cfg");
	// save reboot times
	public static final String STORE_REBOOT_LEFT_TIMES_FILENAME = "rebootlefttimes.cfg";
	public static final File STORE_REBOOT_LEFT_TIMES_PATH = new File("/data/data/com.easyandroid.autotest/files/rebootlefttimes.cfg");
	// save sleep test times
	public static final String STORE_SLEEP_TEST_TIMES_FILENAME = "sleeptesttimes.cfg";
	public static final File STORE_SLEEP_TEST_TIMES_PATH = new File("/data/data/com.easyandroid.autotest/files/sleeptesttimes.cfg");
	// save sleep wait time
	public static final String STORE_GOTOSLEEP_TIME_FILENAME = "gotosleeptime.cfg";
	public static final File STORE_GOTOSLEEP_TIME_PATH = new File("/data/data/com.easyandroid.autotest/files/gotosleeptime.cfg");
	// save alarm time of sleep test
	public static final String STORE_ALARM_TIME_FILENAME = "alarmtime.cfg";
	public static final File STORE_ALARM_TIME_PATH = new File("/data/data/com.easyandroid.autotest/files/alarmtime.cfg");
	// save bluetooth test times
	public static final String STORE_BLUETOOTH_TIMES_FILENAME = "bluetoothtesttimes.cfg";
	public static final File STORE_BLUETOOTH_TIMES_PATH = new File("/data/data/com.easyandroid.autotest/files/bluetoothtesttimes.cfg");
	// save bluetooth test wait time
	public static final String STORE_BLUETOOTH_WAIT_TIME_FILENAME = "bluetoothwaittime.cfg";
	public static final File STORE_BLUETOOTH_WAIT_TIME_PATH = new File("/data/data/com.easyandroid.autotest/files/bluetoothwaittime.cfg");
	// save GPS test times
	public static final String STORE_GPS_TEST_TIMES_FILENAME = "gpstesttimes.cfg";
	public static final File STORE_GPS_TEST_TIMES_PATH = new File("/data/data/com.easyandroid.autotest/files/gpstesttimes.cfg");
	// save GPS test wait time
	public static final String STORE_GPS_WAIT_TIME_FILENAME = "gpswaittime.cfg";
	public static final File STORE_GPS_WAIT_TIME_PATH = new File("/data/data/com.easyandroid.autotest/files/gpswaittime.cfg");
	// save airplane test times
	public static final String STORE_AIRPLANE_TEST_TIMES_FILENAME = "airplanetesttimes.cfg";
	public static final File STORE_AIRPLANE_TEST_TIMES_PATH = new File("/data/data/com.easyandroid.autotest/files/airplanetesttimes.cfg");
	// save airplane test wait time
	public static final String STORE_AIRPLANE_WAIT_TIME_FILENAME = "airplanewaittime.cfg";
	public static final File STORE_AIRPLANE_WAIT_TIME_PATH = new File("/data/data/com.easyandroid.autotest/files/airplanewaittime.cfg");
	// save wifi test times
	public static final String STORE_WIFI_TEST_TIMES_FILENAME = "wifitesttimes.cfg";
	public static final File STORE_WIFI_TEST_TIMES_PATH = new File("/data/data/com.easyandroid.autotest/files/wifitesttimes.cfg");
	// save wifi test wait time
	public static final String STORE_WIFI_WAIT_TIME_FILENAME = "wifiwaittime.cfg";
	public static final File STORE_WIFI_WAIT_TIME_PATH = new File("/data/data/com.easyandroid.autotest/files/wifiwaittime.cfg");
	// save screen test times
	public static final String STORE_SCREEN_TEST_TIMES_FILENAME = "screentesttimes.cfg";
	public static final File STORE_SCREEN_TEST_TIMES_PATH = new File("/data/data/com.easyandroid.autotest/files/screentesttimes.cfg");
	// save screen test wait time
	public static final String STORE_SCREEN_WAIT_TIME_FILENAME = "screenwaittime.cfg";
	public static final File STORE_SCREEN_WAIT_TIME_PATH = new File("/data/data/com.easyandroid.autotest/files/screenwaittime.cfg");
	// save sms test times
	public static final String STORE_SMS_TEST_TIMES_FILENAME = "smstesttimes.cfg";
	public static final File STORE_SMS_TEST_TIMES_PATH = new File("/data/data/com.easyandroid.autotest/files/smstesttimes.cfg");
	// save sms test wait time
	public static final String STORE_SMS_WAIT_TIME_FILENAME = "smswaittime.cfg";
	public static final File STORE_SMS_WAIT_TIME_PATH = new File("/data/data/com.easyandroid.autotest/files/smswaittime.cfg");
	// save sms test message content
	public static final String STORE_SMS_CONTENT_FILENAME = "smscontent.cfg";
	public static final File STORE_SMS_CONTENT_PATH = new File("/data/data/com.easyandroid.autotest/files/smscontent.cfg");
	// save audio output test all time
	public static final String STORE_AUDIO_TEST_ALL_TIME_FILENAME = "audiotestalltime.cfg";
	public static final File STORE_AUDIO_TEST_ALL_TIME_PATH = new File("/data/data/com.easyandroid.autotest/files/audiotestalltime.cfg");
	// save audio output test wait time
	public static final String STORE_AUDIO_WAIT_TIME_FILENAME = "audiowaittime.cfg";
	public static final File STORE_AUDIO_WAIT_TIME_PATH = new File("/data/data/com.easyandroid.autotest/files/audiowaittime.cfg");
	// tag of master clear , if it exists, do clear test, or not do.
	public static final File STORE_CLEAR_TEST_TIMES_PATH = new File("/data/data/com.easyandroid.autotest/undoclear");
	// If have a test of many items, and clear test is contained, the tag will created.
	public static final File STORE_CLEAR_AUTOTEST_TIMES_PATH = new File("/data/data/com.easyandroid.autotest/clearAutoTest");

	// record the sms test log;
	public static final File STORE_SMS_TEST_LOG_PATH = new File("/data/data/com.easyandroid.autotest/files/smstestlog.cfg");
	public static boolean newSmsTest = false;

	// about the call test
	public static int callWaitTime = 5;
	public static String callNumber = "";
	public static boolean testOn = false;

	public static final String STORE_CALL_NUMBER_FILENAME = "callnumber.cfg";
	public static final File STORE_CALL_NUMBER_PATH = new File("/data/data/com.easyandroid.autocalltest/files/callnumber.cfg");
	public static final String STORE_CALL_TIMES_FILENAME = "calltimes.cfg";
	public static final File STORE_CALL_TIMES_PATH = new File("/data/data/com.easyandroid.autocalltest/files/calltimes.cfg");
	public static final String STORE_CALL_WAITTIME_FILENAME = "callwaittime.cfg";
	public static final File STORE_CALL_WAITTIME_PATH = new File("/data/data/com.easyandroid.autocalltest/files/callwaittime.cfg");

	public static final String STORE_CALL_RECEIVE_NUMBER_FILENAME = "receivecallnumber.cfg";
	public static final File STORE_CALL_RECEIVE_NUMBER_PATH = new File("/data/data/com.easyandroid.callreceivetest/files/receivecallnumber.cfg");
	public static final String STORE_CALL_RECEIVE_WAITTIME_FILENAME = "receivecallwaittime.cfg";
	public static final File STORE_CALL_RECEIVE_WAITTIME_PATH = new File("/data/data/com.easyandroid.callreceivetest/files/receivecallwaittime.cfg");

	public static void logOut(String tag, String content) {
		Log.i(tag, content);
		if (tag.equals(Utils.TEST_SMS_TAG)) {
			// if the log is output from sms test, save it to file
			if (newSmsTest) {
				newSmsTest = false;
				STORE_SMS_TEST_LOG_PATH.delete();
			}
			writeToFile("\n" + content, STORE_SMS_TEST_LOG_PATH, true);
		}
	}

	/**
	 * (android) write to file
	 * 
	 * @param fileName
	 * @param toSave
	 * @return
	 */
	public static boolean androidFileSave(Context con, String fileName, String toSave) {
		Properties properties = new Properties();
		properties.put(FILE_ENCODING, toSave);
		try {
			FileOutputStream stream = con.openFileOutput(fileName, Context.MODE_WORLD_WRITEABLE);
			properties.store(stream, "");
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	/**
	 * (android) read from file
	 * 
	 * @param fileName
	 * @return
	 */
	public static String androidFileload(Context con, String fileName) {
		Properties properties = new Properties();
		try {
			FileInputStream stream = con.openFileInput(fileName);
			properties.load(stream);
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}

		return properties.get(FILE_ENCODING).toString();
	}

	/**
	 * 
	 * @param str
	 * @param file
	 * @param add
	 * @return
	 */
	public static boolean writeToFile(String str, File file, boolean add) {
		FileOutputStream out;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			out = new FileOutputStream(file, add);
			String infoToWrite = str;
			out.write(infoToWrite.getBytes());
			out.close();

		} catch (IOException e) {
			return false;
		}

		return true;
	}

	/**
	 * @param file
	 * @return
	 */
	public static String getFileContent(File file) {
		String str = "";
		FileInputStream in;
		try {
			in = new FileInputStream(file);
			int length = (int) file.length();
			byte[] temp = new byte[length];
			in.read(temp, 0, length);
			str = EncodingUtils.getString(temp, FILE_ENCODING);
			in.close();
		} catch (IOException e) {
		}
		return str;
	}

}
