package com.fuwu.mobileim.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.fuwu.mobileim.R;
import com.fuwu.mobileim.util.FxApplication;

public class MyInformationActivity extends Activity {
	private ImageButton my_info_back;// 返回按钮
	private ImageButton my_info_confirm;// 保存按钮
	private FxApplication fxApplication;
	private Handler handler = new Handler() {
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
		setContentView(R.layout.my_information);
		fxApplication = (FxApplication)getApplication();
		my_info_back = (ImageButton) findViewById(R.id.my_info_back);
		my_info_back.setOnClickListener(listener1);// 给返回按钮设置监听
		my_info_confirm= (ImageButton) findViewById(R.id.my_info_confirm);
		my_info_confirm.setOnClickListener(listener2);// 给保存按钮设置监听
	}

	private View.OnClickListener listener1 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			MyInformationActivity.this.finish();
		}
	};
	
	private View.OnClickListener listener2 = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Toast.makeText(getApplicationContext(), "提交修改", Toast.LENGTH_LONG)
			.show();
		}
	};

	

}
