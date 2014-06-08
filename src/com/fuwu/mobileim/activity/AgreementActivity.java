package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.util.Urlinterface;

public class AgreementActivity extends Activity {

	public WebView webview;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.agreement);

		webview = (WebView) findViewById(R.id.webview);
		webview.setWebViewClient(new WebViewClient() {
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				// handler.cancel(); // Android默认的处理方式
				handler.proceed(); // 接受所有网站的证书
			}
		});
		webview.loadUrl(Urlinterface.WebViewUrl);
	}
}