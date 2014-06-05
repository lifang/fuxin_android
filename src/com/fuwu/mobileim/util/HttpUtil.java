package com.fuwu.mobileim.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.util.Base64;
import android.util.Log;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-23 上午9:49:59
 */
public class HttpUtil {
	public static byte[] sendHttps(byte[] data, String url, String method) {
		byte[] bArr = null;
		try {
			String base = "\"" + Base64.encodeToString(data, Base64.DEFAULT)
					+ "\"";
			Log.i("Ax", method + ":" + base);
			Log.i("Ax", "Url:" + url);
			byte[] b = base.getBytes("UTF-8");
			SSLContext sc = SSLContext.getInstance("TLS");

			sc.init(null, new TrustManager[] { new MyTrustManager() },
					new SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection
					.setDefaultHostnameVerifier(new MyHostnameVerifier());
			HttpsURLConnection conn = (HttpsURLConnection) new URL(url)
					.openConnection();

			conn.setRequestMethod(method);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Content-Length", String.valueOf(b.length));
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.connect();

			Log.i("Ax", "conn");
			OutputStream out = conn.getOutputStream();
			out.write(b);
			out.flush();
			out.close();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null)
				sb.append(line);
			String result = sb.toString().substring(1, sb.length() - 1);
			bArr = Base64.decode(result, Base64.DEFAULT);
			Log.i("Ax", "result:" + sb.toString());
		} catch (Exception e) {
			Log.i("Ax", "error:" + e.toString());
			e.printStackTrace();
		}
		return bArr;
	}

	private static class MyHostnameVerifier implements HostnameVerifier {

		@Override
		public boolean verify(String hostname, SSLSession session) {
			// TODO Auto-generated method stub
			return true;
		}
	}

	private static class MyTrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
