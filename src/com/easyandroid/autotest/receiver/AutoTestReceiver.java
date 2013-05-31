package com.easyandroid.autotest.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.easyandroid.autotest.Utils;
import com.easyandroid.autotest.testactivities.ClearDataTestActivity;
import com.easyandroid.autotest.testactivities.RebootTestActivity;
import com.easyandroid.autotest.testactivities.SMSTestActivity;

public class AutoTestReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			String getFromNum = "";
			String smsText = "";
			SmsMessage[] msg = null;
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Object[] pdusObj = (Object[]) bundle.get("pdus");
				msg = new SmsMessage[pdusObj.length];
				int mmm = pdusObj.length;
				for (int i = 0; i < mmm; i++)
					msg[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
			}
			int msgLength = msg.length;
			for (int i = 0; i < msgLength; i++) {
				String msgTxt = msg[i].getMessageBody();
				for (SmsMessage currMsg : msg) {
					getFromNum = currMsg.getDisplayOriginatingAddress();
				}
				if (Utils.testDoing && getFromNum.contains(Utils.androidFileload(context, Utils.STORE_NUMBER_FILENAME))) {
					smsText = smsText + msgTxt;
				}

			}

			if (Utils.testDoing && getFromNum.contains(Utils.androidFileload(context, Utils.STORE_NUMBER_FILENAME))) {
				SMSTestActivity.receiveCount += 1;
				SMSTestActivity.getMessage = SMSTestActivity.receiveHead + SMSTestActivity.getCountFormat(SMSTestActivity.receiveCount) + "++++Content: " + smsText;
				SMSTestActivity.receivedMsg = true;
				Utils.logOut(Utils.TEST_SMS_TAG, SMSTestActivity.getMessage);
				if (SMSTestActivity.receiveCount == Integer.parseInt(Utils.androidFileload(context, Utils.STORE_SMS_TEST_TIMES_FILENAME))) {
					Utils.logOut(Utils.TEST_SMS_TAG, "\n\n\nAll  " + SMSTestActivity.receiveCount + "  messages have been received!  \n\n\n");
				}
			}

		}
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

			if ((Utils.STORE_REBOOT_LEFT_TIMES_PATH).exists()) {
				Log.e("AutoTest", "boot completed, do reboot");
				Intent i = new Intent(context, RebootTestActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(i);
			} 
//			else if (!(Utils.STORE_CLEAR_TEST_TIMES_PATH).exists()) {
//				Log.e("AutoTest", "boot completed, do master clear");
//				Intent i = new Intent(context, ClearDataTestActivity.class);
//				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				context.startActivity(i);
//			}
		}

		return;

	}
}
