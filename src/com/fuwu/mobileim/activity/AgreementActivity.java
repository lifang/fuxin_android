package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.baidu.mobstat.StatService;
import com.fuwu.mobileim.R;
import com.fuwu.mobileim.util.Urlinterface;

public class AgreementActivity extends Activity implements OnClickListener,OnTouchListener {

	public WebView webview;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.agreement);
		findViewById(R.id.exit).setOnClickListener(this);
		findViewById(R.id.exit).setOnTouchListener(this);
		webview = (WebView) findViewById(R.id.webview);
//		webview.getSettings().set
		webview.setWebViewClient(new WebViewClient() {
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				// handler.cancel(); // Android默认的处理方式
				handler.proceed(); // 接受所有网站的证书
			}
		});
		webview.loadUrl(Urlinterface.WebViewUrl);
	}

	public void onClick(View arg0) {
		this.finish();
	}

	public void onResume() {
		super.onResume();

		/**
		 * 页面起始（每个Activity中都需要添加，如果有继承的父Activity中已经添加了该调用，那么子Activity中务必不能添加）
		 * 不能与StatService.onPageStart一级onPageEnd函数交叉使用
		 */
		StatService.onResume(this);
	}

	public void onPause() {
		super.onPause();

		/**
		 * 页面结束（每个Activity中都需要添加，如果有继承的父Activity中已经添加了该调用，那么子Activity中务必不能添加）
		 * 不能与StatService.onPageStart一级onPageEnd函数交叉使用
		 */
		StatService.onPause(this);
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			switch (v.getId()) {
			case R.id.exit:
				findViewById(R.id.exit).getBackground().setAlpha(70);
				break;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			switch (v.getId()) {
			case R.id.exit:
				findViewById(R.id.exit).getBackground().setAlpha(255);
				break;
			}
		}
		return false;
	}
}