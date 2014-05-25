package com.fuwu.mobileim.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.AdapterView.OnItemClickListener;
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
		setContentView(R.layout.block_management_display);
	}

}
