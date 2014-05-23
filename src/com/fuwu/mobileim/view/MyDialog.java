package com.fuwu.mobileim.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MyDialog extends Dialog {
	private static int default_width = 160; // 默认宽度
	private static int default_height = 120;// 默认高度

	public MyDialog(Context context, View layout, int style) {

		this(context, default_width, default_height, layout, style);

	}

	@SuppressWarnings("deprecation")
	public MyDialog(Context context, int width, int height, View layout,
			int style) {

		super(context, style);

		setContentView(layout);

		Window window = getWindow();

		WindowManager m = window.getWindowManager();
		Display d = m.getDefaultDisplay(); // 为获取屏幕宽、高

		android.view.WindowManager.LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
		p.height = (int) (d.getHeight() * 0.21); // 高度设置
		p.width = (int) (d.getWidth() * 0.8); // 宽度设置
		p.alpha = 1.0f; // 设置本 身透明度
		p.dimAmount = 0.6f; // 设置黑暗度
		window.setAttributes(p);

	}

}