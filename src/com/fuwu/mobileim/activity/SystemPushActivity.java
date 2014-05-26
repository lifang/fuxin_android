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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fuwu.mobileim.R;

public class SystemPushActivity extends Activity {

	private ListView mListView;
	private myListViewAdapter clvAdapter;
	private List<String> list = new ArrayList<String>();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_push);
		for (int i = 0; i < 10; i++) {
			list.add("马龙是狗,马龙大黑狗,black dog,马龙是狗,马龙大黑狗,black dog");
		}
		mListView = (ListView) findViewById(R.id.list);
		clvAdapter = new myListViewAdapter(this);
		mListView.setAdapter(clvAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

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
				arg1 = mInflater.inflate(R.layout.systempush_adpter, null);
			}
			TextView content = (TextView) arg1.findViewById(R.id.content);
			ImageView news = (ImageView) arg1.findViewById(R.id.news);
			content.setText(list.get(arg0));
			if (arg0 < 2) {
				news.setVisibility(View.VISIBLE);
				content.setTextColor(getResources().getColor(
						R.color.system_textColor));
			} else {
				news.setVisibility(View.INVISIBLE);
				content.setTextColor(getResources().getColor(
						R.color.system_textColor2));
			}
			return arg1;
		}
	}
}
