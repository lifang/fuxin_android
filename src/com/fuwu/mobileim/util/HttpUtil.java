package com.fuwu.mobileim.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.util.Log;

/**
 * @作者 马龙
 * @时间 创建时间：2014-5-23 上午9:49:59
 */
public class HttpUtil {

	/**
	 * 信任所有主机-对于任何证书都不做检查
	 */
	// private static void trustAllHosts() {
	// // Create a trust manager that does not validate certificate chains
	// // Android 采用X509的证书信息机制
	// // Install the all-trusting trust manager
	// try {
	// SSLContext sc = SSLContext.getInstance("TLS");
	// sc.init(null, xtmArray, new java.security.SecureRandom());
	// HttpsURLConnection
	// .setDefaultSSLSocketFactory(sc.getSocketFactory());
	// // HttpsURLConnection.setDefaultHostnameVerifier(DO_NOT_VERIFY);//
	// // 不进行主机名确认
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			// TODO Auto-generated method stub
			// System.out.println("Warning: URL Host: " + hostname + " vs. "
			// + session.getPeerHost());
			return true;
		}
	};

	private static class TrustAnyTrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}
	}

	private static class TrustAnyHostnameVerifier implements HostnameVerifier {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	public static byte[] post(String url, String content, String charset)
			throws NoSuchAlgorithmException, KeyManagementException,
			IOException {
		byte[] b = content.getBytes();
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, new TrustManager[] { new TrustAnyTrustManager() },
				new java.security.SecureRandom());
		Log.i("aa", "开始");
		URL console = new URL(url);
		HttpsURLConnection conn = (HttpsURLConnection) console.openConnection();
		conn.setConnectTimeout(30000);
		conn.setReadTimeout(30000);
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestProperty("Content-Length", String.valueOf(b.length));
		conn.setRequestProperty("Content-Type", "application/json");
		Log.i("aa", "1");
		conn.setSSLSocketFactory(sc.getSocketFactory());
		conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
		conn.connect();
		Log.i("aa", "2");
		Log.i("aa", conn.getResponseCode() + "");
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		out.write(b);
		// 刷新、关闭
		out.flush();
		out.close();
		InputStream is = conn.getInputStream();
		if (is != null) {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = is.read(buffer)) != -1) {
				outStream.write(buffer, 0, len);
			}
			is.close();
			Log.i("aa", "结束：" + outStream.toByteArray().length);
			return outStream.toByteArray();
		}
		return null;
	}

	public static String doPost2(String reqUrl, String code) {
		String tempLine = "";
		HttpsURLConnection url_con = null;
		String responseContent = null;
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, new TrustManager[] { new TrustAnyTrustManager() },
					new java.security.SecureRandom());
			URL url = new URL(reqUrl);
			Log.i("linshi", url.toString());
			url_con = (HttpsURLConnection) url.openConnection();
			url_con.setSSLSocketFactory(sc.getSocketFactory());
			url_con.setHostnameVerifier(new TrustAnyHostnameVerifier());
			url_con.setRequestMethod("POST");
			url_con.setConnectTimeout(5000);
			url_con.setReadTimeout(5000);
			url_con.setDoOutput(true);
			byte[] b = code.getBytes();
			url_con.getOutputStream().write(b, 0, b.length);
			url_con.getOutputStream().flush();
			url_con.getOutputStream().close();
			Log.i("linshi", "linshi-------dopost---try");
			InputStream in = url_con.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(in,
					"UTF-8"));
			tempLine = rd.readLine();
			rd.close();
			in.close();
			Log.i("linshi", tempLine);
		} catch (IOException e) {
			Log.i("linshi", "发生异常");
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		Log.i("linshi", tempLine);
		return tempLine;
	}
}
