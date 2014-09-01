package com.fuwu.mobileim.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class MyDialog extends Dialog {

	public MyDialog(Context context, View layout, int style) {

		this(context, 0, layout, style);

	}

	@SuppressWarnings({ "deprecation", "static-access" })
	public MyDialog(Context context, int type, View layout, int style) {

		super(context, style);

		setContentView(layout);

		Window window = getWindow();

		WindowManager m = window.getWindowManager();
		Display d = m.getDefaultDisplay(); // 为获取屏幕宽、高

		LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
		if (type == 0) {
			p.height = p.WRAP_CONTENT; // 高度设置
			p.width = (int) (d.getWidth() * 0.8); // 宽度设置
		} else {
			p.height = p.WRAP_CONTENT;
			p.width = p.WRAP_CONTENT;
		}
		p.alpha = 1.0f; // 设置本 身透明度
		p.dimAmount = 0.6f; // 设置黑暗度
		if (type == 2) {
			p.alpha = 0.8f; // 设置本 身透明度
			p.dimAmount = 0f; // 设置黑暗度
		}
		if (type == 3) {
			p.alpha = 1.0f; // 设置本 身透明度
			p.dimAmount = 0.6f; // 设置黑暗度
		}
		window.setAttributes(p);
	}

}