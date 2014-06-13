package com.fuwu.mobileim.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {
	String tag = "SMSReceiver";

	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		Object messages[] = (Object[]) bundle.get("pdus");
		if (messages != null) {
			SmsMessage smsMessage[] = new SmsMessage[messages.length];
			for (int i = 0; i < smsMessage.length; i++) {
				smsMessage[i] = SmsMessage.createFromPdu((byte[]) messages[i]);
				// if(MyApplication.smsNum.equals(smsMessage[i].getOriginatingAddress()))
				Log.i("Max", smsMessage[i].getOriginatingAddress());
			}
		} else {
			Log.e(tag, "messages is null");
		}
	}
}