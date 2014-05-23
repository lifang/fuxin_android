package com.fuwu.mobileim.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.comdo.fuxun.R;
import com.fuwu.mobileim.pojo.ContactPojo;
import com.fuwu.mobileim.view.CircularImage;

public class TalkActivity extends Activity {
	private ListView mListView;
	private myListViewAdapter clvAdapter;
	private List<ContactPojo> list = new ArrayList<ContactPojo>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.talk);
		mListView = (ListView) findViewById(R.id.talk_listview);
		for (int i = 0; i < 20; i++) {
			list.add(new ContactPojo("联系人" + i, "5月7日", "最近的一条对话文本记录", "你好啊", 0));
		}
		clvAdapter = new myListViewAdapter(this);
		mListView.setAdapter(clvAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Toast.makeText(TalkActivity.this, "单击:" + arg2,
						Toast.LENGTH_SHORT).show();
			}
		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Toast.makeText(TalkActivity.this, "长按:" + arg2,
						Toast.LENGTH_SHORT).show();
				return false;
			}
		});

	}

	public class myListViewAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Context mcontext = null;

		public myListViewAdapter(Context context) {
			this.mcontext = context;
			this.mInflater = LayoutInflater.from(mcontext);
		}

		public int getCount() {
			return list.size();
		}

		public Object getItem(int arg0) {
			return list.get(arg0);
		}

		public long getItemId(int arg0) {
			return arg0;
		}

		public View getView(int arg0, View arg1, ViewGroup arg2) {
			if (arg1 == null) {
				arg1 = mInflater.inflate(R.layout.talk_adpter, null);
			}
			CircularImage head = (CircularImage) arg1.findViewById(R.id.head);
			TextView name = (TextView) arg1.findViewById(R.id.name);
			name.setText(list.get(arg0).getName());
			TextView content = (TextView) arg1.findViewById(R.id.content);
			content.setText(list.get(arg0).getLastMes());
			TextView dath = (TextView) arg1.findViewById(R.id.dath);
			dath.setText(list.get(arg0).getDate());
			return arg1;
		}
	}
}