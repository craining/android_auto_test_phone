package com.easyandroid.autotest.receivecalltest;

import java.lang.reflect.Method;

import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;

public class PhoneUtils {
	/**
	 * get ITelephony .
	 * 
	 * @param telephony
	 * @return system ITelephony
	 * @throws Exception
	 */
	public static ITelephony getITelephony(TelephonyManager telephony) throws Exception {
		Method getITelephonyMethod = telephony.getClass().getDeclaredMethod("getITelephony");
		getITelephonyMethod.setAccessible(true); // private method can be used.
		return (ITelephony) getITelephonyMethod.invoke(telephony);
	}
}
