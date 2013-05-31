package com.easyandroid.autotest.docalltest;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.easyandroid.autotest.Utils;

public class CallService extends Service {

	private static final int MSG_DO_CALL = 117;
	private Handler callHandler;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.i("callservice", "start");
		// wait  and do call
		callHandler = new callHandler();
		callHandler.sendEmptyMessageDelayed(MSG_DO_CALL, Utils.callWaitTime*1000);
		super.onStart(intent, startId);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private class callHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_DO_CALL: {
				if(Utils.testOn) {
					Intent myIntentDial = new Intent ("android.intent.action.CALL", Uri.parse("tel:"+ Utils.callNumber) );
					myIntentDial.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(myIntentDial);
				}
				stopSelf();
				break;
			}
			 
			default:
				break;
			}
		}
	}
	
}
