package com.fuwu.mobileim.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.pojo.ContactPojo;

public class BlockManagementDisplayActivity extends Activity {
	private ListView mListView;
	private List<ContactPojo> list = new ArrayList<ContactPojo>();

	private Handler handler = new Handler() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				// prodialog.dismiss();

				break;

			case 7:
				// Toast.makeText(getApplicationContext(),
				// ExerciseBookParams.INTERNET, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.block_management_display);
		Setwindow(0.19f);// 设置窗口化
	}

	public void Setwindow(float s) {
		WindowManager m = getWindowManager();
		Display d = m.getDefaultDisplay(); // 为获取屏幕宽、高

		android.view.WindowManager.LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
		p.height = (int) (d.getHeight() * s); // 高度设置
		p.width = (int) (d.getWidth() * 0.9); // 宽度设置
		p.alpha = 1.0f; // 设置本 身透明度
		p.dimAmount = 0.8f; // 设置黑暗度
		p.y= -150;  //  设置位置
		getWindow().setAttributes(p); // 设置生效
		
		
	}

}
