package com.fuwu.mobileim.util;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.fuwu.mobileim.model.Models.ClientInfo;
import com.fuwu.mobileim.model.Models.ClientInfo.OSType;
import com.fuwu.mobileim.model.Models.ClientInfoRequest;
import com.fuwu.mobileim.model.Models.ClientInfoResponse;
import com.google.protobuf.InvalidProtocolBufferException;

public class ExitService extends Service {

	private int uid;
	private String token;
	private String clientid;

	public IBinder onBind(Intent arg0) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		SharedPreferences sp = getSharedPreferences(Urlinterface.SHARED, 0);
		uid = sp.getInt("exit_user_id", 0);
		token = sp.getString("exit_Token", "null");
		clientid = sp.getString("exit_clientid", "null");
		if (FuXunTools.isConnect(this)) {
			new Thread(new UnAuthentication()).start();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	// 退出登陆
	class UnAuthentication implements Runnable {
		public void run() {
			Intent intent = new Intent(getApplicationContext(),
					ExitService.class);
			try {
				ClientInfo.Builder cinfo = ClientInfo.newBuilder();
				cinfo.setDeviceId(clientid);
				cinfo.setOsType(OSType.Android);
				cinfo.setOSVersion(android.os.Build.VERSION.RELEASE);
				cinfo.setUserId(uid);
				cinfo.setChannel(Urlinterface.current_channel);
				cinfo.setClientVersion(Urlinterface.current_version + "");
				cinfo.setIsPushEnable(false);
				ClientInfoRequest.Builder builder = ClientInfoRequest
						.newBuilder();
				builder.setUserId(uid);
				builder.setToken(token);
				builder.setClientInfo(cinfo);
				ClientInfoRequest request = builder.build();
				byte[] by = HttpUtil.sendHttps(request.toByteArray(),
						Urlinterface.Client, "PUT");
				if (by != null && by.length > 0) {
					ClientInfoResponse response = ClientInfoResponse
							.parseFrom(by);
					Log.i("exit",
							response.getIsSucceed() + "/"
									+ response.getErrorCode());
					SharedPreferences sp = getSharedPreferences(
							Urlinterface.SHARED, 0);
					sp.edit().putInt("user_id", 0).commit();
					sp.edit().putString("Token", "null").commit();
					sp.edit().putString("pwd", "").commit();
					sp.edit().putString("clientid", "").commit();
					stopService(intent);
				}
			} catch (InvalidProtocolBufferException e) {
				// e.printStackTrace();
				Log.i("exit", "退出请求失败");
				stopService(intent);
			}
		}
	}

	public void onDestroy() {
		super.onDestroy();
	}
}
